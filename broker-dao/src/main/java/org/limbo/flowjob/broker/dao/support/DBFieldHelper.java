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

package org.limbo.flowjob.broker.dao.support;

/**
 * @author Devil
 * @since 2022/11/14
 */
public class DBFieldHelper {

    private static final long ZERO = 0;
    /**
     * false
     */
    public static final long FALSE_LONG = ZERO;

    /**
     * 当value > 0 返回true
     * @param value
     * @return
     */
    public static boolean greaterThanZero(Long value) {
        return value != null && value > 0;
    }

    /**
     * 当value > 0 返回true
     * @param value
     * @return
     */
    public static Long boolToLong(boolean bool, Long value) {
        return bool ? (value == null ? FALSE_LONG : value) : FALSE_LONG;
    }

}
