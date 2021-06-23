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

package org.limbo.flowjob.tracker.commons.dto.job;

import lombok.Data;

/**
 * job执行器数据注册
 *
 * @author Devil
 * @date 2021/6/23 5:14 下午
 */
@Data
public class JobExecutorRegisterDto {
    /**
     * 名称唯一
     */
    private String name;

    private String description;

}
