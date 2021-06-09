package org.limbo.flowjob.tracker.admin.adapter.worker.rsocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

/**
 * @author Brozen
 * @since 2021-06-09
 */
@Configuration
public class RSocketServerConfiguration {

    /**
     * RSocket帧handler，用于解析帧中的路径，交给@Controller注解Bean中的 @MessageMapping 或 @ConnectMapping 方法。
     */
    @Bean
    public RSocketMessageHandler rsocketMessageHandler() {
        RSocketMessageHandler handler = new RSocketMessageHandler();
        handler.setRouteMatcher(new PathPatternRouteMatcher());
        return handler;
    }

}
