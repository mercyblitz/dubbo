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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import static org.apache.dubbo.common.utils.ClassUtils.resolveClass;
import static org.apache.dubbo.common.utils.MethodUtils.invokeMethod;

/**
 * Commons Annotation Utilities class
 *
 * @since 2.7.6
 */
public interface AnnotationUtils {

    /**
     * Get the attribute from the specified {@link Annotation annotation}
     *
     * @param annotation    the specified {@link Annotation annotation}
     * @param attributeName the attribute name
     * @param <T>           the type of attribute
     * @return the attribute value
     * @throws IllegalArgumentException If the attribute name can't be found
     */
    static <T> T getAttribute(Annotation annotation, String attributeName) throws IllegalArgumentException {
        return invokeMethod(annotation, attributeName);
    }

    /**
     * Tests the annotated element is annotated the specified annotations or not
     *
     * @param annotatedElement {@link AnnotatedElement}
     * @param matchAll         If <code>true</code>, checking all annotation types are present or not, or match any
     * @param annotationTypes  the specified annotation types
     * @return If the specified annotation types are present, return <code>true</code>, or <code>false</code>
     */
    static boolean isAnnotationPresent(AnnotatedElement annotatedElement,
                                       boolean matchAll,
                                       Class<? extends Annotation>... annotationTypes) {

        int size = annotationTypes == null ? 0 : annotationTypes.length;

        if (size < 1) {
            return false;
        }

        int presentCount = 0;

        for (int i = 0; i < size; i++) {
            Class<? extends Annotation> annotationType = annotationTypes[i];
            if (annotatedElement.isAnnotationPresent(annotationType)) {
                presentCount++;
            }
        }

        return matchAll ? presentCount == size : presentCount > 0;
    }

    /**
     * Tests the annotated element is annotated the specified annotations or not
     *
     * @param annotatedElement {@link AnnotatedElement}
     * @param annotationTypes  the specified annotation types
     * @return If the specified annotation types are present, return <code>true</code>, or <code>false</code>
     */
    static boolean isAnnotationPresent(AnnotatedElement annotatedElement, Class<? extends Annotation>... annotationTypes) {
        return isAnnotationPresent(annotatedElement, true, annotationTypes);
    }

    /**
     * Tests the annotated element is present any specified annotation types
     *
     * @param annotatedElement {@link AnnotatedElement}
     * @param annotationTypes  the specified annotation types
     * @return If any specified annotation types are present, return <code>true</code>
     */
    static boolean isAnyAnnotationPresent(AnnotatedElement annotatedElement,
                                          Class<? extends Annotation>... annotationTypes) {
        return isAnnotationPresent(annotatedElement, false, annotationTypes);
    }

    /**
     * Tests the annotated element is present any specified annotation types
     *
     * @param annotatedElement    {@link AnnotatedElement}
     * @param annotationClassName the class name of annotation
     * @return If any specified annotation types are present, return <code>true</code>
     */
    static boolean isAnnotationPresent(AnnotatedElement annotatedElement, String annotationClassName) {
        ClassLoader classLoader = annotatedElement.getClass().getClassLoader();
        Class<?> annotationType = resolveClass(annotationClassName, classLoader);
        return annotationType != null && Annotation.class.isAssignableFrom(annotationType) &&
                isAnnotationPresent(annotatedElement, (Class<? extends Annotation>) annotationType);
    }
}
