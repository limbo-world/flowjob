package org.limbo.flowjob.tracker.infrastructure.config;

import org.limbo.flowjob.tracker.core.tracker.SimpleJobTracker;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Brozen
 * @since 2021-06-01
 */
@Configuration
@Import(RepositoryAutoConfiguration.class)
public class JobTrackerAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean(SimpleJobTracker.class)
    public SimpleJobTracker jobTracker(WorkerRepository workerRepository) {
        return new SimpleJobTracker(workerRepository);
    }





}
