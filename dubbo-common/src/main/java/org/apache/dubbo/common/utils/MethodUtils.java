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
package org.apache.dubbo.common.utils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.apache.dubbo.common.function.Streams.filterAll;
import static org.apache.dubbo.common.utils.ClassUtils.getAllSuperClasses;
import static org.apache.dubbo.common.utils.MemberUtils.isStatic;
import static org.apache.dubbo.common.utils.ReflectUtils.EMPTY_CLASS_ARRAY;
import static org.apache.dubbo.common.utils.ReflectUtils.resolveTypes;

public abstract class MethodUtils {

    public static boolean isSetter(Method method) {
        return method.getName().startsWith("set")
                && !"set".equals(method.getName())
                && Modifier.isPublic(method.getModifiers())
                && method.getParameterCount() == 1
                && ClassUtils.isPrimitive(method.getParameterTypes()[0]);
    }

    public static boolean isGetter(Method method) {
        String name = method.getName();
        return (name.startsWith("get") || name.startsWith("is"))
                && !"get".equals(name) && !"is".equals(name)
                && !"getClass".equals(name) && !"getObject".equals(name)
                && Modifier.isPublic(method.getModifiers())
                && method.getParameterTypes().length == 0
                && ClassUtils.isPrimitive(method.getReturnType());
    }

    public static boolean isMetaMethod(Method method) {
        String name = method.getName();
        if (!(name.startsWith("get") || name.startsWith("is"))) {
            return false;
        }
        if ("get".equals(name)) {
            return false;
        }
        if ("getClass".equals(name)) {
            return false;
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        if (method.getParameterTypes().length != 0) {
            return false;
        }
        if (!ClassUtils.isPrimitive(method.getReturnType())) {
            return false;
        }
        return true;
    }

    public static boolean isDeprecated(Method method) {
        return method.getAnnotation(Deprecated.class) != null;
    }

    /**
     * Find the {@link Method} by the the specified type and method name without the parameter types
     *
     * @param type       the target type
     * @param methodName the specified method name
     * @return if not found, return <code>null</code>
     * @since 2.7.6
     */
    public static Method findMethod(Class type, String methodName) {
        return findMethod(type, methodName, EMPTY_CLASS_ARRAY);
    }

    /**
     * Find the {@link Method} by the the specified type, method name and parameter types
     *
     * @param type           the target type
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @return if not found, return <code>null</code>
     * @since 2.7.6
     */
    public static Method findMethod(Class type, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = type.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
        }
        return method;
    }

    /**
     * Invoke the target object and method
     *
     * @param object           the target object
     * @param methodName       the method name
     * @param methodParameters the method parameters
     * @param <T>              the return type
     * @return the target method's execution result
     * @since 2.7.6
     */
    public static <T> T invokeMethod(Object object, String methodName, Object... methodParameters) {
        Class type = object.getClass();
        Class[] parameterTypes = resolveTypes(methodParameters);
        Method method = findMethod(type, methodName, parameterTypes);
        T value = null;

        try {
            final boolean isAccessible = method.isAccessible();

            if (!isAccessible) {
                method.setAccessible(true);
            }
            value = (T) method.invoke(object, methodParameters);
            method.setAccessible(isAccessible);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        return value;
    }

    /**
     * Get all public {@link Method methods} of the specified type
     *
     * @param targetType the target type
     * @return non-null read-only {@link List}
     * @since 2.7.6
     */
    public static List<Method> getMethods(Class<?> targetType) {
        return asList(targetType.getMethods());
    }

    /**
     * Get all public {@link Method methods} of the specified type hierarchically
     *
     * @param targetType the target type
     * @return non-null read-only {@link List}
     * @since 2.7.6
     */
    public static List<Method> getAllMethods(Class<?> targetType, Predicate<Method>... methodFilters) {

        Set<Class<?>> superClasses = getAllSuperClasses(targetType);

        Set<Method> allMethods = new LinkedHashSet<>(getMethods(targetType));

        for (Class<?> superClass : superClasses) {
            allMethods.addAll(getMethods(superClass));
        }

        return unmodifiableList(new LinkedList<>(filterAll(allMethods, methodFilters)));
    }


    /**
     * Tests whether one method, as a member of a given type,
     * overrides another method.
     *
     * @param overrider  the first method, possible overrider
     * @param overridden the second method, possibly being overridden
     * @return {@code true} if and only if the first method overrides
     * the second
     * @jls 8.4.8 Inheritance, Overriding, and Hiding
     * @jls 9.4.1 Inheritance and Overriding
     * @see Elements#overrides(ExecutableElement, ExecutableElement, TypeElement)
     */
    public static boolean overrides(Method overrider, Method overridden) {

        if (overrider == null || overridden == null) {
            return false;
        }

        // equality comparison: If two methods are same
        if (Objects.equals(overrider, overridden)) {
            return true;
        }

        // Modifiers comparison: Any method must be non-static method
        if (isStatic(overrider) || isStatic(overridden)) { //
            return false;
        }

        // Modifiers comparison: Accessibility will be ignored, trust the compiler verify

        // Inheritance comparison: The declaring class of overrider must be inherit from the overridden's
        if (!overridden.getDeclaringClass().isAssignableFrom(overrider.getDeclaringClass())) {
            return false;
        }

        // Method comparison: must not be "default" method
        if (overrider.isDefault()) {
            return false;
        }

        // Method comparison: The method name must be equal
        if (!Objects.equals(overrider.getName(), overridden.getName())) {
            return false;
        }

        // Method comparison: The count of method parameters must be equal
        if (!Objects.equals(overrider.getParameterCount(), overridden.getParameterCount())) {
            return false;
        }

        // Method comparison: Any parameter type of overrider must equal the overridden's
        for (int i = 0; i < overrider.getParameterCount(); i++) {
            if (!Objects.equals(overridden.getParameterTypes()[i], overrider.getParameterTypes()[i])) {
                return false;
            }
        }

        // Method comparison: The return type of overrider must be inherit from the overridden's
        if (!overridden.getReturnType().isAssignableFrom(overrider.getReturnType())) {
            return false;
        }

        // Throwable comparison: "throws" Throwable list will be ignored, trust the compiler verify

        return true;
    }
}
