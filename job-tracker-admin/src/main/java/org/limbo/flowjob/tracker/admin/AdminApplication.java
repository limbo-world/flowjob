package org.limbo.flowjob.tracker.admin;

import org.limbo.flowjob.tracker.infrastructure.config.HttpWorkerMessagingConfiguration;
import org.limbo.flowjob.tracker.infrastructure.config.JobTrackerConfiguration;
import org.limbo.flowjob.tracker.infrastructure.config.MyBatisConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

/**
 * @author Brozen
 * @since 2021-06-01
 */
@SpringBootApplication
@Import({
        JobTrackerConfiguration.class,
        MyBatisConfiguration.class,
        HttpWorkerMessagingConfiguration.class,
})
public class AdminApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .web(WebApplicationType.REACTIVE)
                .sources(AdminApplication.class)
                .build()
                .run(args);
    }

}
