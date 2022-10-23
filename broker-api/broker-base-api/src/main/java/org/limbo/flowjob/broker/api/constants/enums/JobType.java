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

package org.limbo.flowjob.broker.api.constants.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 *
 * @author Brozen
 * @since 2021-05-19
 */
public enum JobType implements DescribableEnum<Byte> {
    /**
     * 给一个节点下发的任务
     */
    NORMAL(1, "普通类型"),
    /**
     * 给每个可选中节点下发任务
     */
    BROADCAST(2, "广播类型"),
    /**
     * 拆分子任务的任务
     * 分割任务 产生后续task的切割情况
     * 后续有且只有一个map任务
     */
    SPLIT(3, "split任务"),
    /**
     * map任务
     * 根据splite返回值创建对应task
     * 前继有且只有一个splite任务
     */
    MAP(4, "Map任务"),
    /**
     * reduce任务
     * 根据map任务的返回值进行结果处理
     * 前继有且只有一个map任务
     */
    REDUCE(5, "Reduce任务"),
    ;

    /**
     * 可通过{@link #describe(Class)}方法生成，用于在swagger3为枚举添加说明
     */
    public static final String DESCRIPTION = "1-普通类型; 2-广播类型; 3-Map分片类型; 4-MapReduce类型;";

    public final byte type;

    @Getter
    public final String desc;

    @JsonCreator
    JobType(int type, String desc) {
        this(((byte) type), desc);
    }

    JobType(byte type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * 校验是否是当前状态
     *
     * @param type 待校验值
     */
    public boolean is(JobType type) {
        return equals(type);
    }

    /**
     * 校验是否是当前状态
     *
     * @param type 待校验状态值
     */
    public boolean is(Number type) {
        return type != null && type.byteValue() == this.type;
    }

    /**
     * 解析上下文状态值
     */
    public static JobType parse(Number type) {
        if (type == null) {
            return null;
        }

        for (JobType jobType : values()) {
            if (jobType.is(type)) {
                return jobType;
            }
        }

        return null;
    }

    @Override
    public Byte getValue() {
        return type;
    }
}
