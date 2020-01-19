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

import java.lang.reflect.Method;

import static org.apache.dubbo.common.utils.MethodUtils.findMethod;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link MethodComparator} Test
 *
 * @since 2.7.6
 */
public class MethodComparatorTest {

    @Test
    public void testCompare() {

        MethodComparator comparator = MethodComparator.INSTANCE;

        // Case : two methods are same
        Method m1 = findMethod(getClass(), "m");
        Method m2 = findMethod(getClass(), "m");
        assertEquals(0, comparator.compare(m1, m2));

        // Case : the different method names
        m1 = findMethod(getClass(), "m");
        m2 = findMethod(getClass(), "n");
        assertEquals(-1, comparator.compare(m1, m2));

        // Case : the same method name and different the count of parameters
        m1 = findMethod(getClass(), "m");
        m2 = findMethod(getClass(), "m", int.class, String.class);
        assertEquals(-1, comparator.compare(m1, m2));

        // Case : the same method name, the count of parameters and different parameter types
        m1 = findMethod(getClass(), "m", String.class);
        m2 = findMethod(getClass(), "m", int.class);
        assertEquals(1, comparator.compare(m1, m2));
    }

    void m() {

    }

    void m(int i) {
    }

    void m(String s) {
    }

    void m(int i, String s) {
    }

    void n() {
    }

}
