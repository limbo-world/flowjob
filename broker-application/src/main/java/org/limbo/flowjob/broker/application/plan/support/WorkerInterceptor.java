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

package org.limbo.flowjob.broker.application.plan.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.api.constants.WorkerHeaders;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.broker.application.plan.utils.JWTUtil;
import org.limbo.flowjob.broker.application.plan.utils.WebUtil;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Devil
 * @since 2022/11/2
 */
@Slf4j
public class WorkerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 验证会话存在
        String token = request.getHeader(WorkerHeaders.TOKEN_HEADER);
        if (StringUtils.isBlank(token)) {
            WebUtil.writeToResponse(response, JacksonUtils.toJSONString(new ResponseDTO.Builder<Void>().unauthorized("unauthorized").build()));
            return false;
        }
        try {
            JWTUtil.verifyToken(token, WorkerHeaders.TOKEN_KEY);
        } catch (Exception e) {
            WebUtil.writeToResponse(response, JacksonUtils.toJSONString(new ResponseDTO.Builder<Void>().unauthorized("authenticate fail!").build()));
            return false;
        }

        return true;
    }

}
