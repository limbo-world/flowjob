package org.limbo.flowjob.tracker.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import reactor.netty.http.client.HttpClient;

/**
 * 当与Http协议的Worker进行通信时，使用此配置
 *
 * @author Brozen
 * @since 2021-06-03
 */
public class HttpWorkerMessagingConfiguration {


    /**
     * Http通信使用的响应式HttpClient，基于reactor-netty
     */
    @Bean
    @ConditionalOnMissingBean(HttpClient.class)
    public HttpClient httpClient() {
        return HttpClient.create();
    }


}
