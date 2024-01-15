/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.core.meta.processor;

import org.limbo.flowjob.api.constants.InstanceType;

/**
 * @author Devil
 * @since 2024/1/12
 */
public class InstanceProcessorFactory {

    private final PlanInstanceProcessor planInstanceProcessor;

    private final DelayInstanceProcessor delayInstanceProcessor;

    public InstanceProcessorFactory(PlanInstanceProcessor planInstanceProcessor, DelayInstanceProcessor delayInstanceProcessor) {
        this.planInstanceProcessor = planInstanceProcessor;
        this.delayInstanceProcessor = delayInstanceProcessor;
    }

    public InstanceProcessor getProcessor(InstanceType instanceType) {
        if (InstanceType.STANDALONE == instanceType || InstanceType.WORKFLOW == instanceType) {
            return planInstanceProcessor;
        } else {
            return delayInstanceProcessor;
        }
    }
}
