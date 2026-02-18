package com.sunbox.util;

import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import javax.crypto.Cipher;



public class RSAUtils {

    /**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";

    /**
     * 签名算法
     */
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    /**
     * 获取公钥的key
     */
    public static final String PUBLIC_KEY = "RSAPublicKey";

    /**
     * 获取私钥的key
     */
    public static final String PRIVATE_KEY = "RSAPrivateKey";

    private static String RSA_KEY = "RSA/ECB/PKCS1Padding";

    /**
     * 生成公钥和私钥
     * @throws NoSuchAlgorithmException
     *
     */
    public static HashMap<String, Object> getKeys() throws NoSuchAlgorithmException{
        HashMap<String, Object> map = new HashMap<String, Object>();
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(1024);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        map.put("public", publicKey);
        map.put("private", privateKey);
        return map;
    }
    /**
     * 使用模和指数生成RSA公钥
     * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA
     * /None/NoPadding】
     *
     * @param modulus
     *            模
     * @param exponent
     *            指数
     * @return
     */
    public static RSAPublicKey getPublicKey(String modulus, String exponent) {
        try {
            BigInteger b1 = new BigInteger(modulus);
            BigInteger b2 = new BigInteger(exponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** */
    /**
     * <p>
     * 获取公钥
     * </p>
     *
     * @param keyMap
     *            密钥对
     * @return
     * @throws Exception
     */
    public static String getPublicKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);

        return Base64Util.encode(key.getEncoded());
    }

    /** */
    /**
     * <p>
     * 获取私钥
     * </p>
     *
     * @param keyMap
     *            密钥对
     * @return
     * @throws Exception
     */
    public static String getPrivateKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return Base64Util.encode(key.getEncoded());
    }

