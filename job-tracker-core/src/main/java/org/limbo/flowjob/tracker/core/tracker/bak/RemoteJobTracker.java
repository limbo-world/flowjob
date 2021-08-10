///*
// * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * 	http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.limbo.flowjob.tracker.core.tracker.bak;
//
///**
// * 此类型所表示的JobTracker运行在其他JVM中，调用此JobTracker方法时，将通过RPC接口调用远程JobTracker提供的服务。
// * TODO
// *
// * @author Brozen
// * @since 2021-06-16
// */
//public abstract class RemoteJobTracker extends AbstractJobTracker {
//
//    /**
//     * RemoteJobTracker不支持此方法。
//     * @return {@inheritDoc}
//     */
//    @Override
//    public DisposableJobTracker start() {
//        throw new UnsupportedOperationException("Cannot start remote job tracker.");
//    }
//
//    /**
//     * RemoteJobTracker不支持此方法。
//     * @return {@inheritDoc}
//     */
//    @Override
//    public JobTrackerLifecycle lifecycle() {
//        throw new UnsupportedOperationException("Remote job tracker lifecycle is not supported yet.");
//    }
//
//}
