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
package org.apache.dubbo.config.spring.context.config;

import org.apache.dubbo.config.AbstractConfig;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * The {@link DubboConfigBeanCustomizer} class to set the bean name to {@link AbstractConfig#getId() id property} as the
 * default value.
 *
 * @since 2.7.6
 */
public class IdPropertyDefaultValueDubboConfigBeanCustomizer implements DubboConfigBeanCustomizer {

    /**
     * The bean name of {@link IdPropertyDefaultValueDubboConfigBeanCustomizer}
     */
    public static final String BEAN_NAME = "ddPropertyDefaultValueDubboConfigBeanCustomizer";

    @Override
    public void customize(String beanName, AbstractConfig dubboConfigBean) {
        String id = dubboConfigBean.getId();
        if (isEmpty(id)) {
            dubboConfigBean.setId(beanName);
        }
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
