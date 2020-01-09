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
package org.apache.dubbo.metadata.definition;

import org.apache.dubbo.metadata.definition.model.MethodDefinition;
import org.apache.dubbo.metadata.definition.model.TypeDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * {@link MethodDefinition} Builder based on Java Reflection
 *
 * @since 2.7.6
 */
public class MethodDefinitionBuilder {

    private final TypeDefinitionBuilder builder;

    public MethodDefinitionBuilder(TypeDefinitionBuilder builder) {
        this.builder = builder;
    }

    public MethodDefinitionBuilder() {
        this.builder = new TypeDefinitionBuilder();
    }

    /**
     * Build the instance of {@link MethodDefinition}
     *
     * @param method {@link Method}
     * @return non-null
     */
    public MethodDefinition build(Method method) {

        MethodDefinition md = new MethodDefinition();
        md.setName(method.getName());

        // Process parameter types.
        Class<?>[] paramTypes = method.getParameterTypes();
        Type[] genericParamTypes = method.getGenericParameterTypes();

        String[] parameterTypes = new String[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            TypeDefinition td = builder.build(genericParamTypes[i], paramTypes[i]);
            parameterTypes[i] = td.getType();
        }
        md.setParameterTypes(parameterTypes);

        // Process return type.
        TypeDefinition td = builder.build(method.getGenericReturnType(), method.getReturnType());
        md.setReturnType(td.getType());

        return md;
    }

}
