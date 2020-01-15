/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.metadata.rest.resolver;

import org.apache.dubbo.common.utils.MethodUtils;
import org.apache.dubbo.common.utils.ServiceAnnotationResolver;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.metadata.definition.MethodDefinitionBuilder;
import org.apache.dubbo.metadata.definition.model.MethodDefinition;
import org.apache.dubbo.metadata.rest.RequestMetadata;
import org.apache.dubbo.metadata.rest.RestMethodMetadata;
import org.apache.dubbo.metadata.rest.ServiceRestMetadata;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.apache.dubbo.common.function.ThrowableFunction.execute;
import static org.apache.dubbo.common.utils.AnnotationUtils.isAnyAnnotationPresent;
import static org.apache.dubbo.common.utils.ClassUtils.forName;
import static org.apache.dubbo.common.utils.ClassUtils.getAllInterfaces;
import static org.apache.dubbo.common.utils.MethodUtils.getAllMethods;

/**
 * The abstract {@link ServiceRestMetadataResolver} class to provider some template methods assemble the instance of
 * {@link ServiceRestMetadata} will extended by the sub-classes.
 *
 * @since 2.7.6
 */
public abstract class AbstractServiceRestMetadataResolver implements ServiceRestMetadataResolver {

    @Override
    public final boolean supports(Class<?> serviceType) {
        return isImplementedInterface(serviceType) && isServiceAnnotationPresent(serviceType) && supports0(serviceType);
    }

    protected final boolean isImplementedInterface(Class<?> serviceType) {
        return !getAllInterfaces(serviceType).isEmpty();
    }

    protected final boolean isServiceAnnotationPresent(Class<?> serviceType) {
        return isAnyAnnotationPresent(serviceType, Service.class, com.alibaba.dubbo.config.annotation.Service.class);
    }

    /**
     * internal support method
     *
     * @param serviceType Dubbo Service interface or type
     * @return If supports, return <code>true</code>, or <code>false</code>
     */
    protected abstract boolean supports0(Class<?> serviceType);

    @Override
    public final ServiceRestMetadata resolve(Class<?> serviceType) {

        ServiceRestMetadata serviceRestMetadata = new ServiceRestMetadata();

        // Process ServiceRestMetadata
        processServiceRestMetadata(serviceRestMetadata, serviceType);

        // Process RestMethodMetadata
        processAllRestMethodMetadata(serviceRestMetadata, serviceType);

        return serviceRestMetadata;
    }

    /**
     * Process the service type including the sub-routines:
     * <ul>
     *     <li>{@link ServiceRestMetadata#setServiceInterface(String)}</li>
     *     <li>{@link ServiceRestMetadata#setVersion(String)}</li>
     *     <li>{@link ServiceRestMetadata#setGroup(String)}</li>
     * </ul>
     *
     * @param serviceRestMetadata {@link ServiceRestMetadata}
     * @param serviceType         Dubbo Service interface or type
     */
    protected void processServiceRestMetadata(ServiceRestMetadata serviceRestMetadata, Class<?> serviceType) {
        ServiceAnnotationResolver resolver = new ServiceAnnotationResolver(serviceType);
        serviceRestMetadata.setServiceInterface(resolver.resolveInterfaceClassName());
        serviceRestMetadata.setVersion(resolver.resolveVersion());
        serviceRestMetadata.setGroup(resolver.resolveGroup());
    }

    /**
     * Process all {@link RestMethodMetadata}
     *
     * @param serviceRestMetadata {@link ServiceRestMetadata}
     * @param serviceType         Dubbo Service interface or type
     */
    protected void processAllRestMethodMetadata(ServiceRestMetadata serviceRestMetadata, Class<?> serviceType) {

        Class<?> serviceInterfaceClass = resolveServiceInterfaceClass(serviceRestMetadata, serviceType);

        List<Method> serviceMethods = resolveServiceMethods(serviceType, serviceInterfaceClass);

        for (Method serviceMethod : serviceMethods) {
            processRestMethodMetadata(serviceMethod, serviceType, serviceInterfaceClass, serviceRestMetadata.getMeta()::add);
        }
    }

    /**
     * Resolve the service methods around the service interface class
     *
     * @param serviceType           the service interface implementation class
     * @param serviceInterfaceClass the service interface class
     * @return the declared methods in the interface and their overrider methods
     */
    protected List<Method> resolveServiceMethods(Class<?> serviceType, Class<?> serviceInterfaceClass) {
        // The service methods declared the interface
        List<Method> serviceMethods = new LinkedList<>();
        List<Method> declaredServiceMethods = getAllMethods(serviceInterfaceClass);
        List<Method> overriderMethods = getAllMethods(serviceType, method -> overrides(method, declaredServiceMethods));
        // Add the overrider methods first
        serviceMethods.addAll(overriderMethods);
        serviceMethods.addAll(declaredServiceMethods);
        return serviceMethods;
    }

