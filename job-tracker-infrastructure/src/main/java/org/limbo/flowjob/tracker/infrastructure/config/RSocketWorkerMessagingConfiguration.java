package org.limbo.flowjob.tracker.infrastructure.config;

import org.limbo.utils.JacksonUtils;
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
