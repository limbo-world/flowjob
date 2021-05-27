/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.tracker.core.executor.dispatcher;

import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;

/**
 * 作业分发方式.
 * <ul>
 *     <li>{@linkplain JobDispatchType#ROUND_ROBIN 轮询}</li>
 *     <li>{@linkplain JobDispatchType#RANDOM 随机}</li>
 *     <li>{@linkplain JobDispatchType#APPOINT 指定节点}</li>
 *     <li>{@linkplain JobDispatchType#LEAST_FREQUENTLY_USED 最不经常使用}</li>
 *     <li>{@linkplain JobDispatchType#LEAST_RECENTLY_USED 最近最少使用}</li>
 *     <li>{@linkplain JobDispatchType#CONSISTENT_HASH 一致性hash}</li>
 * </ul>
 *
 * @author Brozen
 * @since 2021-05-16
 */
public enum JobDispatchType implements JobDispatcherFactory {

    /**
     * 轮询。
     * TODO 如何实现轮询，当worker动态增减的时候，怎么保证轮训。
     */
    ROUND_ROBIN(1, "轮询") {
        @Override
        public JobDispatcher newDispatcher(JobTracker tracker, JobContext context) {
            return new RoundRobinJobDispatcher();
        }
    },

    /**
     * 随机。将作业随机下发给某一个worker执行
     */
    RANDOM(2, "随机") {
        @Override
        public JobDispatcher newDispatcher(JobTracker jobTracker, JobContext context) {
            return new RandomJobDispatcher();
        }
    },

    /**
     * 指定节点。通过某种方式，让作业指定下发到某个worker执行。
     * TODO 根据什么指定，IP？还是worker注册时提供一个tag？
     */
    APPOINT(3, "指定节点") {
        @Override
        public JobDispatcher newDispatcher(JobTracker jobTracker, JobContext context) {
            return new AppointJobDispatcher();
        }
    },

    /**
     * 最不经常使用。将作业下发给一个时间窗口内，接收作业最少的worker。
     */
    LEAST_FREQUENTLY_USED(4, "最不经常使用") {
        @Override
        public JobDispatcher newDispatcher(JobTracker jobTracker, JobContext context) {
            return new LFUJobDispatcher();
        }
    },

    /**
     * 最近最少使用。将作业下发给一个时间窗口内，最长时间没有接受worker的worker。
     */
    LEAST_RECENTLY_USED(5, "最近最少使用") {
        @Override
        public JobDispatcher newDispatcher(JobTracker jobTracker, JobContext context) {
            return new LRUJobDispatcher();
        }
    },

    /**
     * 一致性hash。同样参数的作业将始终下发给同一台机器。
     */
    CONSISTENT_HASH(6, "一致性hash") {
        @Override
        public JobDispatcher newDispatcher(JobTracker jobTracker, JobContext context) {
            throw new UnsupportedOperationException();
        }
    },

    ;

    /**
     * 分发类型值
     */
    public final int type;

    /**
     * 分发类型描述
     */
    public final String desc;

    JobDispatchType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }


    /**
     * 解析作业分发类型。
     */
    public static JobDispatchType parse(Integer type) {
        if (type == null) {
            return null;
        }

        for (JobDispatchType dispatchType : values()) {
            if (type.equals(dispatchType.type)) {
                return dispatchType;
            }
        }

        return null;
    }

}
