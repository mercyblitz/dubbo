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
package org.apache.dubbo.rpc.model;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.model.invoker.ProviderInvokerWrapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represent a application which is using Dubbo and store basic metadata info for using
 * during the processing of RPC invoking.
 *
 * ApplicationModel includes many ProviderModel which is about published services
 * and many Consumer Model which is about subscribed services.
 *
 * adjust project structure in order to fully utilize the methods introduced here.
 */
public class ApplicationModel {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ApplicationModel.class);

    private static final String DEFAULT_APPLICATION = "default";

    public static Collection<ConsumerModel> allConsumerModels() {
        return getDefaultApplicationModel().getConsumerModels();
    }

    public static Collection<ProviderModel> allProviderModels() {
        return getDefaultApplicationModel().getProviderModels();
    }

    public static ProviderModel getProviderModel(String serviceName) {
        return getDefaultApplicationModel().providerModel(serviceName);
    }

    public static ConsumerModel getConsumerModel(String serviceName) {
        return getDefaultApplicationModel().consumerModel(serviceName);
    }

    public static void initConsumerModel(String serviceName, ConsumerModel consumerModel) {
        getDefaultApplicationModel().addConsumerModel(serviceName, consumerModel);
    }

    public static void initProviderModel(String serviceName, ProviderModel providerModel) {
        getDefaultApplicationModel().addProviderModel(serviceName, providerModel);
    }

    public static String getApplication() {
        return getDefaultApplicationModel().getName();
    }

    public static void setApplication(String application) {
        getDefaultApplicationModel().setName(application);
    }

//    private DubboServer server;

    private static Map<String, ApplicationModel> applications = new ConcurrentHashMap<>();

    /**
     * full qualified class name -> provided service
     */
    private final ConcurrentMap<String, ProviderModel> providedServices = new ConcurrentHashMap<>();
    /**
     * full qualified class name -> subscribe service
     */
    private final ConcurrentMap<String, ConsumerModel> consumedServices = new ConcurrentHashMap<>();

    private String name;

    public ApplicationModel() {
    }

    public static ApplicationModel getApplicationModel(String app) {
        return applications.get(app);
    }

    public static ApplicationModel getDefaultApplicationModel() {
        // TODO what should a default ApplicationModel like?
        return applications.computeIfAbsent(DEFAULT_APPLICATION, (key) -> new ApplicationModel());
    }

//    public DubboServer getServer() {
//        return server;
//    }
//
//    public void addServer(DubboServer server) {
//        this.server = server;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<ConsumerModel> getConsumerModels() {
        return consumedServices.values();
    }

    public Collection<ProviderModel> getProviderModels() {
        return providedServices.values();
    }

    public ProviderModel providerModel(String serviceKey) {
        return providedServices.get(serviceKey);
    }

    public ConsumerModel consumerModel(String serviceKey) {
        return consumedServices.get(serviceKey);
    }

    public void addConsumerModel(String serviceKey, ConsumerModel consumerModel) {
        if (consumedServices.putIfAbsent(serviceKey, consumerModel) != null) {
            LOGGER.warn("Already register the same consumer:" + serviceKey);
        }
    }

    public void addProviderModel(String serviceKey, ProviderModel providerModel) {
        if (providedServices.putIfAbsent(serviceKey, providerModel) != null) {
            LOGGER.warn("Already register the same:" + serviceKey);
        }
    }

    /**
     * For unit test
     */
    public void reset() {
        providedServices.clear();
        consumedServices.clear();
    }


    public <T> ProviderInvokerWrapper<T> registerProviderInvoker(Invoker<T> invoker, URL registryUrl, URL providerUrl) {
        ProviderInvokerWrapper<T> wrapperInvoker = new ProviderInvokerWrapper<>(invoker, registryUrl, providerUrl);
        ProviderModel providerModel = this.providerModel(providerUrl.getServiceKey());
        providerModel.addInvoker(wrapperInvoker);
        return wrapperInvoker;
    }

    public Collection<ProviderInvokerWrapper> getProviderInvokers(String serviceKey) {
        ProviderModel providerModel = this.providerModel(serviceKey);
        if (providerModel == null) {
            return Collections.emptySet();
        }
        return providerModel.getInvokers();
    }

    public <T> ProviderInvokerWrapper<T> getProviderInvoker(String serviceKey, Invoker<T> invoker) {
        ProviderModel providerModel = this.providerModel(serviceKey);
        return providerModel.getInvoker(invoker.getUrl().getProtocol());
    }

    public boolean isRegistered(String serviceKey) {
        return getProviderInvokers(serviceKey).stream().anyMatch(ProviderInvokerWrapper::isReg);
    }

    public void registerConsumerInvoker(Invoker invoker, String serviceKey) {
        ConsumerModel consumerModel = this.consumerModel(serviceKey);
        consumerModel.setInvoker(invoker);
    }

    public <T> Invoker<T> getConsumerInvoker(String serviceKey) {
        return (Invoker<T>) this.consumerModel(serviceKey).getInvoker();
    }
}
