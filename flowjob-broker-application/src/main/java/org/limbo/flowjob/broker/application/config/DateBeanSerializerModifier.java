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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DateBeanSerializerModifier extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter bpw : beanProperties) {
            if (isLocalDateType(bpw)) {
                bpw.assignSerializer(new LocalDateConverter());
            } else if (isLocalDateTimeType(bpw)) {
                bpw.assignSerializer(new LocalDateTimeConverter());
            }
        }
        return beanProperties;
    }

    private boolean isLocalDateType(BeanPropertyWriter bpw) {
        Class<?> clazz = bpw.getType().getRawClass();
        return LocalDate.class.isAssignableFrom(clazz);
    }

    private boolean isLocalDateTimeType(BeanPropertyWriter bpw) {
        Class<?> clazz = bpw.getType().getRawClass();
        return LocalDateTime.class.isAssignableFrom(clazz);
    }

    public static class LocalDateConverter extends JsonSerializer<Object> {

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeNumber(((LocalDate) value).atStartOfDay().toInstant(TimeUtils.zoneOffset()).toEpochMilli());
        }
    }

    public static class LocalDateTimeConverter extends JsonSerializer<Object> {

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeNumber(((LocalDateTime) value).toInstant(TimeUtils.zoneOffset()).toEpochMilli());
        }
    }

}
