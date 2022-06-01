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

package org.limbo.flowjob.broker.core.events;

import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import java.util.function.BiConsumer;

/**
 * bs-utils预定
 *
 * @author Brozen
 * @since 2021-08-25
 */
public final class EmitFailureHandlers {

    private EmitFailureHandlers() {}

    public static FailureHandlerResultSpec signal(SignalType signal) {
        EmitFailureHandlersSpecs.FailureHandlerSignalResultRetrySpecImpl spec
                = new EmitFailureHandlersSpecs.FailureHandlerSignalResultRetrySpecImpl();
        return spec.signal(signal);
    }

    public static FailureHandlerResultSpec signals(SignalType... signals) {
        EmitFailureHandlersSpecs.FailureHandlerSignalResultRetrySpecImpl spec
                = new EmitFailureHandlersSpecs.FailureHandlerSignalResultRetrySpecImpl();
        return spec.signals(signals);
    }


    public interface FailureHandlerSignalSpec extends CreatableFailureHandlerSpec {

        /**
         * 当失败的signalType与指定值相等时触发。
         */
        FailureHandlerResultSpec signal(SignalType signalType);

        /**
         * 当失败的signalType是指定值列表中的一个时触发。
         */
        FailureHandlerResultSpec signals(SignalType... signalTypes);

    }

    public interface FailureHandlerResultSpec {

        /**
         * 当失败的result与指定值相等时触发。
         */
        FailureHandlerRetrySpec result(Sinks.EmitResult result);

        /**
         * 当失败的result是指定值列表中的一个时触发。
         */
        FailureHandlerRetrySpec results(Sinks.EmitResult... results);

    }

    public interface FailureHandlerRetrySpec {

        /**
         * 重试retryTimes次
         * @param retryTimes 重试次数
         */
        FailureHandlerSignalSpec retry(int retryTimes);

        /**
         * 不重试
         */
        FailureHandlerSignalSpec ignore();

    }


    public interface ListenableFailureHandlerSpec {

        /**
         * 当触发重试时，执行回调
         * @param listener 回调函数
         */
        FailureHandlerSignalSpec onRetry(BiConsumer<SignalType, Sinks.EmitResult> listener);

    }


    public interface CreatableFailureHandlerSpec extends ListenableFailureHandlerSpec {

        /**
         * 结束构造emit失败处理器，并生成
         */
        Sinks.EmitFailureHandler end();

    }

}
