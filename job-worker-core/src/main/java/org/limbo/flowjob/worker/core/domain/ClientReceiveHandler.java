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

package org.limbo.flowjob.worker.core.domain;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.dto.Command;
import org.limbo.flowjob.tracker.commons.dto.job.JobContextDto;
import org.limbo.utils.JacksonUtils;

/**
 * @author Devil
 * @date 2021/6/29 4:48 下午
 */
@Slf4j
public class ClientReceiveHandler extends SimpleChannelInboundHandler<Command> {

    private Worker worker;

    public ClientReceiveHandler(Worker worker) {
        this.worker = worker;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command cmd) throws Exception {
        log.info(JacksonUtils.toJSONString(cmd));
        switch (cmd.getCommand()) {
            case "submit":
                worker.submit(JacksonUtils.parseObject(cmd.getBody(), JobContextDto.class));
                break;
            case "state":
                worker.jobState(cmd.getBody());
                break;
            case "resize":
                worker.resize(Integer.parseInt(cmd.getBody()));
                break;
            default:
                break;
        }
    }

}
