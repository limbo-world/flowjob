/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.worker.core.infrastructure;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Devil
 * @date 2021/6/10 4:51 下午
 */
public class WorkerConfig {
    /**
     * 唯一ID
     */
    private String id;
    /**
     * 租户 todo ak/sk
     */
    private String tenant;
    /**
     * 名称
     */
    private String name;
    /**
     * 队列大小
     */
    private int queueSize;
    /**
     * 服务端口
     */
    private int serverPort;
    /**
     * tracker地址 ip:host,ip:host
     */
    private String trackerAddress;
    /**
     * 标签 k=v
     */
    private List<String> tags;

    private static final String DEFAULT_PATH = "application.yaml";

    public static WorkerConfig create(String path) throws IOException, URISyntaxException {
        Yaml yaml = new Yaml();
        String config;
        if (StringUtils.isBlank(path)) {
            config = IOUtils.toString(WorkerConfig.class.getClassLoader().getResourceAsStream(DEFAULT_PATH), Charset.defaultCharset());
        } else {
            config = IOUtils.toString(new URI(path), Charset.defaultCharset());
        }
        return yaml.loadAs(config, WorkerConfig.class);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getTrackerAddress() {
        return trackerAddress;
    }

    public void setTrackerAddress(String trackerAddress) {
        this.trackerAddress = trackerAddress;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
