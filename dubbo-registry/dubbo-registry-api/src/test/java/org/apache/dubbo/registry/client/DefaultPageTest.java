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
package org.apache.dubbo.registry.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DefaultPage} Test case
 *
 * @since 2.7.2
 */
public class DefaultPageTest {

    private final DefaultPage<String> page = new DefaultPage<>(0, 20);

    @BeforeEach
    public void init() {
        page.setTotalSize(101);
        page.setData(asList("1", "2", "3"));
    }

    @Test
    public void testPage() {
        assertEquals(0, page.getRequestOffset());
        assertEquals(20, page.getRequestSize());
        assertEquals(101, page.getTotalSize());
        assertEquals(3, page.getDataSize());
        assertEquals(asList("1", "2", "3"), page.getData());
        assertTrue(page.hasData());
    }
}
