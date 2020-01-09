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

import org.apache.dubbo.config.annotation.Service;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.apache.dubbo.common.utils.AnnotationUtils.getAttribute;
import static org.apache.dubbo.common.utils.AnnotationUtils.isAnnotationPresent;
import static org.apache.dubbo.common.utils.AnnotationUtils.isAnyAnnotationPresent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AnnotationUtils} Test
 *
 * @since 2.7.6
 */
@Service(interfaceName = "java.lang.CharSequence", interfaceClass = CharSequence.class)
@com.alibaba.dubbo.config.annotation.Service(interfaceName = "java.lang.CharSequence", interfaceClass = CharSequence.class)
public class AnnotationUtilsTest {

    @Test
    public void testGetAttribute() {
        Annotation annotation = getClass().getAnnotation(Service.class);
        assertEquals("java.lang.CharSequence", getAttribute(annotation, "interfaceName"));
        assertEquals(CharSequence.class, getAttribute(annotation, "interfaceClass"));
        assertEquals("", getAttribute(annotation, "version"));
        assertEquals("", getAttribute(annotation, "group"));
        assertEquals("", getAttribute(annotation, "path"));
        assertEquals(true, getAttribute(annotation, "export"));
        assertEquals(false, getAttribute(annotation, "deprecated"));
    }

    @Test
    public void testIsAnnotationPresent() {
        assertTrue(isAnnotationPresent(getClass(), true, Service.class));
        assertTrue(isAnnotationPresent(getClass(), true, Service.class, com.alibaba.dubbo.config.annotation.Service.class));
        assertTrue(isAnnotationPresent(getClass(), Service.class));
        assertTrue(isAnnotationPresent(getClass(), Service.class, Service.class, com.alibaba.dubbo.config.annotation.Service.class));
        assertFalse(isAnnotationPresent(getClass(), Deprecated.class));
    }

    @Test
    public void testIsAnyAnnotationPresent() {
        assertTrue(isAnyAnnotationPresent(getClass(), Service.class, com.alibaba.dubbo.config.annotation.Service.class, Deprecated.class));
        assertTrue(isAnyAnnotationPresent(getClass(), Service.class, com.alibaba.dubbo.config.annotation.Service.class));
        assertTrue(isAnyAnnotationPresent(getClass(), Service.class, Deprecated.class));
        assertTrue(isAnyAnnotationPresent(getClass(), com.alibaba.dubbo.config.annotation.Service.class, Deprecated.class));
        assertTrue(isAnyAnnotationPresent(getClass(), Service.class));
        assertTrue(isAnyAnnotationPresent(getClass(), com.alibaba.dubbo.config.annotation.Service.class));
        assertFalse(isAnyAnnotationPresent(getClass(), Deprecated.class));
    }
}
