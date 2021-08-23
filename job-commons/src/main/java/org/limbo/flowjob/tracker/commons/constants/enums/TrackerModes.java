/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.tracker.commons.constants.enums;

import org.apache.commons.lang3.StringUtils;

import javax.sound.midi.Track;

/**
 * @author Brozen
 * @since 2021-08-23
 */
public enum TrackerModes {

    /**
     * 单机模式
     */
    SINGLE("single", "单机模式"),

    /**
     * 主从选举模式
     */
    ELECTION("election", "主从选举模式"),

    /**
     * 集群模式
     * TODO 暂未实现
     */
    CLUSTER("cluster", "集群模式"),

    ;


    public final String mode;

    public final String desc;

    TrackerModes(String mode, String desc) {
        this.mode = mode;
        this.desc = desc;
    }


    /**
     * 解析tracker节点启动模式，默认单机模式
     */
    public static TrackerModes parse(String mode) {
        // 默认单机模式
        if (StringUtils.isBlank(mode)) {
            return SINGLE;
        }

        for (TrackerModes trackerMode : values()) {
            if (trackerMode.mode.equalsIgnoreCase(mode)) {
                return trackerMode;
            }
        }

        return SINGLE;
    }

}
