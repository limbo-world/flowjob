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

package org.limbo.flowjob.tracker.commons.constants.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 可被描述的枚举。枚举中至少有value、desc两个属性的getter方法，value的getter将作为Jackson序列化的值，desc的getter视为返回枚举的描述信息。
 *
 * @author Brozen
 * @since 2021-07-27
 */
public interface DescribableEnum<T> {

    /**
     * 枚举值，也是Jackson进行序列化时的值。
     */
    @JsonValue
    T getValue();


    /**
     * 枚举描述信息
     */
    String getDesc();


    /**
     * 枚举描述信息缓存
     */
    Map<Class<? extends DescribableEnum<?>>, String> CACHED_DESCRIPTION = new ConcurrentHashMap<>();


    /**
     * 描述一个枚举类型。枚举的描述信息为 "value1-desc1; value2-desc2; ....."
     * @param clazz 枚举类型
     * @return 返回描述信息。
     */
    static String describe(Class<? extends DescribableEnum<?>> clazz) {
        return CACHED_DESCRIPTION.computeIfAbsent(clazz, c -> {

            StringBuilder description = new StringBuilder();
            DescribableEnum<?>[] values = clazz.getEnumConstants();
            for (DescribableEnum<?> value : values) {
                description.append(value.getValue()).append("-").append(value.getDesc()).append("; ");
            }
            return description.toString();

        });
    }

}
