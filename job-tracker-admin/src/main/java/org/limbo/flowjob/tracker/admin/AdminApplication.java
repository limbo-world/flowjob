package org.limbo.flowjob.tracker.admin;

import org.limbo.flowjob.tracker.infrastructure.config.JobTrackerAutoConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

/**
 * @author Brozen
 * @since 2021-06-01
 */
@SpringBootApplication
@Import(JobTrackerAutoConfiguration.class)
public class AdminApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .web(WebApplicationType.REACTIVE)
                .sources(AdminApplication.class)
                .build()
                .run(args);
    }

}
