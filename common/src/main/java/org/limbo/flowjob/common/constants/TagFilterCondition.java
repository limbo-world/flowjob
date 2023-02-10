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

package org.limbo.flowjob.common.constants;

public enum TagFilterCondition {

        /**
         * 存在指定名称的标签
         */
        EXISTS,

        /**
         * 不存在指定名称的标签
         */
        NOT_EXISTS,

        /**
         * 存在指定名称的标签且匹配指定值
         */
        MUST_MATCH_VALUE,

        /**
         * 存在指定名称的标签，且不匹配指定值
         */
        MUST_NOT_MATCH_VALUE,

        /**
         * 存在指定名称的标签且匹配正则表达式
         */
        MUST_MATCH_VALUE_REGEX

    }
