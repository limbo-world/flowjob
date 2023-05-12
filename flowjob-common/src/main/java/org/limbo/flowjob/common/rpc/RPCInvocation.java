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

package org.limbo.flowjob.common.rpc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.limbo.flowjob.common.lb.Invocation;

import java.util.Collections;
import java.util.Map;

/**
 * @author Brozen
 * @since 2022-12-14
 */
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class RPCInvocation implements Invocation {

    /**
     * 调用接口的 PATH
     */
    private final String path;

    /**
     * 负载均衡参数
     */
    private final Map<String, String> lbParameters;


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getInvokeTargetId() {
        return this.path;
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Map<String, String> getLBParameters() {
        return Collections.unmodifiableMap(lbParameters);
    }

}