    private boolean overrides(Method serviceMethod, List<Method> declaredServiceMethods) {
        for (Method declaredServiceMethod : declaredServiceMethods) {
            if (MethodUtils.overrides(serviceMethod, declaredServiceMethod)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolve the class of Dubbo Service interface
     *
     * @param serviceRestMetadata {@link ServiceRestMetadata}
     * @param serviceType         Dubbo Service interface or type
     * @return non-null
     * @throws RuntimeException If the class is not found, the {@link RuntimeException} wraps the cause will be thrown
     */
    protected Class<?> resolveServiceInterfaceClass(ServiceRestMetadata serviceRestMetadata, Class<?> serviceType) {
        return execute(serviceType.getClassLoader(), classLoader -> {
            String serviceInterface = serviceRestMetadata.getServiceInterface();
            return forName(serviceInterface, classLoader);
        });
    }

    /**
     * Process the single {@link RestMethodMetadata} by the specified {@link Consumer} if present
     *
     * @param serviceMethod         Dubbo Service method
     * @param serviceType           Dubbo Service interface or type
     * @param serviceInterfaceClass The type of Dubbo Service interface
     * @param metadataToProcess     {@link RestMethodMetadata} to process if present
     */
    protected void processRestMethodMetadata(Method serviceMethod, Class<?> serviceType,
                                             Class<?> serviceInterfaceClass,
                                             Consumer<RestMethodMetadata> metadataToProcess) {

        if (!isRestCapableMethod(serviceMethod, serviceType, serviceInterfaceClass)) {
            return;
        }

        String requestPath = resolveRequestPath(serviceMethod, serviceType, serviceInterfaceClass); // requestPath is required

        if (requestPath == null) {
            return;
        }

        String requestMethod = resolveRequestMethod(serviceMethod, serviceType, serviceInterfaceClass); // requestMethod is required

        if (requestMethod == null) {
            return;
        }

        RestMethodMetadata metadata = new RestMethodMetadata();

        MethodDefinition methodDefinition = resolveMethodDefinition(serviceMethod, serviceType, serviceInterfaceClass);
        // Set MethodDefinition
        metadata.setMethod(methodDefinition);

        // process the annotated method parameters
        processAnnotatedMethodParameters(serviceMethod, serviceType, serviceInterfaceClass, metadata);

        // process produces
        Set<String> produces = new LinkedHashSet<>();
        processProduces(serviceMethod, serviceType, serviceInterfaceClass, produces);

        // process consumes
        Set<String> consumes = new LinkedHashSet<>();
        processConsumes(serviceMethod, serviceType, serviceInterfaceClass, consumes);

        // Initialize RequestMetadata
        RequestMetadata request = metadata.getRequest();
        request.setPath(requestPath);
        request.setMethod(requestMethod);
        request.setProduces(produces);
        request.setConsumes(consumes);

        // Post-Process
        postProcessRestMethodMetadata(serviceMethod, serviceType, serviceInterfaceClass, metadata);

        // Accept RestMethodMetadata
        metadataToProcess.accept(metadata);
    }

    /**
     * Test the service method is capable of REST or not?
     *
     * @param serviceMethod         Dubbo Service method
     * @param serviceType           Dubbo Service interface or type
     * @param serviceInterfaceClass The type of Dubbo Service interface
     * @return If capable, return <code>true</code>
     */
    protected abstract boolean isRestCapableMethod(Method serviceMethod, Class<?> serviceType, Class<?> serviceInterfaceClass);

    /**
     * Resolve the request method
     *
     * @param serviceMethod         Dubbo Service method
     * @param serviceType           Dubbo Service interface or type
     * @param serviceInterfaceClass The type of Dubbo Service interface
     * @return if can't be resolve, return <code>null</code>
     */
    protected abstract String resolveRequestMethod(Method serviceMethod, Class<?> serviceType, Class<?> serviceInterfaceClass);

    /**
     * Resolve the request path
     *
     * @param serviceMethod         Dubbo Service method
     * @param serviceType           Dubbo Service interface or type
     * @param serviceInterfaceClass The type of Dubbo Service interface
     * @return if can't be resolve, return <code>null</code>
     */
    protected abstract String resolveRequestPath(Method serviceMethod, Class<?> serviceType, Class<?> serviceInterfaceClass);

    /**
     * Resolve the {@link MethodDefinition}
     *
     * @param serviceMethod         Dubbo Service method
     * @param serviceType           Dubbo Service interface or type
     * @param serviceInterfaceClass The type of Dubbo Service interface
     * @return if can't be resolve, return <code>null</code>
     * @see MethodDefinitionBuilder
     */
    protected MethodDefinition resolveMethodDefinition(Method serviceMethod, Class<?> serviceType,
                                                       Class<?> serviceInterfaceClass) {
        MethodDefinitionBuilder builder = new MethodDefinitionBuilder();
        return builder.build(serviceMethod);
    }

    private void processAnnotatedMethodParameters(Method serviceMethod, Class<?> serviceType,
                                                  Class<?> serviceInterfaceClass, RestMethodMetadata metadata) {

    }

    protected abstract void processProduces(Method serviceMethod, Class<?> serviceType, Class<?> serviceInterfaceClass,
                                            Set<String> produces);

    protected abstract void processConsumes(Method serviceMethod, Class<?> serviceType, Class<?> serviceInterfaceClass,
                                            Set<String> consumes);

    protected abstract void postProcessRestMethodMetadata(Method serviceMethod, Class<?> serviceType,
                                                          Class<?> serviceInterfaceClass, RestMethodMetadata metadata);
}
