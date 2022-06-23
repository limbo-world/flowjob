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

package org.limbo.flowjob.tracker.admin.test;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;
import org.limbo.flowjob.broker.api.constants.enums.DescribableEnum;
import org.limbo.flowjob.broker.api.constants.enums.LoadBalanceType;
import org.limbo.flowjob.broker.core.utils.json.JacksonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Brozen
 * @since 2021-07-27
 */
public class JacksonPropertySharpTest {

    @Test
    public void test() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", LoadBalanceType.APPOINT);
        String json = JacksonUtils.toJSONString(map);
        System.out.println(json);

        Map<String, Object> map2 = JacksonUtils.parseObject(json, new TypeReference<Map<String, Object>>() {
        });
        System.out.println(map2);

        System.out.println(DescribableEnum.describe(LoadBalanceType.class));
    }

}
