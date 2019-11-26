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

import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.function.Function;

import static org.apache.dubbo.common.utils.CollectionUtils.ofSet;
import static org.apache.dubbo.common.utils.TypeUtils.findActualTypeArguments;
import static org.apache.dubbo.common.utils.TypeUtils.getAllGenericInterfaces;
import static org.apache.dubbo.common.utils.TypeUtils.getAllGenericSuperClasses;
import static org.apache.dubbo.common.utils.TypeUtils.getClassNames;
import static org.apache.dubbo.common.utils.TypeUtils.getGenericTypes;
import static org.apache.dubbo.common.utils.TypeUtils.getRawClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TypeUtils} Test cases
 *
 * @since 2.7.5
 */
public class TypeUtilsTest {

    @Test
    public void testGetAllGenericInterfaces() {
        List<ParameterizedType> genericInterfaces = getAllGenericInterfaces(String.class);
        assertEquals(1, genericInterfaces.size());
        assertEquals(getClassNames(ofSet(Comparable.class)), getClassNames(genericInterfaces));
    }

    @Test
    public void testGetAllGenericSuperClasses() {
        List<ParameterizedType> allGenericSuperClasses = getAllGenericSuperClasses(StringToBooleanFunction.class);
        int i = 0;
        assertEquals(StringFunction.class, allGenericSuperClasses.get(i++).getRawType());
        assertEquals(FunctionClass.class, allGenericSuperClasses.get(i++).getRawType());

        allGenericSuperClasses = getAllGenericSuperClasses(StringToBooleanFunction.class,
                type -> Function.class.isAssignableFrom(getRawClass(type.getRawType())));
        i = 0;
        assertEquals(StringFunction.class, allGenericSuperClasses.get(i++).getRawType());
        assertEquals(FunctionClass.class, allGenericSuperClasses.get(i++).getRawType());

        allGenericSuperClasses = getAllGenericSuperClasses(StringToBooleanFunction.class,
                type -> String.class.isAssignableFrom(getRawClass(type.getRawType())));

        assertTrue(allGenericSuperClasses.isEmpty());
    }

    @Test
    public void testGetHierarchicalGenericTypes() {
        getGenericTypes(StringToBooleanFunction.class,
                type -> getRawClass(type) != null && Function.class.isAssignableFrom(getRawClass(type)))
                .forEach(type -> {
                    System.out.println(type);
                });
    }

    @Test
    public void testGetActualTypeArguments() {

        List<Class<?>> typeArguments = findActualTypeArguments(StringToBooleanFunction.class, Function.class);

        int i = 0;
        assertEquals(String.class, typeArguments.get(i++));
        assertEquals(Boolean.class, typeArguments.get(i++));

        typeArguments = findActualTypeArguments(StringToBooleanInteger.class, ThreeArgumentTypes.class);
        i = 0;
        assertEquals(String.class, typeArguments.get(i++));
        assertEquals(Boolean.class, typeArguments.get(i++));
        assertEquals(Integer.class, typeArguments.get(i++));

        typeArguments = findActualTypeArguments(StringToBooleanInteger.class, Function.class);
        assertTrue(typeArguments.isEmpty());
    }

    static abstract class FunctionClass<T, R> implements Function<T, R> {

    }

    static abstract class StringFunction<R> extends FunctionClass<String, R> {

    }

    static class StringToBooleanFunction extends StringFunction<Boolean> implements Function<String, Boolean> {

        @Override
        public Boolean apply(String s) {
            return Boolean.valueOf(s);
        }
    }

    static class StringToBooleanInteger implements StringToBooleanOneArgumentTypes<Integer> {

    }

    static interface StringToBooleanOneArgumentTypes<C> extends StringToTwoArgumentTypes<Boolean, C> {

    }

    static interface StringToTwoArgumentTypes<B, C> extends ThreeArgumentTypes<String, B, C> {
    }


    static interface ThreeArgumentTypes<A, B, C> {

    }
}
