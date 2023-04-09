package org.limbo.flowjob.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * @author Brozen
 * @since 2022-12-14
 */
public final class MD5Utils {


    /**
     * 对入参进行MD5加密，并返回签名二进制数据。
     * @param origin 原始字符串
     * @return 签名二进制数据
     */
    public static byte[] bytes(String origin) {
        return bytes(origin.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * 对入参进行MD5加密，并返回签名二进制数据。
     * @param origin 原始字符串
     * @param charset 原始字符串字符集
     * @return 签名二进制数据
     */
    public static byte[] bytes(String origin, Charset charset) {
        Objects.requireNonNull(charset, "charset");
        return bytes(origin.getBytes(charset));
    }


    /**
     * 对入参进行MD5加密，并返回签名二进制数据。
     * @param origin 原始字符串
     * @param charset 原始字符串字符集
     * @return 签名二进制数据
     */
    public static byte[] bytes(String origin, String charset) {
        try {
            charset = StringUtils.isBlank(charset) ? "UTF-8" : charset;
            return bytes(origin.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("字符串编码异常", e);
        }
    }

    /**
     * 计算 MD5 签名，返回签名二进制数据。
     * @param bytes	原始数据
     * @return 签名二进制数据
     */
    public static byte[] bytes(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("MD5 加密失败", e);
        }
    }

}
