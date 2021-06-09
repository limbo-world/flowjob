package org.limbo.flowjob.tracker.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.client.HttpClient;

/**
 * 当与Http协议的Worker进行通信时，使用此配置
 *
 * @author Brozen
 * @since 2021-06-03
 */
@Configuration
public class HttpWorkerMessagingConfiguration {



    @Bean
    @ConditionalOnMissingBean(HttpClient.class)
    public HttpClient httpClient() {
        return HttpClient.create();
    }


}
