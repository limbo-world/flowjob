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

package org.limbo.flowjob.broker.core.dispatch;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelectArgument;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.common.utils.attribute.Attributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Brozen
 * @since 2023-02-01
 */
public class SimpleWorkerSelectArguments implements WorkerSelectArgument {

    private final Task task;

    public SimpleWorkerSelectArguments(Task task) {
        this.task = task;
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getExecutorName() {
        return task.getExecutorName();
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public DispatchOption getDispatchOption() {
        return task.getDispatchOption();
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attr = new HashMap<>();
        putStringEntry(attr, task.getContext());
        putStringEntry(attr, task.getAttributes());

        Attributes mapAttrs = task.getMapAttributes();
        if (mapAttrs != null) {
            putStringEntry(attr, task.getMapAttributes());
        }

        List<Attributes> reduceAttrs = task.getReduceAttributes();
        if (CollectionUtils.isNotEmpty(reduceAttrs)) {
            reduceAttrs.forEach(a -> putStringEntry(attr, a));
        }

        return attr;
    }


    private void putStringEntry(Map<String, String> attrMap, Attributes attr) {
        attr.toMap().entrySet().stream()
                .filter(entry -> entry.getValue() instanceof String)
                .forEach(entry -> attrMap.put(entry.getKey(), (String) entry.getValue()));
    }
}
