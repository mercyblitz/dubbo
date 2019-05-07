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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The default implementation of {@link ServiceInstance}.
 *
 * @since 2.7.2
 */
public class DefaultServiceInstance implements ServiceInstance {

    private final String serviceName;

    private final String host;

    private final int port;

    private boolean enabled = true;

    private boolean healthy = true;

    private Map<String, String> metadata = new HashMap<>();

    public DefaultServiceInstance(String serviceName, String host, int port) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultServiceInstance)) return false;
        DefaultServiceInstance that = (DefaultServiceInstance) o;
        return getPort() == that.getPort() &&
                Objects.equals(getServiceName(), that.getServiceName()) &&
                Objects.equals(getHost(), that.getHost()) &&
                Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServiceName(), getHost(), getPort(), getMetadata());
    }

    @Override
    public String toString() {
        return "DefaultServiceInstance{" +
                "serviceName='" + serviceName + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", enabled=" + enabled +
                ", healthy=" + healthy +
                ", metadata=" + metadata +
                '}';
    }
}
