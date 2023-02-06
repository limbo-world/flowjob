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

package org.limbo.flowjob.broker.application.config;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.ResponseDTO;
import org.limbo.flowjob.common.exception.VerifyException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2021-07-05
 */
@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    /**
     * 所有未处理的异常最终执行分支
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseDTO<?> handleException(Exception e) {
        log.error("出错了", e);
        return ResponseDTO.builder().error(e.getMessage()).build();
    }


    /**
     * 参数绑定异常，JSR303 校验失败时可能会抛出此异常
     */
    @ExceptionHandler(value = BindException.class)
    public ResponseDTO<?> handleBindException(BindException e) {
        BindingResult result = e.getBindingResult();
        if (!result.hasErrors()) {
            log.error("Arguments bind error", e);
            return ResponseDTO.builder().badRequest(e.getMessage()).build();
        }

        String msg = result.getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("\n"));

        return ResponseDTO.builder().badRequest("Arguments error: \n" + msg).build();
    }


    /**
     * 处理 JSR303 参数校验错误，WebFlux 框架下。
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseDTO<?> handleValidateFailedException(WebExchangeBindException e) {
        // 拼装错误信息
        StringBuilder errMsg = new StringBuilder();
        e.getFieldErrors().forEach(fe -> {
            errMsg.append(fe.getField()).append(" ")
                    .append(fe.getDefaultMessage()).append("; ");
        });

        return ResponseDTO.builder().badRequest(errMsg.toString()).build();
    }


    /**
     * 处理业务校验失败异常
     */
    @ExceptionHandler(VerifyException.class)
    public ResponseDTO<?> handleVerifyFailedException(VerifyException e) {
        return ResponseDTO.builder().badRequest(e.getMessage()).build();
    }

}
