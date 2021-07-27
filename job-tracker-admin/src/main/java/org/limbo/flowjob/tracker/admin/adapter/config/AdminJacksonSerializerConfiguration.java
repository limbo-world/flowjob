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

package org.limbo.flowjob.tracker.admin.adapter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.limbo.utils.JacksonUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Jackson的序列化、反序列化处理
 *
 * @author Brozen
 * @since 2021-07-27
 */
public class AdminJacksonSerializerConfiguration {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JacksonUtils.mapper;
    }

}
