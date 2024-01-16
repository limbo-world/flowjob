/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.application;

import org.limbo.flowjob.broker.application.config.BrokerAutoConfiguration;
import org.limbo.flowjob.broker.application.config.WebConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Brozen
 * @since 2021-06-01
 */
@SpringBootApplication
@Import({
        WebConfiguration.class,
        BrokerAutoConfiguration.class,
})
@ComponentScan(basePackages = "org.limbo.flowjob.broker")
@EntityScan(basePackages = "org.limbo.flowjob.broker.dao.entity")
@EnableJpaRepositories(value = {"org.limbo.flowjob.broker.dao.repositories"})
public class WebApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .web(WebApplicationType.SERVLET)
                .sources(WebApplication.class)
                .build()
                .run(args);
    }

}
