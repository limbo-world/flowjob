/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.limbo.utils.strings.UUIDUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Brozen
 * @since 2021-07-06
 */
public class Symbol implements Serializable {

    private static final long serialVersionUID = -8199733477148226173L;

    private final String symbol;

    private Symbol() {
        symbol = UUIDUtils.randomID();
    }

    @JsonCreator
    public Symbol(String symbol) {
        this.symbol = Objects.requireNonNull(symbol);
    }

    @JsonValue
    public String getSymbol() {
        return this.symbol;
    }

    public static Symbol newSymbol() {
        return new Symbol();
    }

}
