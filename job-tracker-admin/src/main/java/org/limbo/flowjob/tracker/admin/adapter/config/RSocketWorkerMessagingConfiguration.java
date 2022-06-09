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

import org.limbo.utils.jackson.JacksonUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;

/**
 * 当与RSocket协议的Worker进行通信时，使用此配置
 * @author Brozen
 * @since 2021-06-29
 */
public class RSocketWorkerMessagingConfiguration {


    /**
     * 配置RSocket payload的解析器
     */
    @Bean
    public RSocketStrategies workerMessagingRSocketStrategies() {
        return RSocketStrategies.builder()
                .encoder(new Jackson2JsonEncoder(JacksonUtils.mapper))
                .decoder(new Jackson2JsonDecoder(JacksonUtils.mapper))
                .build();
    }


}
