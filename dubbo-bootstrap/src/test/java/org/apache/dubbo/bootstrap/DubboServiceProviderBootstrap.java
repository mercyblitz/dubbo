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
package org.apache.dubbo.bootstrap;

import org.apache.curator.test.TestingServer;

import java.io.IOException;

/**
 * Dubbo Provider Bootstrap
 *
 * @since 2.7.4
 */
public class DubboServiceProviderBootstrap {

    private static final String ZK_ADDRESS = "127.0.0.1";

    private static final int ZK_PORT = 2181;

    public static void main(String[] args) throws Exception {

        String address = "zookeeper://" + ZK_ADDRESS + ":" + ZK_PORT + "?registry-type=service";

        TestingServer testingServer = new TestingServer(2181, true);

        new DubboBootstrap()
                .application("dubbo-provider-demo")
                // Zookeeper in service registry type
                .registry("zookeeper", builder -> builder.address(address))
                // Nacos
//                .registry("nacos", builder -> builder.address("nacos://127.0.0.1:8848?registry-type=service"))
                .protocol(builder -> builder.port(-1).name("dubbo"))
                .service(builder -> builder.id("test").interfaceClass(EchoService.class).ref(new EchoServiceImpl()))
                .start()
                .await();


        // shutdown Zookeeper server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                testingServer.close();
            } catch (IOException e) {
            }
        }));
    }
}
