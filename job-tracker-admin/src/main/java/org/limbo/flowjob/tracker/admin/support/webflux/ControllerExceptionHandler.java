package org.limbo.flowjob.tracker.admin.support.webflux;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.dto.ResponseDto;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

/**
 * @author Brozen
 * @since 2021-07-05
 */
@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    public ControllerExceptionHandler() {
        System.out.println("POK");
    }

    /**
     * 所有未处理的异常最终执行分支
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseDto<?> handleException(Exception e) {
        log.error("出错了", e);
        return ResponseDto.builder().error(e.getMessage()).build();
    }


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

}
