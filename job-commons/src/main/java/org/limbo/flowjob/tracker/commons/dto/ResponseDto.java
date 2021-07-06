package org.limbo.flowjob.tracker.commons.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.limbo.utils.web.HttpStatus;

/**
 * @author Brozen
 * @since 2021-06-16
 */
@Getter
@NoArgsConstructor
@Schema(title = "请求响应封装")
public class ResponseDto<T> {

    /**
     * 响应状态码，参考{@link HttpStatus}中状态码的定义
     */
    @Schema(description = "响应状态码")
    private int code;

    /**
     * 错误提示信息，可选项
     */
    @Schema(description = "错误提示信息，可选项")
    private String message;

    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;

    ResponseDto(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 获取一个响应类Builder
     * @param <T> 响应类封装的数据类型
     * @return 响应类Builder
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * 响应类Response的Builder
     */
    public static class Builder<T> {

        private int code;

        private String message;

        private T data;

        /**
         * 根据Builder的配置生成响应结果
         * @return 响应
         */
        public ResponseDto<T> build() {
            return new ResponseDto<>(code, message, data);
        }

        /**
         * 设置响应状态码{@link HttpStatus#SC_OK}
         * @return 链式调用
         */
        public Builder<T> ok() {
            this.code = HttpStatus.SC_OK;
            return this;
        }

        /**
         * 设置响应状态码{@link HttpStatus#SC_OK}，并设置响应data
         * @param data 响应的数据
         * @return 链式调用
         */
        public Builder<T> ok(T data) {
            this.code = HttpStatus.SC_OK;
            return this;
        }

        /**
         * 设置响应状态码{@link HttpStatus#SC_BAD_REQUEST}，并设置提示信息
         * @param message 错误提示信息
         * @return 链式调用
         */
        public Builder<T> badRequest(String message) {
            this.code = HttpStatus.SC_BAD_REQUEST;
            this.message = message;
            return this;
        }

        /**
         * 设置响应状态码{@link HttpStatus#SC_INTERNAL_SERVER_ERROR}
         * @param message 错误提示信息
         * @return 链式调用
         */
        public Builder<T> error(String message) {
            this.code = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            this.message = message;
            return this;
        }

        /**
         * 设置错误码和错误信息
         * @param code 错误码
         * @param message 错误信息
         * @return 链式调用
         */
        public Builder<T> error(int code, String message) {
            this.code = code;
            this.message = message;
            return this;
        }

        /**
         * 设置响应中的提示信息
         * @param message 提示信息
         * @return 链式调用
         */
        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        /**
         * 设置响应中的数据
         * @param data 响应数据
         * @return 链式调用
         */
        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

    }

}
