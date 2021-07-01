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

package org.limbo.flowjob.worker.start.bak;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.dto.tcp.JobSubmitRequest;
import org.limbo.flowjob.tracker.commons.dto.tcp.MetricResponse;
import org.limbo.flowjob.worker.core.domain.Worker;

/**
 * @author Devil
 * @date 2021/6/29 4:48 下午
 */
@Slf4j
public class ClientReceiveHandler extends SimpleChannelInboundHandler<Object> {

    private Worker worker;

    public ClientReceiveHandler(Worker worker) {
        this.worker = worker;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object obj) throws Exception {
        log.info(obj.toString());

        if (obj instanceof MetricResponse) {
//            worker.setTrackers(((MetricResponse) obj).getTrackers());
        } else if (obj instanceof JobSubmitRequest) {
            JobSubmitRequest request = (JobSubmitRequest) obj;
            worker.receive(request.getId(), request.getExecutor());
        }

    }

}
