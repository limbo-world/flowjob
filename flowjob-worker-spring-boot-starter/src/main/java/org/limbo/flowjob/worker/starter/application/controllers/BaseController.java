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

package org.limbo.flowjob.worker.starter.application.controllers;

import com.google.common.base.VerifyException;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.constants.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
public class BaseController {

    /**
     * 处理JSR303参数校验错误
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseDTO<?> handleValidateFailedException(WebExchangeBindException e) {
        // 拼装错误信息
        StringBuilder errMsg = new StringBuilder();
        e.getFieldErrors().forEach(fe -> errMsg
                .append(fe.getField()).append(" ")
                .append(fe.getDefaultMessage()).append("; ")
        );

        return ResponseDTO.builder().badRequest(errMsg.toString()).build();
    }


    /**
     * 校验导致的异常
     */
    @ExceptionHandler(value = VerifyException.class)
    public ResponseDTO<?> handleVerifyException(VerifyException e) {
        return ResponseDTO.builder().error(HttpStatus.SC_BAD_REQUEST, e.getMessage()).build();
    }


    /**
     * 所有未处理的异常最终执行分支
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseDTO<?> handleException(Exception e) {
        log.error("请求执行失败", e);
        return ResponseDTO.builder().error(e.getMessage()).build();
    }

}
