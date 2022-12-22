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

package org.limbo.flowjob.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * @author Brozen
 * @since 1.0
 */
public class UUIDUtils {

    /**
     * 生成一个UUID，去除中连接线-
     */
    public static String randomID() {
        return StringUtils.replace(UUID.randomUUID().toString(), "-", "").toUpperCase();
    }

    /**
     * 短UUID
     * 获取UUID的第一段字符串，短UUID可能重复
     */
    public static String shortRandomID() {
        return UUID.randomUUID().toString().split("-")[0];
    }

}
