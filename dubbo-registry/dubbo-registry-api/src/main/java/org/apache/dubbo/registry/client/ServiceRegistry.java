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

/**
 * The common interface to register and deregister for a service registry
 *
 * @since 2.7.2
 */
public interface ServiceRegistry {

    /**
     * Registers an instance of {@link ServiceInstance}.
     *
     * @param serviceInstance an instance of {@link ServiceInstance} to be registered
     * @return If success, return <code>true</code>, or <code>false</code>
     */
    boolean register(ServiceInstance serviceInstance);

    /**
     * Deregisters an instance of {@link ServiceInstance}.
     *
     * @param serviceInstance an instance of {@link ServiceInstance} to be deregistered
     * @return If success, return <code>true</code>, or <code>false</code>
     */
    void deregister(ServiceInstance serviceInstance);

    /**
     * Closes the ServiceRegistry. This is a lifecycle method.
     */
    void close();
}
