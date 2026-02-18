package com.sunbox.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by zhoubo on 17-5-6.
 */
public class MD5Util {

    private static final String HEX_CHARS = "0123456789abcdef";
    /** 日志 */
    private static Log logger = LogFactory.getLog(MD5Util.class);
    private MD5Util() {}

    /**
     * 返回 MessageDigest MD5
     */
    private static MessageDigest getDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回 MessageDigest MD5
     */
    private static MessageDigest getDigestBySha() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * MD5加密，并返回作为一个十六进制字节
     */
    public static byte[] md5(byte[] data) {
        return getDigest().digest(data);
    }

    /**
     * SHA-256加密，并返回作为一个十六进制字节
     */
    public static byte[] sha256(byte[] data) {
        return getDigestBySha().digest(data);
    }

    /**
     * MD5加密，并返回作为一个十六进制字节
     * <code>byte[]</code>.
     *
     * @param data
     *            Data to digest
     * @return MD5 digest
     */
    public static byte[] md5(String data) {
        byte[] bytes = null;
        try {
            bytes = md5(data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("MD5加密出错。",e);
        }
        return bytes;
    }
    public static byte[] md5GBK(String data) {
        byte[] bytes = null;
        try {
            bytes = md5(data.getBytes("GBK"));
        } catch (UnsupportedEncodingException e) {
            logger.error("MD5加密出错。",e);
        }
        return bytes;
    }
    /**
     * MD5加密，并返回一个32字符的十六进制值
     */
    public static String md5Hex(byte[] data) {
        return toHexString(md5(data));
    }

    /**
     * MD5加密，并返回一个32字符的十六进制值
     */
    public static String md5Hex(String data) {
        return toHexString(md5(data));
    }
    public static String md5HexGBK(String data) {
        return toHexString(md5GBK(data));
    }
    /**
     * SHA256加密
     */
    public static String sha256Hex(String data) {
        try {
            return toHexString(sha256(data.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            logger.error("MD5加密出错。",e);
            return null;
        }
    }

    private static String toHexString(byte[] b) {
        StringBuffer stringbuffer = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            stringbuffer.append(HEX_CHARS.charAt(b[i] >>> 4 & 0x0F));
            stringbuffer.append(HEX_CHARS.charAt(b[i] & 0x0F));
        }
        return stringbuffer.toString();
    }

    /**
     * Hmac-SHA256
     * 微信小程序中 部分sign 使用这种方式
     * @param data
     * @param key
     * @return
     */
    public static String hash_hmac(String data,String key){
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] array = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString().toUpperCase();
        }catch (Exception e){
            return "";
        }

    }
}
