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

package org.limbo.flowjob.broker.core.domain.task;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.common.constants.TaskStatus;
import org.limbo.flowjob.common.constants.TaskType;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 作业执行上下文
 *
 * @author Brozen
 * @since 2021-05-14
 */
@Slf4j
@Getter
@Setter
@ToString
public class Task {

    private String taskId;

    private String jobInstanceId;

    private String jobId;

    private String planId;

    private String planVersion;

    /**
     * 类型
     */
    private TaskType type;

    /**
     * 状态
     */
    private TaskStatus status;

    /**
     * 此分发执行此作业上下文的worker
     */
    private String workerId;

    /**
     * 下发参数
     */
    private DispatchOption dispatchOption;

    /**
     * 执行器参数
     */
    private String executorName;

    /**
     * 全局上下文
     */
    private Attributes context = new Attributes();

    /**
     * 对应job配置的属性
     */
    private Attributes jobAttributes;

    /**
     * task参数属性
     */
    @Setter(AccessLevel.NONE)
    private Object taskAttributes;

    public void setContext(Attributes context) {
        if (context == null) {
            return;
        }
        this.context = context;
    }

    public void setTaskAttributes(TaskType type, String json) {
        if (StringUtils.isBlank(json)) {
            return;
        }
        if (TaskType.REDUCE == type) {
            List<Attributes> attrs = new ArrayList<>();
            List<Map<String, Object>> list = JacksonUtils.parseObject(json, new TypeReference<List<Map<String, Object>>>() {
            });
            if (CollectionUtils.isNotEmpty(list)) {
                for (Map<String, Object> map : list) {
                    attrs.add(new Attributes(map));
                }
            }
            this.taskAttributes = attrs;
        } else {
            Map<String, Object> map = JacksonUtils.parseObject(json, new TypeReference<Map<String, Object>>() {
            });
            this.taskAttributes = new Attributes(map);
        }
    }

    public Attributes getMapAttributes() {
        return (Attributes) taskAttributes;
    }

    public void setMapAttributes(Attributes mapAttributes) {
        this.taskAttributes = mapAttributes;
    }

    public List<Attributes> getReduceAttributes() {
        return (List<Attributes>) taskAttributes;
    }

    public void setReduceAttributes(List<Attributes> reduceAttributes) {
        this.taskAttributes = reduceAttributes;
    }

}
