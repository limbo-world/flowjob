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

package org.limbo.flowjob.common.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.net.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.common.exception.BrokerRpcException;
import org.limbo.flowjob.common.lb.LBServer;
import org.limbo.flowjob.common.lb.LBServerRepository;
import org.limbo.flowjob.common.lb.LBStrategy;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Devil
 * @since 2023/8/4
 */
@Slf4j
public class OKHttpRpc<S extends LBServer> {

    private final OkHttpClient client;

    // application/json; charset=utf-8
    private static final String JSON_UTF_8 = com.google.common.net.MediaType.JSON_UTF_8.toString();

    private static final MediaType MEDIA_TYPE = MediaType.parse(JSON_UTF_8);

    public OKHttpRpc(LBServerRepository<S> repository, LBStrategy<S> strategy) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (repository != null) {
            builder.addInterceptor(new LoadBalanceInterceptor<>(repository, strategy));
        }
        this.client = builder.build();
    }

    protected ResponseBody executeGet(String url) {
        Request request = new Request.Builder()
                .url(url)
                .header(HttpHeaders.CONTENT_TYPE, JSON_UTF_8)
                .get()
                .build();
        Call call = client.newCall(request);
        if (log.isDebugEnabled()) {
            log.debug("call api {}", logRequest(url));
        }

        try {
            // HTTP 响应状态异常
            Response response = call.execute();
            if (!response.isSuccessful()) {
                throw new BrokerRpcException("Api access failed; " + logRequest(url) + " code=" + response.code());
            }

            // 无响应 body 是异常
            if (response.body() == null) {
                throw new BrokerRpcException("Api response empty body " + logRequest(url));
            }
            return response.body();
        } catch (IOException e) {
            throw new BrokerRpcException("Api access failed " + logRequest(url), e);
        }
    }

    /**
     * 通过 OkHttp 执行请求，并获取响应
     */
    protected ResponseBody executePost(String url, Object param) {
        String json = "";
        if (param != null) {
            json = JacksonUtils.toJSONString(param);
        }
        RequestBody body = RequestBody.create(MEDIA_TYPE, json);

        Request request = new Request.Builder()
                .url(url)
                .header(HttpHeaders.CONTENT_TYPE, JSON_UTF_8)
                .post(body)
                .build();
        Call call = client.newCall(request);

        if (log.isDebugEnabled()) {
            log.debug("call api {}", logRequest(url, json));
        }

        try {
            // HTTP 响应状态异常
            Response response = call.execute();
            if (!response.isSuccessful()) {
                throw new BrokerRpcException("Api access failed; " + logRequest(url, json) + " code=" + response.code());
            }

            // 无响应 body 是异常
            if (response.body() == null) {
                throw new BrokerRpcException("Api response empty body " + logRequest(url, json));
            }
            return response.body();
        } catch (IOException e) {
            throw new BrokerRpcException("Api access failed " + logRequest(url, json), e);
        }
    }

    protected String logRequest(String url) {
        return String.format("request[url=%s]", url);
    }

    protected String logRequest(String url, String param) {
        return String.format("request[url=%s, param=%s]", url, param);
    }

    /**
     * 通过 OkHttp 执行请求，并获取响应
     */
    protected <T> ResponseDTO<T> executePost(String url, Object param, TypeReference<ResponseDTO<T>> reference) {
        Objects.requireNonNull(reference);

        ResponseBody responseBody = executePost(url, param);
        try {
            return JacksonUtils.parseObject(responseBody.string(), reference);
        } catch (IOException e) {
            throw new BrokerRpcException("Api access failed " + logRequest(url, JacksonUtils.toJSONString(param)), e);
        }
    }

    /**
     * 通过 OkHttp 执行请求，并获取响应
     */
    protected <T> ResponseDTO<T> executeGet(String url, TypeReference<ResponseDTO<T>> reference) {
        Objects.requireNonNull(reference);

        ResponseBody responseBody = executeGet(url);
        try {
            return JacksonUtils.parseObject(responseBody.string(), reference);
        } catch (IOException e) {
            throw new BrokerRpcException("Api access failed " + logRequest(url), e);
        }
    }

}
