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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * 业务校验工具类，校验不通过会抛出{@link VerifyException}
 *
 * @author Brozen
 * @since 2019-07-29
 */
public final class Verifies {

    /**
     * 校验表达式结果是否为true，不为true时抛出{@link VerifyException}
     *
     * @param expression boolean表达式结果
     */
    public static void verify(boolean expression) {
        verify(expression, "Verify Failed!");
    }


    /**
     * 校验表达式结果是否为true，不为true时抛出{@link VerifyException}
     *
     * @param expression boolean表达式结果
     * @param message 抛出异常的提示信息
     */
    public static void verify(boolean expression, String message) {
        if (!expression) {
            throw new VerifyException(message);
        }
    }


    /**
     * 校验引用不为null，否则抛出{@link VerifyException}
     * @param ref 引用
     */
    public static void notNull(@Nullable Object ref) {
        verify(ref != null, "Verify Failed! Reference is null!");
    }


    /**
     * 校验引用不为null，否则抛出{@link VerifyException}
     * @param ref 引用
     * @param message 抛出异常的提示信息
     */
    public static void notNull(@Nullable Object ref, String message) {
        verify(ref != null, message);
    }


    /**
     * 校验引用是null，否则抛出{@link VerifyException}
     * @param ref 引用
     */
    public static void isNull(@Nullable Object ref) {
        verify(ref == null, "Verify Failed! Reference is not null!");
    }


    /**
     * 校验引用是null，否则抛出{@link VerifyException}
     * @param ref 引用
     * @param message 抛出异常的提示信息
     */
    public static void isNull(@Nullable Object ref, String message) {
        verify(ref == null, message);
    }


    /**
     * 校验引用不为null，并返回入参引用，否则抛出{@link VerifyException}
     * @param ref 引用
     * @return 入参引用，如果入参引用不为null
     */
    @Nonnull
    public static <T> T requireNotNull(@Nullable T ref) {
        notNull(ref);
        return ref;
    }


    /**
     * 校验引用不为null，并返回入参引用，否则抛出{@link VerifyException}
     * @param ref 引用
     * @param message 抛出异常的提示信息
     * @return 入参引用，如果入参引用不为null
     */
    @Nonnull
    public static <T> T requireNotNull(@Nullable T ref, String message) {
        notNull(ref, message);
        return ref;
    }


    /**
     * 校验字符串不为null且不为空字符串，否则抛出{@link VerifyException}
     * @param str 待校验字符串
     * @see StringUtils#isNotBlank(CharSequence)
     */
    public static void notBlank(String str) {
        verify(StringUtils.isNotBlank(str), "Verify Failed! String is blank!");
    }


    /**
     * 校验字符串不为null且不为空字符串，否则抛出{@link VerifyException}
     * @param str 待校验字符串
     * @param message 抛出异常的提示信息
     * @see StringUtils#isNotBlank(CharSequence)
     */
    public static void notBlank(String str, String message) {
        verify(StringUtils.isNotBlank(str), message);
    }


    /**
     * 校验字符串不为null且不为空字符串，并返回入参字符串，否则抛出{@link VerifyException}
     * @param str 待校验字符串
     * @see StringUtils#isNotBlank(CharSequence)
     * @return 入参字符串
     * @since 1.0.3
     */
    public static String requireNotBlank(String str) {
        return requireNotBlank(str, "Verify Failed! String is blank!");
    }


    /**
     * 校验字符串不为null且不为空字符串，并返回入参字符串，否则抛出{@link VerifyException}
     * @param str 待校验字符串
     * @param message 抛出异常的提示信息
     * @see StringUtils#isNotBlank(CharSequence)
     * @return 入参字符串
     * @since 1.0.3
     */
    public static String requireNotBlank(String str, String message) {
        notBlank(str, message);
        return str;
    }


    /**
     * 校验字符串引用是null或是空字符串，否则抛出{@link VerifyException}
     * @param str 待校验字符串
     * @see StringUtils#isBlank(CharSequence)
     */
    public static void isBlank(String str) {
        verify(StringUtils.isNotBlank(str), "Verify Failed! String is not blank!");
    }


    /**
     * 校验字符串引用是null或是空字符串，否则抛出{@link VerifyException}
     * @param str 待校验字符串
     * @param message 抛出异常的提示信息
     * @see StringUtils#isBlank(CharSequence)
     */
    public static void isBlank(String str, String message) {
        verify(StringUtils.isBlank(str), message);
    }


    /**
     * 校验集合引用是null或集合为空，否则抛出{@link VerifyException}
     * @param collection 待校验集合
     * @param <C> 集合类型
     * @param <E> 集合中的元素类型
     * @see CollectionUtils#isNotEmpty(Collection)
     * @since 1.0.3
     */
    public static <C extends Collection<E>, E> void notEmpty(C collection) {
        verify(CollectionUtils.isNotEmpty(collection), "Verify Failed! Collection is empty!");
    }


    /**
     * 校验集合引用是null或集合为空，否则抛出{@link VerifyException}
     * @param collection 待校验集合
     * @param message 抛出异常的提示信息
     * @param <C> 集合类型
     * @param <E> 集合中的元素类型
     * @see CollectionUtils#isNotEmpty(Collection)
     * @since 1.0.3
     */
    public static <C extends Collection<E>, E> void notEmpty(C collection, String message) {
        verify(CollectionUtils.isNotEmpty(collection), message);
    }


    /**
     * 校验集合引用是null或集合为空，并返回集合，否则抛出{@link VerifyException}
     * @param collection 待校验集合
     * @param message 抛出异常的提示信息
     * @param <C> 集合类型
     * @param <E> 集合中的元素类型
     * @return 入参集合
     * @see CollectionUtils#isNotEmpty(Collection)
     * @since 1.0.3
     */
    public static <C extends Collection<E>, E> C requireNotEmpty(C collection, String message) {
        notEmpty(collection, message);
        return collection;
    }


    /**
     * 校验两个引用相等，否则抛出{@link VerifyException}
     * @param o1 待校验引用
     * @param o2 待校验引用
     * @see Objects#equals(Object, Object)
     */
    public static void equals(Object o1, Object o2) {
        equals(o1, o2, "Verify Failed! Objects is not equal!");
    }


    /**
     * 校验两个引用相等，否则抛出{@link VerifyException}
     * @param o1 待校验引用
     * @param o2 待校验引用
     * @param message 抛出异常的提示信息
     * @see Objects#equals(Object, Object)
     */
    public static void equals(Object o1, Object o2, String message) {
        verify(Objects.equals(o1, o2), message);
    }

}
