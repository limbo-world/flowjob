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

package org.limbo.flowjob.common.utils.attribute;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 适用于并发场景
 *
 * @author Brozen
 * @since 2021-05-19
 */
public class MutableAttribute extends Attributes {
    private static final long serialVersionUID = 2246780973857365561L;

    public MutableAttribute(Attributes attributes) {
        this(attributes.toMap());
    }

    public MutableAttribute(Map<String, Object> attributes) {
        this.attributes = new ConcurrentHashMap<>(attributes);
    }

}
