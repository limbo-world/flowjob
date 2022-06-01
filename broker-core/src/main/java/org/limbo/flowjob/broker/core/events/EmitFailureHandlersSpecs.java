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

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.utils.verifies.Verifies;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * @author Brozen
 * @since 2021-08-25
 */
class EmitFailureHandlersSpecs {

    static final class DelegatedEmitFailureHandler implements Sinks.EmitFailureHandler {

        private final List<ConditionalEmitFailureHandler> handlers;

        private DelegatedEmitFailureHandler(List<ConditionalEmitFailureHandler> handlers) {
            if (CollectionUtils.isEmpty(handlers)) {
                throw new IllegalArgumentException();
            }
            this.handlers = handlers;
        }

        @Override
        public boolean onEmitFailure(SignalType signalType, Sinks.EmitResult emitResult) {
            for (ConditionalEmitFailureHandler handler : handlers) {
                if (handler.canApply(signalType, emitResult)) {
                    return handler.onEmitFailure(signalType, emitResult);
                }
            }

            return false;
        }
    }

    static final class ConditionalEmitFailureHandler implements Sinks.EmitFailureHandler {

        private BiPredicate<SignalType, Sinks.EmitResult> condition;

        private BiPredicate<SignalType, Sinks.EmitResult> needRetry;

        private BiConsumer<SignalType, Sinks.EmitResult> onRetry;

        public ConditionalEmitFailureHandler(BiPredicate<SignalType, Sinks.EmitResult> condition,
                                             BiPredicate<SignalType, Sinks.EmitResult> needRetry,
                                             BiConsumer<SignalType, Sinks.EmitResult> onRetry) {
            this.condition = Objects.requireNonNull(condition);
            this.needRetry = Objects.requireNonNull(needRetry);
            this.onRetry = onRetry;
        }

        boolean canApply(SignalType signal, Sinks.EmitResult result) {
            return condition.test(signal, result);
        }

        @Override
        public boolean onEmitFailure(SignalType signalType, Sinks.EmitResult emitResult) {
            boolean retry = needRetry.test(signalType, emitResult);
            if (retry && onRetry != null) {
                onRetry.accept(signalType, emitResult);
            }
            return retry;
        }

    }


    abstract static class CreatableFailureHandlerSpecImpl implements EmitFailureHandlers.CreatableFailureHandlerSpec {

        private List<ConditionalEmitFailureHandler> handlers;

        private CreatableFailureHandlerSpecImpl() {
            handlers = new LinkedList<>();
        }

        protected void addHandler(ConditionalEmitFailureHandler handler) {
            handlers.add(handler);
        }

        @Override
        public Sinks.EmitFailureHandler end() {
            buildHandler();
            return new DelegatedEmitFailureHandler(handlers);
        }

        /**
         * 构造handler
         */
        protected abstract void buildHandler();

    }


    static final class FailureHandlerSignalResultRetrySpecImpl extends CreatableFailureHandlerSpecImpl
            implements EmitFailureHandlers.FailureHandlerSignalSpec,
            EmitFailureHandlers.FailureHandlerResultSpec,
            EmitFailureHandlers.FailureHandlerRetrySpec {

        private transient boolean inBuilding;

        private Set<SignalType> signals;

        private Set<Sinks.EmitResult> results;

        private boolean needRetry;

        private int retryTimes;

        private BiConsumer<SignalType, Sinks.EmitResult> onRetry;

        FailureHandlerSignalResultRetrySpecImpl() {
            signals = new HashSet<>();
            results = new HashSet<>();
        }

        @Override
        public EmitFailureHandlers.FailureHandlerResultSpec signal(SignalType signalType) {
            buildHandler();
            inBuilding = true;

            signals.add(signalType);
            return this;
        }

        @Override
        public EmitFailureHandlers.FailureHandlerResultSpec signals(SignalType... signalTypes) {
            buildHandler();
            inBuilding = true;

            if (signalTypes == null || signalTypes.length <= 0) {
                signalTypes = SignalType.values();
            }
            signals.addAll(Arrays.asList(signalTypes));

            return this;
        }

        @Override
        public EmitFailureHandlers.FailureHandlerRetrySpec result(Sinks.EmitResult result) {
            this.results.add(result);
            return this;
        }

        @Override
        public EmitFailureHandlers.FailureHandlerRetrySpec results(Sinks.EmitResult... results) {
            if (results == null || results.length <= 0) {
                results = Sinks.EmitResult.values();
            }
            this.results.addAll(Arrays.asList(results));

            return this;
        }

        @Override
        public EmitFailureHandlers.FailureHandlerSignalSpec retry(int retryTimes) {
            Verifies.verify(retryTimes > 0, "retryTimes must be a positive integer");

            this.needRetry = true;
            this.retryTimes = retryTimes;
            return this;
        }

        @Override
        public EmitFailureHandlers.FailureHandlerSignalSpec ignore() {
            this.needRetry = false;
            return this;
        }

        @Override
        public EmitFailureHandlers.FailureHandlerSignalSpec onRetry(BiConsumer<SignalType, Sinks.EmitResult> listener) {
            this.onRetry = listener;
            return this;
        }

        /**
         * 根据DSL指定的条件，生成事件触发异常的处理器
         */
        protected void buildHandler() {
            if (!inBuilding) {
                return;
            }

            // 记录之前的参数，生成条件
            Set<SignalType> signals = this.signals;
            Set<Sinks.EmitResult> results = this.results;
            BiPredicate<SignalType, Sinks.EmitResult> condition = (signal, result) -> signals.contains(signal);
            condition = condition.and((signal, result) -> results.contains(result));

            // 记录之前的参数，生成retry
            boolean needRetry = this.needRetry;
            BiPredicate<SignalType, Sinks.EmitResult> doRetry;
            if (!needRetry) {
                doRetry = (s, r) -> false;
            } else {
                int retryTimes = this.retryTimes;
                AtomicInteger retriedTimes = new AtomicInteger(0);
                doRetry = (s, r) -> retriedTimes.getAndIncrement() < retryTimes;
            }

            // 生成handler
            addHandler(new ConditionalEmitFailureHandler(condition, doRetry, onRetry));

            // 清理参数
            this.signals = new HashSet<>();
            this.results = new HashSet<>();
            this.needRetry = false;
            this.retryTimes = 0;
            this.onRetry = null;
        }

    }

}
