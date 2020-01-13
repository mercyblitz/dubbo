/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dubbo.common.utils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.apache.dubbo.common.utils.ClassUtils.getAllInheritedTypes;
import static org.apache.dubbo.common.utils.ClassUtils.getAllInterfaces;
import static org.apache.dubbo.common.utils.ClassUtils.getAllSuperClasses;
import static org.apache.dubbo.common.utils.ClassUtils.isAssignableFrom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

public class ClassUtilsTest {

    @Test
    public void testForNameWithThreadContextClassLoader() throws Exception {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader classLoader = Mockito.mock(ClassLoader.class);
            Thread.currentThread().setContextClassLoader(classLoader);
            ClassUtils.forNameWithThreadContextClassLoader("a.b.c.D");
            verify(classLoader).loadClass("a.b.c.D");
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    @Test
    public void tetForNameWithCallerClassLoader() throws Exception {
        Class c = ClassUtils.forNameWithCallerClassLoader(ClassUtils.class.getName(), ClassUtilsTest.class);
        assertThat(c == ClassUtils.class, is(true));
    }

    @Test
    public void testGetCallerClassLoader() throws Exception {
        assertThat(ClassUtils.getCallerClassLoader(ClassUtilsTest.class), sameInstance(ClassUtilsTest.class.getClassLoader()));
    }

    @Test
    public void testGetClassLoader1() throws Exception {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            assertThat(ClassUtils.getClassLoader(ClassUtilsTest.class), sameInstance(oldClassLoader));
            Thread.currentThread().setContextClassLoader(null);
            assertThat(ClassUtils.getClassLoader(ClassUtilsTest.class), sameInstance(ClassUtilsTest.class.getClassLoader()));
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    @Test
    public void testGetClassLoader2() throws Exception {
        assertThat(ClassUtils.getClassLoader(), sameInstance(ClassUtils.class.getClassLoader()));
    }

    @Test
    public void testForName1() throws Exception {
        assertThat(ClassUtils.forName(ClassUtilsTest.class.getName()) == ClassUtilsTest.class, is(true));
    }

    @Test
    public void testForName2() throws Exception {
        assertThat(ClassUtils.forName("byte") == byte.class, is(true));
        assertThat(ClassUtils.forName("java.lang.String[]") == String[].class, is(true));
        assertThat(ClassUtils.forName("[Ljava.lang.String;") == String[].class, is(true));
    }

    @Test
    public void testForName3() throws Exception {
        ClassLoader classLoader = Mockito.mock(ClassLoader.class);
        ClassUtils.forName("a.b.c.D", classLoader);
        verify(classLoader).loadClass("a.b.c.D");
    }

    @Test
    public void testResolvePrimitiveClassName() throws Exception {
        assertThat(ClassUtils.resolvePrimitiveClassName("boolean") == boolean.class, is(true));
        assertThat(ClassUtils.resolvePrimitiveClassName("byte") == byte.class, is(true));
        assertThat(ClassUtils.resolvePrimitiveClassName("char") == char.class, is(true));
        assertThat(ClassUtils.resolvePrimitiveClassName("double") == double.class, is(true));
        assertThat(ClassUtils.resolvePrimitiveClassName("float") == float.class, is(true));
        assertThat(ClassUtils.resolvePrimitiveClassName("int") == int.class, is(true));
        assertThat(ClassUtils.resolvePrimitiveClassName("long") == long.class, is(true));
        assertThat(ClassUtils.resolvePrimitiveClassName("short") == short.class, is(true));
        assertThat(ClassUtils.resolvePrimitiveClassName("[Z") == boolean[].class, is(true));
        assertThat(ClassUtils.resolvePrimitiveClassName("[B") == byte[].class, is(true));
        assertThat(ClassUtils.resolvePrimitiveClassName("[C") == char[].class, is(true));
        assertThat(ClassUtils.resolvePrimitiveClassName("[D") == double[].class, is(true));
        assertThat(ClassUtils.resolvePrimitiveClassName("[F") == float[].class, is(true));
        assertThat(ClassUtils.resolvePrimitiveClassName("[I") == int[].class, is(true));
        assertThat(ClassUtils.resolvePrimitiveClassName("[J") == long[].class, is(true));
        assertThat(ClassUtils.resolvePrimitiveClassName("[S") == short[].class, is(true));
    }

    @Test
    public void testToShortString() throws Exception {
        assertThat(ClassUtils.toShortString(null), equalTo("null"));
        assertThat(ClassUtils.toShortString(new ClassUtilsTest()), startsWith("ClassUtilsTest@"));
    }

    @Test
    public void testConvertPrimitive() throws Exception {

        assertThat(ClassUtils.convertPrimitive(char.class, ""), equalTo('\0'));
        assertThat(ClassUtils.convertPrimitive(char.class, null), equalTo(null));
        assertThat(ClassUtils.convertPrimitive(char.class, "6"), equalTo('6'));

        assertThat(ClassUtils.convertPrimitive(boolean.class, ""), equalTo(Boolean.FALSE));
        assertThat(ClassUtils.convertPrimitive(boolean.class, null), equalTo(null));
        assertThat(ClassUtils.convertPrimitive(boolean.class, "true"), equalTo(Boolean.TRUE));


        assertThat(ClassUtils.convertPrimitive(byte.class, ""), equalTo(null));
        assertThat(ClassUtils.convertPrimitive(byte.class, null), equalTo(null));
        assertThat(ClassUtils.convertPrimitive(byte.class, "127"), equalTo(Byte.MAX_VALUE));


        assertThat(ClassUtils.convertPrimitive(short.class, ""), equalTo(null));
        assertThat(ClassUtils.convertPrimitive(short.class, null), equalTo(null));
        assertThat(ClassUtils.convertPrimitive(short.class, "32767"), equalTo(Short.MAX_VALUE));

        assertThat(ClassUtils.convertPrimitive(int.class, ""), equalTo(null));
        assertThat(ClassUtils.convertPrimitive(int.class, null), equalTo(null));
        assertThat(ClassUtils.convertPrimitive(int.class, "6"), equalTo(6));

        assertThat(ClassUtils.convertPrimitive(long.class, ""), equalTo(null));
        assertThat(ClassUtils.convertPrimitive(long.class, null), equalTo(null));
        assertThat(ClassUtils.convertPrimitive(long.class, "6"), equalTo(new Long(6)));

        assertThat(ClassUtils.convertPrimitive(float.class, ""), equalTo(null));
        assertThat(ClassUtils.convertPrimitive(float.class, null), equalTo(null));
        assertThat(ClassUtils.convertPrimitive(float.class, "1.1"), equalTo(new Float(1.1)));

        assertThat(ClassUtils.convertPrimitive(double.class, ""), equalTo(null));
        assertThat(ClassUtils.convertPrimitive(double.class, null), equalTo(null));
        assertThat(ClassUtils.convertPrimitive(double.class, "10.1"), equalTo(new Double(10.1)));
    }


    /**
     * Test {@link ClassUtils#getAllSuperClasses(Class, Predicate[])}
     * <p>
     * A -> LinkedHashSet -> HashSet -> AbstractSet -> AbstractCollection -> Object.class
     *
     * @since 2.7.6
     */
    @Test
    public void testGetAllSuperClasses() {
        assertTrue(CollectionUtils.equals(
                asList(LinkedHashSet.class, HashSet.class, AbstractSet.class, AbstractCollection.class, Object.class),
                getAllSuperClasses(A.class)));

        assertTrue(CollectionUtils.equals(
                asList(LinkedHashSet.class, HashSet.class, AbstractSet.class, AbstractCollection.class),
                getAllSuperClasses(A.class, type -> !Object.class.equals(type))));

        assertTrue(CollectionUtils.equals(asList(Object.class),
                getAllSuperClasses(A.class, type -> Object.class.equals(type))));
    }

    /**
     * Test {@link ClassUtils#getAllInterfaces(Class, Predicate[])}
     * <p>
     * A -> Set, Cloneable, Serializable -> Collection -> Iterable
     *
     * @since 2.7.6
     */
    @Test
    public void testGetAllInterfaces() {

        assertTrue(CollectionUtils.equals(
                asList(Set.class, Cloneable.class, Serializable.class, Collection.class, Iterable.class),
                getAllInterfaces(A.class)));

        assertTrue(CollectionUtils.equals(
                asList(Set.class, Cloneable.class, Serializable.class, Collection.class),
                getAllInterfaces(A.class, type -> !Iterable.class.equals(type))));
    }

    /**
     * Test {@link ClassUtils#getAllInheritedTypes(Class, Predicate[])}
     *
     * @since 2.7.6
     */
    @Test
    public void testGetAllInheritedTypes() {

        assertTrue(CollectionUtils.equals(asList(
                LinkedHashSet.class, HashSet.class, AbstractSet.class, AbstractCollection.class, Object.class,
                Set.class, Cloneable.class, Serializable.class, Collection.class, Iterable.class),
                getAllInheritedTypes(A.class)));

        assertTrue(CollectionUtils.equals(asList(
                LinkedHashSet.class, HashSet.class, AbstractSet.class, AbstractCollection.class, Object.class,
                Set.class, Cloneable.class, Serializable.class),
                getAllInheritedTypes(A.class, type -> !Iterable.class.equals(type), type -> !Collection.class.equals(type))));
    }


    @Test
    public void testIsAssignableFrom() {

        // null
        assertFalse(isAssignableFrom(null, null));
        assertFalse(isAssignableFrom(A.class, null));
        assertFalse(isAssignableFrom(null, A.class));

        // same type
        assertTrue(isAssignableFrom(A.class, A.class));

        // sub type
        assertTrue(isAssignableFrom(LinkedHashSet.class, A.class));
        assertTrue(isAssignableFrom(HashSet.class, A.class));
        assertTrue(isAssignableFrom(Set.class, A.class));

        // sub type : reverse
        assertFalse(isAssignableFrom(A.class, Set.class));

    }


    // A -> LinkedHashSet -> HashSet -> AbstractSet -> AbstractCollection -> Object.class
    // A -> Set, Cloneable, Serializable -> Collection -> Iterable
    static class A extends LinkedHashSet {

    }

}