    /**
     * 使用模和指数生成RSA私钥
     * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA
     * /None/NoPadding】
     *
     * @param modulus
     *            模
     * @param exponent
     *            指数
     * @return
     */
    public static RSAPrivateKey getPrivateKey(String modulus, String exponent) {
        try {
            BigInteger b1 = new BigInteger(modulus);
            BigInteger b2 = new BigInteger(exponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 公钥加密
     *
     * @param data
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static String encryptByPublicKey(String data, RSAPublicKey publicKey)
            throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        // 模长
        int key_len = publicKey.getModulus().bitLength() / 8;
        // 加密数据长度 <= 模长-11
        String[] datas = splitString(data, key_len - 11);
        String mi = "";
        //如果明文长度大于模长-11则要分组加密
        for (String s : datas) {
            mi += bcd2Str(cipher.doFinal(s.getBytes("UTF-8")));
        }
        return mi;
    }

    /**
     * 私钥解密
     *
     * @param data
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String decryptByPrivateKey(String data, RSAPrivateKey privateKey)
            throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        //模长
        int key_len = privateKey.getModulus().bitLength() / 8;
        byte[] bytes = data.getBytes("UTF-8");
        byte[] bcd = ASCII_To_BCD(bytes, bytes.length);
        System.err.println(bcd.length);
        //如果密文长度大于模长则要分组解密
        String ming = "";
        byte[][] arrays = splitArray(bcd, key_len);
        for(byte[] arr : arrays){
            ming += new String(cipher.doFinal(arr));
        }
        return ming;
    }
    /**
     * ASCII码转BCD码
     *
     */
    public static byte[] ASCII_To_BCD(byte[] ascii, int asc_len) {
        byte[] bcd = new byte[asc_len / 2];
        int j = 0;
        for (int i = 0; i < (asc_len + 1) / 2; i++) {
            bcd[i] = asc_to_bcd(ascii[j++]);
            bcd[i] = (byte) (((j >= asc_len) ? 0x00 : asc_to_bcd(ascii[j++])) + (bcd[i] << 4));
        }
        return bcd;
    }
    public static byte asc_to_bcd(byte asc) {
        byte bcd;

        if ((asc >= '0') && (asc <= '9'))
            bcd = (byte) (asc - '0');
        else if ((asc >= 'A') && (asc <= 'F'))
            bcd = (byte) (asc - 'A' + 10);
        else if ((asc >= 'a') && (asc <= 'f'))
            bcd = (byte) (asc - 'a' + 10);
        else
            bcd = (byte) (asc - 48);
        return bcd;
    }
    /**
     * BCD转字符串
     */
    public static String bcd2Str(byte[] bytes) {
        char temp[] = new char[bytes.length * 2], val;

        for (int i = 0; i < bytes.length; i++) {
            val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);
            temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');

            val = (char) (bytes[i] & 0x0f);
            temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');
        }
        return new String(temp);
    }
    /**
     * 拆分字符串
     */
    public static String[] splitString(String string, int len) {
        int x = string.length() / len;
        int y = string.length() % len;
        int z = 0;
        if (y != 0) {
            z = 1;
        }
        String[] strings = new String[x + z];
        String str = "";
        for (int i=0; i<x+z; i++) {
            if (i==x+z-1 && y!=0) {
                str = string.substring(i*len, i*len+y);
            }else{
                str = string.substring(i*len, i*len+len);
            }
            strings[i] = str;
        }
        return strings;
    }
    /**
     *拆分数组
     */
    public static byte[][] splitArray(byte[] data,int len){
        int x = data.length / len;
        int y = data.length % len;
        int z = 0;
        if(y!=0){
            z = 1;
        }
        byte[][] arrays = new byte[x+z][];
        byte[] arr;
        for(int i=0; i<x+z; i++){
            arr = new byte[len];
            if(i==x+z-1 && y!=0){
                System.arraycopy(data, i*len, arr, 0, y);
            }else{
                System.arraycopy(data, i*len, arr, 0, len);
            }
            arrays[i] = arr;
        }
        return arrays;
    }

    public static String decryptByPrivateKey(String request, String privateKey)
            throws Exception {
        byte[] encryptedData = Base64Util.decode(request);
        byte[] keyBytes = Base64Util.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache = null;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return new String (decryptedData,"UTF-8");
    }

    /** */
    /**
     * <P>
     * 私钥解密
     * </p>
     *
     * @param encryptedData
     *            已加密数据
     * @param privateKey
     *            私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] encryptedData,
                                             String privateKey) throws Exception {
        byte[] keyBytes = Base64Util.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(RSA_KEY);
        cipher.init(Cipher.DECRYPT_MODE, privateK);
        // int inputLen = encryptedData.length;
        // ByteArrayOutputStream out = new ByteArrayOutputStream();
        // int offSet = 0;
        // byte[] cache;
        // int i = 0;
        // // 对数据分段解密
        // while (inputLen - offSet > 0) {
        // if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
        // cache = cipher
        // .doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
        // } else {
        // cache = cipher
        // .doFinal(encryptedData, offSet, inputLen - offSet);
        // }
        // out.write(cache, 0, cache.length);
        // i++;
        // offSet = i * MAX_DECRYPT_BLOCK;
        // }
        // byte[] decryptedData = out.toByteArray();
        // out.close();
        byte[] decryptedData = cipher.doFinal(encryptedData);
        // String s = new String(deBytes );
        return decryptedData;
    }

    public static String encryptByPublicKey(String request, String publicKey)
            throws Exception {
        byte[] data = request.getBytes("UTF-8");
        byte[] keyBytes = Base64Util.decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return Base64Util.encode(encryptedData);
    }
    /**
     * 验签并解密
     * <p>
     * 对于<b>合作伙伴</b>，publicKey是指保险公司的公钥，privateKey是指合作伙伴的私钥<br>
     * 对于<b>保险公司</b>，publicKey是指合作伙伴的公钥，privateKey是指保险公司自己的私钥<br>
     *
     * @param request 原始报文(JSON字符串)
     * @param publicKey  公钥
     * @param privateKey 私钥
     * @param isCheckSign 是否验签
     * @param isDecrypt 是否解密
     * @return 解密后的明文，验签失败则异常抛出
     * @throws Exception
     */
    public static String checkSignAndDecrypt(String request,String publicKey, String privateKey,
                                             boolean isCheckSign, boolean isDecrypt) throws Exception {
        boolean verifyResult = false;
        JSONObject requestJSONObject = JSONObject.parseObject(request);
        String sign = requestJSONObject.getString("signContent");//获取的签文
        String bizContent = requestJSONObject.getString("bizContent");//获取的业务数据密文
        requestJSONObject.remove("signContent");//要排序先去掉signContent（签文）
        String signContent = getSignContent(requestJSONObject);//所有key排序后放入LingkedMap后map.toString()
        System.out.println("签名字符串为：" + signContent);
        if(isCheckSign){
            verifyResult = verify(signContent, sign, publicKey);
        }
        if(isDecrypt && verifyResult){
            return decryptByPrivateKey(bizContent, privateKey);
        }
        return null;
    }

    /**
     * 加密并签名
     * <p>
     * 对于<b>合作伙伴</b>，publicKey是指保险公司的公钥，privateKey是指合作伙伴的私钥<br>
     * 对于<b>保险公司</b>，publicKey是指合作伙伴的公钥，privateKey是指保险公司自己的私钥<br>
     *
     * @param publicKey  公钥
     * @param privateKey  私钥
     * @param responseContent 返回报文原文
     * @param isCheckSign  是否签名
     * @param isEncrypt  是否加密
     * @return  加密加签后的返回报文
     * @throws Exception
     */
    public static JSONObject encryptAndSign(String publicKey, String privateKey,String responseContent,
                                            boolean isCheckSign, boolean isEncrypt) throws Exception{
        JSONObject responseJSONObject = JSONObject.parseObject(responseContent);
        String bizContent = responseJSONObject.getString("bizContent");
        responseJSONObject.remove("signContent");//去掉signContent,之后加签添加
        if(isEncrypt){
            String encryptBizContent = encryptByPublicKey(bizContent, publicKey);
            System.out.println("加密后业务数据：" + encryptBizContent);
            responseJSONObject.put("bizContent", encryptBizContent);
            if(isCheckSign){
                String signContent = getSignContent(responseJSONObject);//所有key排序后放入LingkedMap后map.toString()
                System.out.println("待签名字符串为：" + signContent);
                String sign = sign(signContent, privateKey);
                responseJSONObject.put("signContent", sign);
            }
        } else if(isCheckSign){//只加签、不加密
            String signContent = getSignContent(responseJSONObject);//所有key排序后放入LingkedMap后map.toString()
            System.out.println("待签名字符串为：" + signContent);
            String sign = sign(signContent, privateKey);
            responseJSONObject.put("signContent", sign);
        }
        return responseJSONObject;
    }
    /**
     * 封装待验签的内容
     * @param sortedParam
     * @return
     */
    public static String getSignContent(Map<String, Object> sortedParam) {
        LinkedHashMap map = new LinkedHashMap();
        List<String> keys = new ArrayList<String>(sortedParam.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = sortedParam.get(key).toString();
            map.put(key, value);
        }
        System.out.println("map = "+map.toString());
        return map.toString();
    }

    /**
     * <p>
     * 校验数字签名
     * </p>
     * @param data 已加密数据
     * @param publicKey 公钥(BASE64编码)
     * @param sign 数字签名
     *
     * @return
     * @throws Exception
     *
     */
    public static boolean verify(String request, String sign, String publicKey) {
        try {
            byte[] data = request.getBytes("UTF-8");
            byte[] keyBytes = Base64Util.decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey publicK = keyFactory.generatePublic(keySpec);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicK);
            signature.update(data);
            return signature.verify(Base64Util.decode(sign));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * <p>
     * 用私钥对信息生成数字签名
     * </p>
     *
     * @param request 已加密数据
     * @param privateKey 私钥(BASE64编码)
     *
     * @return
     * @throws Exception
     */
    public static String sign(String request, String privateKey) throws Exception {
        byte[] data = request.getBytes("UTF-8");
        byte[] keyBytes = Base64Util.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateK);
        signature.update(data);
        return Base64Util.encode(signature.sign());
    }

    /** */
    /**
     * <p>
     * 生成密钥对(公钥和私钥)
     * </p>
     * @return
     * @throws Exception
     */
    public static Map<String, Object> genKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator
                .getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(2048);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Map<String, Object> keyMap = new HashMap<String, Object>(2);
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    /**
     * 获取已生成的公钥
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static String genPubKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return Base64Util.encode(key.getEncoded());
    }

    /**
     * 获取已生成的私钥
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static String genPrivateKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return Base64Util.encode(key.getEncoded());
    }

    public static void main(String[] args) throws Exception {
    	/*HashMap<String, Object> map = RSAUtils.getKeys();
        //生成公钥和私钥
        RSAPublicKey publicKey = (RSAPublicKey) map.get("public");
        RSAPrivateKey privateKey = (RSAPrivateKey) map.get("private");
        System.out.println("publicKey:"+Base64Util.encode(publicKey.getEncoded()));
        System.out.println("privateKey:"+Base64Util.encode(privateKey.getEncoded()));
        */
        String pubKey="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCayO6iG9/WLk4OF9Po/iYdLGG5TolsuCdc7RZKtTWOEznnpfy2hlIq/Aa4CWBfH3U5QGoO7ieZlUimkcZfWJPajWKTLuMWu5MK8Ox37uXNXMwuf6LwMTMmN+dkq4qzc5Hiq6k8iPG0SAsC4qO/GVkYZuzTXawN+Wt65v8lm0dk+wIDAQAB";
        String priKey="MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJrI7qIb39YuTg4X0+j+Jh0sYblOiWy4J1ztFkq1NY4TOeel/LaGUir8BrgJYF8fdTlAag7uJ5mVSKaRxl9Yk9qNYpMu4xa7kwrw7Hfu5c1czC5/ovAxMyY352SrirNzkeKrqTyI8bRICwLio78ZWRhm7NNdrA35a3rm/yWbR2T7AgMBAAECgYAV/tGyZaWkSQY8iHRLmroN0T292IQ819p4IRTW3vNSUjcbTQIhaesJQrEtu99fBRhiX13y6zLjZ0xgpzx6VhsEbgZOEOU2B6GSwKHRUAXPd0/ZSh7+U2EpzwfuAzjDRKvXwa2gqZzX158s2nhRcY41SB3dYUD2DSmpqgKzXuC6mQJBANGqtLGm/UaIlWOuvCXH8i3m+hlFn2paRV+d5q3kU6Oo1WsGdTfwYfC03YxINpaLPd5GZmdWIZavbDiRsRAUutUCQQC8/XCcwaPyyLI9l2cD/ldKSuxFXt6+j0lZw9CI1IOQDTb+PZyXvx3vdQehzwO+xBmHRYrr4XJHTxkNsUlJ2uiPAkEAjfIlLRTh6eu0P+1fgbCRPBRhA9UH7Ue6KdbMArRPD8DQ5AlWOI3ssLqyNzP4iQkx1JcS4ykDynFvMgkot7thFQJBAIj8LvcAiKR/AmsQZfQIFB9ehTmXFV8Mnv84ptu3JhFw8q5/3BRFzB34Nur7EVzh6RsatiJZjCrR03gQS+4DhVMCQC0/AlWHcDm+sGD2AYEWpLhx/sKpYUtBCXTeok9xSkEwYMPPlmPWMrUxRxrrhczwz8nQLvPwJuIwsJSaj9CGF6s=";
        Map<String, String> map = new HashMap<String, String>();
        map.put("plateNo", "粤B12345");
        map.put("brand", "宝马750Li");
        map.put("frameNo", "4512546421685452");
        map.put("registrationDate", "2017-04-25 00:00:00");
        map.put("commercialEndtime", "2017-04-25 00:00:00");
        map.put("otherUserId", "o5x_uAfVZVQu5PXKyQl1T5MXVyE");
        map.put("driver", "张三");
        map.put("idcard", "450923199005048315");
        map.put("rank", "Teacher");
        map.put("reqSource", "WxMenu");
        map.put("reqSourceMsg", "微信菜单");
        map.put("tellphone", "15118835548");
        map.put("address", "广东省深圳市");
        String mi = RSAUtils.encryptByPublicKey(JSONObject.toJSONString(map), pubKey);
        System.out.println(mi);
        String ming = RSAUtils.decryptByPrivateKey(mi, priKey);
        System.out.println("解密后为："+ming);

    }
}
