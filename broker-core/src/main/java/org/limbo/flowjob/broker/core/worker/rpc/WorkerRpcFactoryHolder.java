/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.worker.rpc;

import java.util.ServiceLoader;

/**
 * @author Brozen
 * @since 2022-09-21
 */
class WorkerRpcFactoryHolder {

    static final WorkerRpcFactory INSTANCE;

    static {
        WorkerRpcFactory factory = null;
        ServiceLoader<WorkerRpcFactory> loader = ServiceLoader.load(WorkerRpcFactory.class);
        for (WorkerRpcFactory found : loader) {
            factory = found;
            break;
        }

        if (factory == null) {
            String factoryClassRef = WorkerRpcFactory.class.getName();
            throw new IllegalStateException("无可用的 Worker RPC 协议工厂，请声明类继承["
                    + factoryClassRef + "]，并在 classpath:META-INF/services/"
                    + factoryClassRef + " 文件中声明实现类全路径");
        }

        INSTANCE = factory;
    }

}
