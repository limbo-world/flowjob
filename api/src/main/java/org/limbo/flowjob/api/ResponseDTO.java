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

package org.limbo.flowjob.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.limbo.flowjob.api.constants.HttpStatus;

/**
 * @author Brozen
 * @since 2021-06-16
 */
@Getter
@NoArgsConstructor
@Schema(title = "请求响应封装")
public class ResponseDTO<T> {

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

    ResponseDTO(int code, String message, T data) {
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
     * 请求是否成功
     * @return
     */
    public boolean success() {
        return this.code == HttpStatus.SC_OK;
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
        public ResponseDTO<T> build() {
            return new ResponseDTO<>(code, message, data);
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
            this.data = data;
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
         * 设置响应状态码{@link HttpStatus#SC_UNAUTHORIZED} 未认证，未登录，并设置提示信息
         * @return 链式调用
         */
        public Builder<T> unauthorized() {
            this.code = HttpStatus.SC_UNAUTHORIZED;
            return this;
        }

        /**
         * 设置响应状态码{@link HttpStatus#SC_UNAUTHORIZED} 未认证，未登录，并设置提示信息
         * @param message 错误提示信息
         * @return 链式调用
         */
        public Builder<T> unauthorized(String message) {
            this.code = HttpStatus.SC_UNAUTHORIZED;
            this.message = message;
            return this;
        }


        /**
         * 设置响应状态码{@link HttpStatus#SC_FORBIDDEN}
         * @return 链式调用
         */
        public Builder<T> forbidden() {
            this.code = HttpStatus.SC_FORBIDDEN;
            return this;
        }

        /**
         * 设置响应状态码{@link HttpStatus#SC_FORBIDDEN} 未授权，无权限，并设置提示信息
         * @param message 错误提示信息
         * @return 链式调用
         */
        public Builder<T> forbidden(String message) {
            this.code = HttpStatus.SC_FORBIDDEN;
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
