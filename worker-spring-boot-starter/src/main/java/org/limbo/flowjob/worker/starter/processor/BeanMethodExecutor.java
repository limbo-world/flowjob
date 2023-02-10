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

package org.limbo.flowjob.worker.starter.processor;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.worker.core.domain.Task;
import org.limbo.flowjob.worker.core.executor.TaskExecutor;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author Brozen
 * @since 2022-09-07
 */
@Slf4j
public class BeanMethodExecutor implements TaskExecutor {

    private final Object bean;

    private final Method method;

    @Setter
    private String name;

    @Setter
    private String description;

    public BeanMethodExecutor(Object bean, Method method) {
        this.bean = Objects.requireNonNull(bean);
        this.method = Objects.requireNonNull(method);
        this.name = method.getName();
        this.description = bean.getClass().getName() + "#" + method.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }


    /**
     * {@inheritDoc}
     * @param task 执行的任务
     */
    @Override
    public void run(Task task) {
        try {
            Object[] args = parseArgs(task);
            this.method.invoke(this.bean, args);
        } catch (ReflectiveOperationException e) {
            log.error("Invoke @Executor method error, bean={}, method={}, executorName={}",
                    this.bean.getClass().getName(), this.method.getName(), this.name);
            throw new IllegalStateException("Invoke executor [" + this.name + "] failed", e);
        }
    }


    /**
     * 解析方法入参，目前支持两种类型的入参：Task
     */
    private Object[] parseArgs(Task task) {
        Class<?>[] argTypes = this.method.getParameterTypes();
        if (argTypes.length == 0) {
            return new Object[0];
        }

        Object[] args = new Object[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            Class<?> type = argTypes[i];
            if (Task.class.isAssignableFrom(type)) {
                args[i] = task;
            } else {
                args[i] = null;
            }
        }

        return args;
    }


}
