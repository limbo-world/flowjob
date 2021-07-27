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
import org.limbo.flowjob.tracker.commons.constants.enums.DescribableEnum;
import org.limbo.flowjob.tracker.commons.constants.enums.DispatchType;
import org.limbo.flowjob.tracker.commons.dto.plan.ScheduleOptionDto;
import org.limbo.flowjob.tracker.core.job.ScheduleOption;
import org.limbo.utils.JacksonUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Brozen
 * @since 2021-07-27
 */
public class JacksonPropertySharpTest {


    /*public static void main(String[] args) {
        ScheduleOptionDto dto = new ScheduleOptionDto();
        dto.setScheduleDelay(Duration.ofMinutes(10));
        dto.setScheduleStartAt(LocalDateTime.now());
        System.out.println(JacksonUtils.toJSONString(dto));

        ScheduleOption opt = JacksonUtils.parseObject(JacksonUtils.toJSONString(dto), ScheduleOption.class);
        System.out.println(opt);
        System.out.println(JacksonUtils.toJSONString(opt));
    }*/



    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", DispatchType.APPOINT);
        String json = JacksonUtils.toJSONString(map);
        System.out.println(json);

        Map<String, Object> map2 = JacksonUtils.parseObject(json, new TypeReference<Map<String, Object>>() {
        });
        System.out.println(map2);

        System.out.println(DescribableEnum.describe(DispatchType.class));
    }

}
