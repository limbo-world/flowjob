package org.limbo.flowjob.worker.start.adapter.http.config;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.dto.ResponseDto;
import org.limbo.utils.verifies.VerifyException;
import org.limbo.utils.web.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

/**
 * @author Devil
 * @date 2021/7/1 11:01 上午
 */
@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    /**
     * 处理JSR303参数校验错误
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseDto<?> handleValidateFailedException(WebExchangeBindException e) {
        // 拼装错误信息
        StringBuilder errMsg = new StringBuilder();
        e.getFieldErrors().forEach(fe -> {
            errMsg.append(fe.getField()).append(" ")
                    .append(fe.getDefaultMessage()).append("; ");
        });

        return ResponseDto.builder().badRequest(errMsg.toString()).build();
    }

    /**
     * 校验导致的异常
     */
    @ExceptionHandler(value = VerifyException.class)
    public ResponseDto<?> handleVerifyException(VerifyException e) {
        return ResponseDto.builder().error(HttpStatus.SC_BAD_REQUEST, e.getMessage()).build();
    }


    /**
     * 所有未处理的异常最终执行分支
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseDto<?> handleException(Exception e) {
        log.error("请求执行失败", e);
        return ResponseDto.builder().error(e.getMessage()).build();
    }

}
