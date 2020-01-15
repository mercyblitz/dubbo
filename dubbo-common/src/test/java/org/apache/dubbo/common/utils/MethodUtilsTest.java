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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.dubbo.common.utils.MethodUtils.findMethod;
import static org.apache.dubbo.common.utils.MethodUtils.findNearestOverriddenMethod;
import static org.apache.dubbo.common.utils.MethodUtils.getAllMethods;
import static org.apache.dubbo.common.utils.MethodUtils.getMethods;
import static org.apache.dubbo.common.utils.MethodUtils.invokeMethod;
import static org.apache.dubbo.common.utils.MethodUtils.overrides;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MethodUtilsTest {

    @Test
    public void testGetMethod() {
        Method getMethod = null;
        for (Method method : MethodTestClazz.class.getMethods()) {
            if (MethodUtils.isGetter(method)) {
                getMethod = method;
            }
        }
        assertNotNull(getMethod);
        Assertions.assertEquals("getValue", getMethod.getName());
    }

    @Test
    public void testSetMethod() {
        Method setMethod = null;
        for (Method method : MethodTestClazz.class.getMethods()) {
            if (MethodUtils.isSetter(method)) {
                setMethod = method;
            }
        }
        assertNotNull(setMethod);
        Assertions.assertEquals("setValue", setMethod.getName());
    }

    @Test
    public void testIsDeprecated() throws Exception {
        assertTrue(MethodUtils.isDeprecated(MethodTestClazz.class.getMethod("deprecatedMethod")));
        Assertions.assertFalse(MethodUtils.isDeprecated(MethodTestClazz.class.getMethod("getValue")));
    }

    @Test
    public void testFindMethod() {
        Method method = findMethod(MethodTestClazz.class, "getValue");
        assertNotNull(method);

        method = findMethod(MethodTestClazz.class, "setValue", String.class);
        assertNotNull(method);

        method = findMethod(MethodTestClazz.class, "setValue");
        assertNull(method);
    }

    @Test
    public void testInvokeMethod() {
        MethodTestClazz instance = new MethodTestClazz();
        String value = invokeMethod(instance, "getValue");
        assertNull(value);
        invokeMethod(instance, "setValue", "Hello,World");
        value = invokeMethod(instance, "getValue");
        assertEquals("Hello,World", value);
    }

    @Test
    public void testGetMethods() {

        List<Method> methods = getMethods(I.class);
        assertEquals(asList(findMethod(I.class, "execute", int.class, Object.class)), methods);

        methods = new LinkedList<>(getMethods(A.class));
        methods.removeAll(getMethods(Object.class));
        assertEquals(asList(findMethod(A.class, "execute", int.class, Object.class), findMethod(A.class, "execute")), methods);
    }

    @Test
    public void testGetAllMethods() {

        List<Method> methods = new LinkedList<>(getAllMethods(A.class));
        methods.removeAll(getAllMethods(Object.class));
        assertEquals(asList(
                findMethod(A.class, "execute", int.class, Object.class),
                findMethod(A.class, "execute")
        ), methods);

    }

    @Test
    public void testOverrides() {

        Method overridden = findMethod(I.class, "execute", int.class, Object.class);

        // Case: null argument
        assertFalse(overrides(overridden, null));
        assertFalse(overrides(null, overridden));
        assertFalse(overrides(null, null));

        // Case : two methods are same
        assertTrue(overrides(overridden, overridden));

        // Case : instance method and static method
        Method staticMethod = findMethod(B.class, "staticMethod");
        assertFalse(overrides(overridden, staticMethod));

        // Case : two methods with same signatures, without inheritance
        Method nonOverriddenMethod = findMethod(C.class, "execute", int.class, Object.class);
        assertFalse(overrides(overridden, nonOverriddenMethod));

        // Case : two methods with different name
        assertFalse(overrides(overridden, findMethod(B.class, "instanceMethod")));

        // Case : Default method
        assertFalse(overrides(overridden, findMethod(DI.class, "execute", int.class, Object.class)));

        // Case : overload methods with the different arguments' count
        assertFalse(overrides(overridden, findMethod(B.class, "execute")));

        // Case : overload methods with the different parameters' types
        assertFalse(overrides(overridden, findMethod(B.class, "execute", int.class, String.class)));

        // Case : override method
        Method overrider = findMethod(A.class, "execute", int.class, Object.class);
        assertTrue(overrides(overrider, overridden));

        overrider = findMethod(B.class, "execute", int.class, Object.class);
        overridden = findMethod(A.class, "execute", int.class, Object.class);
        assertTrue(overrides(overrider, overridden));
    }

    @Test
    public void testFindOverriddenMethod() {
        Method expectedOverriddenMethod = findMethod(DI.class, "execute", int.class, Object.class);
        Method overriddenMethod = findNearestOverriddenMethod(findMethod(A.class, "execute", int.class, Object.class));
        assertEquals(expectedOverriddenMethod, overriddenMethod);

        expectedOverriddenMethod = findMethod(A.class, "execute", int.class, Object.class);
        overriddenMethod = findNearestOverriddenMethod(findMethod(B.class, "execute", int.class, Object.class));
        assertEquals(expectedOverriddenMethod, overriddenMethod);

        overriddenMethod = findNearestOverriddenMethod(findMethod(C.class, "execute", int.class, Object.class));
        assertNull(overriddenMethod);
    }

    public interface I {

        Object execute(int value, Object object);

    }

    public interface DI extends I {

        @Override
        default Object execute(int value, Object object) {
            return null;
        }

    }

    public static abstract class A implements DI {

        public Object execute(int value, Object object) {
            return null;
        }

        public abstract void execute(); // No overridden method, overload method

    }

    public static class B extends A {

        @Override
        public void execute() {

        }

        public String execute(int value, Object object) {
            return null;
        }

        public void execute(int value, Integer string) {

        }

        public void execute(int value, String string) {

        }

        protected void instanceMethod() {

        }

        protected static void staticMethod() {

        }
    }

    public static class C {

        public Object execute(int value, Object object) {
            return null;
        }
    }


    public class MethodTestClazz {

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Deprecated
        public Boolean deprecatedMethod() {
            return true;
        }

    }

}
