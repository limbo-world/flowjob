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

package org.limbo.flowjob.agent.starter.component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.agent.core.FlowjobConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Devil
 * @since 2023/8/11
 */
@Slf4j
public class H2ConnectionFactory implements FlowjobConnectionFactory {

    private String jdbUrl;

    private String username;

    private String password;

    private DataSource dataSource;

    public H2ConnectionFactory(String jdbUrl, String username, String password) {
        this.jdbUrl = jdbUrl;
        this.username = username;
        this.password = password;
    }

    @Override
    public void init() {
        synchronized (this) {
            if (dataSource != null) {
                return;
            }
            // 配置 HikariCP 连接池
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbUrl);
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                config.setUsername(username);
                config.setPassword(password);
            }
            config.setMinimumIdle(4);
            config.setMaximumPoolSize(64);
            dataSource = new HikariDataSource(config);

            log.info("H2ConnectionFactory init success url={}", jdbUrl);
        }

    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            init();
        }
        return dataSource.getConnection();
    }
}
