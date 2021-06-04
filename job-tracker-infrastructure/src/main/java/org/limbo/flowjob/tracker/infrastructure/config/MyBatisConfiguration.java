package org.limbo.flowjob.tracker.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Brozen
 * @since 2021-06-03
 */
@Configuration
@MapperScan("org.limbo.flowjob.tracker.dao.mybatis")
public class MyBatisConfiguration {
}
