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

package org.limbo.flowjob.common.utils.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.limbo.flowjob.common.utils.time.Formatters;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Devil
 * @since 1.0
 */
public class JacksonUtils {

    public static final ObjectMapper mapper = newObjectMapper();

    public static final String DEFAULT_NONE_OBJECT = "{}";

    public static final String DEFAULT_NONE_ARRAY = "[]";

    /**
     * 生成新的{@link ObjectMapper}
     */
    public static ObjectMapper newObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册JDK8的日期API处理模块
        // @since 1.0.1 Add by brozen
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // 注册LocalDateTime的类型处理，可以通过limbo.jackson.date-time-pattern环境变量指定
        String dateTimePattern = System.getProperty("limbo.jackson.date-time-pattern", Formatters.YMD_HMS);
        javaTimeModule.addSerializer(new LocalDateTimeSerializer(dateTimePattern));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimePattern));
        mapper.registerModule(javaTimeModule);

        //在反序列化时忽略在 json 中存在但 Java 对象不存在的属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //在序列化时日期格式默认为 yyyy-MM-dd HH:mm:ss
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.getDeserializationConfig().with(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        //在序列化时忽略值为 null 的属性
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }


    /**
     * 将对象转换为JSON字符串
     */
    public static <T> String toJSONString(T t, ObjectMapper mapper) {
        Objects.requireNonNull(t);
        try {
            return mapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Jackson序列化失败！type=" + t.getClass().getName(), e);
        }
    }

    /**
     * 将对象转换为JSON字符串
     */
    public static <T> String toJSONString(T t) {
        Objects.requireNonNull(t);
        try {
            return mapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Jackson序列化失败！type=" + t.getClass().getName(), e);
        }
    }

    /**
     * 将对象转换为JSON字符串
     */
    public static <T> String toJSONString(T t, String defaultValue) {
        if (t == null) {
            return defaultValue;
        }
        try {
            return mapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Jackson序列化失败！type=" + t.getClass().getName(), e);
        }
    }


    /**
     * 解析JSON字符串为指定类型
     */
    public static <T> T parseObject(String json, Class<T> type) {
        Objects.requireNonNull(json);
        Objects.requireNonNull(type);
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Jackson反序列化失败！type=" + type.getName(), e);
        }
    }


    /**
     * 解析JSON字符串为指定类型，可以指定泛型以及多重嵌套泛型。
     */
    public static <T> T parseObject(String json, TypeReference<T> type) {
        Objects.requireNonNull(json);
        Objects.requireNonNull(type);
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Jackson反序列化失败！type=" + type.getType().getTypeName(), e);
        }
    }


}
