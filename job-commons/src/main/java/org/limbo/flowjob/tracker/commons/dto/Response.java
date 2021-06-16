package org.limbo.flowjob.tracker.commons.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.limbo.utils.web.HttpStatus;

/**
 * @author Brozen
 * @since 2021-06-16
 */
@Getter
@Schema(title = "请求响应封装")
public class Response<T> {

    /**
     * 响应状态码，参考{@link HttpStatus}中状态码的定义
     */
    @Schema(description = "响应状态码")
    private final int code;

    /**
     * 错误提示信息，可选项
     */
    @Schema(description = "错误提示信息，可选项")
    private final String message;

    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private final T data;

    Response(int code, String message, T data) {
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
        public Response<T> build() {
            return new Response<>(code, message, data);
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
