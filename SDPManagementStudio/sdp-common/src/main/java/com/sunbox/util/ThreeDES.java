package com.sunbox.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

public class ThreeDES {


    public static String decryption(String key, String src){
        String result = "";
        byte[]keybyte =  key.getBytes();
        byte[] newKey = new byte[24];
        System.arraycopy(keybyte, 0, newKey, 0, 16);
        System.arraycopy(keybyte, 0, newKey, 16, 8);
        byte[] data= getBase64Decode(src);
        byte[] str4 = new byte[0];
        try {
            str4 = ees3DecodeECB(newKey, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            result = new String(str4, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
    public static String encryption(String key, String src){
        String result = "";
        byte[]keybyte =  key.getBytes();
        byte[] newKey = new byte[24];
        System.arraycopy(keybyte, 0, newKey, 0, 16);
        System.arraycopy(keybyte, 0, newKey, 16, 8);
        try {
            byte[] data = src.getBytes("UTF-8");
            byte[] str4 = new byte[0];
            str4 = des3EncodeECB(newKey, data);
            result = getBase64Encode(str4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }




    /**
     * ECB,ҪIV
     * @param key Կ
     * @param data
     * @return Base64
     * @throws Exception
     */
    public static byte[] des3EncodeECB(byte[] key, byte[] data)
            throws Exception {
        Key deskey = null;
        DESedeKeySpec spec = new DESedeKeySpec(key);
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
        deskey = keyfactory.generateSecret(spec);
        Cipher cipher = Cipher.getInstance("desede" + "/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, deskey);
        byte[] bOut = cipher.doFinal(data);
        return bOut;
    }
    /**
     * ECB,ҪIV
     * @param key Կ
     * @param data Base64
     * @return
     * @throws Exception
     */
    public static byte[] ees3DecodeECB(byte[] key, byte[] data)
            throws Exception {
        Key deskey = null;
        DESedeKeySpec spec = new DESedeKeySpec(key);
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
        deskey = keyfactory.generateSecret(spec);
        Cipher cipher = Cipher.getInstance("desede" + "/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, deskey);
        byte[] bOut = cipher.doFinal(data);
        return bOut;
    }

    /*
	 * 根据字符串生成密钥字节数组
	 * @param keyStr 密钥字符串
	 * @return
	 * @throws UnsupportedEncodingException
	 */
    public static byte[] build3DesKey(String keyStr) throws UnsupportedEncodingException {
        byte[] key = new byte[24];    //声明一个24位的字节数组，默认里面都是0
        byte[] temp = keyStr.getBytes("UTF-8");    //将字符串转成字节数组

		/*
		 * 执行数组拷贝
		 * System.arraycopy(源数组，从源数组哪里开始拷贝，目标数组，拷贝多少位)
		 */
        if(key.length > temp.length){
            //如果temp不够24位，则拷贝temp数组整个长度的内容到key数组中
            System.arraycopy(temp, 0, key, 0, temp.length);
        }else{
            //如果temp大于24位，则拷贝temp数组24个长度的内容到key数组中
            System.arraycopy(temp, 0, key, 0, key.length);
        }
        return key;
    }
    public static String get3DESEncryptECB(String src,String secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(build3DesKey(secretKey), "DESede"));
            String base64Encode = getBase64Encode(cipher.doFinal(src.getBytes("UTF-8")));
            return filter(base64Encode);
        } catch (Exception ex) {
            //加密失败，打日志
//			//logger.error(ex,ex);
        }
        return null;
    }

    public static String get3DESDecryptECB(String src,String key){
        try {
            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(build3DesKey(key), "DESede"));
            byte[] base64DValue = getBase64Decode(src);
            byte ciphertext[] = cipher.doFinal(base64DValue);
            return new String(ciphertext, "UTF-8");
        } catch (Exception e) {
            //解密失败，打日志
//			//logger.error(e,e);
        }
        return null;
    }

    /**
     * 对字符串进行Base64编码
     *
     *
     * @return String 进行编码后的字符串
     */

    public static byte[] getBase64Decode(String encodedText) {
        org.apache.commons.codec.binary.Base64 base = new org.apache.commons.codec.binary.Base64();
        byte[] str=base.decode(encodedText);
        return str;
    }

    public static String getBase64Encode(byte[] textByte) {
        org.apache.commons.codec.binary.Base64 base = new org.apache.commons.codec.binary.Base64();
        String encodedText = base.encodeToString(textByte);
        return encodedText;
    }

    /**
     * 去掉字符串的换行符号 base64编码3-DES的数据时，得到的字符串有换行符号 ，一定要去掉，否则uni-wise平台解析票根不会成功，
     * 提示“sp验证失败”。在开发的过程中，因为这个问题让我束手无策， 一个朋友告诉我可以问联通要一段加密后 的文字，然后去和自己生成的字符串比较，
     * 这是个不错的调试方法。我最后比较发现我生成的字符串唯一不同的 是多了换行。 我用c#语言也写了票根请求程序，没有发现这个问题。
     *
     */

    private static String filter(String str) {
        String output = null;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            int asc = str.charAt(i);
            if (asc != 10 && asc != 13) {
                sb.append(str.subSequence(i, i + 1));
            }
        }
        output = new String(sb);
        return output;
    }

    /**
     * CBC
     * @param key Կ
     * @param keyiv IV
     * @param data
     * @return Base64
     * @throws Exception
     */
    public static byte[] des3EncodeCBC(byte[] key, byte[] keyiv, byte[] data)
            throws Exception {
        Key deskey = null;
        DESedeKeySpec spec = new DESedeKeySpec(key);
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
        deskey = keyfactory.generateSecret(spec);
        Cipher cipher = Cipher.getInstance("desede" + "/CBC/PKCS5Padding");
        IvParameterSpec ips = new IvParameterSpec(keyiv);
        cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);
        byte[] bOut = cipher.doFinal(data);
        return bOut;
    }
    /**
     * CBC
     * @param key Կ
     * @param keyiv IV
     * @param data Base64
     * @return
     * @throws Exception
     */
    public static byte[] des3DecodeCBC(byte[] key, byte[] keyiv, byte[] data)
            throws Exception {
        Key deskey = null;
        DESedeKeySpec spec = new DESedeKeySpec(key);
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
        deskey = keyfactory.generateSecret(spec);
        Cipher cipher = Cipher.getInstance("desede" + "/CBC/PKCS5Padding");
        IvParameterSpec ips = new IvParameterSpec(keyiv);
        cipher.init(Cipher.DECRYPT_MODE, deskey, ips);
        byte[] bOut = cipher.doFinal(data);
        return bOut;
    }

    public static void main1(String[] args){
        String SECRETKEY = "20200224@gdsy";
        String DESKEY = "P@9oo8F4=wNc+2P1";
        String ids = "c041f9c2421a4e8c9555078187db77dbs";
        String orderNo = "202003021633531528607";
        String timeStamp = "202002241230";
        String sign = EncryptionUtil.MD5("secretKey="+SECRETKEY+
                "&orderNo="+orderNo+
                "&timeStamp="+timeStamp+
                "&secretKey="+SECRETKEY).toUpperCase();
        System.out.println("md5="+sign);
        Map<String, String> par = new HashMap<>();
        par.put("orderNo",orderNo);
        par.put("timeStamp",timeStamp);
        par.put("sign",sign);
        String encryption = encryption(DESKEY, JSON.toJSONString(par));
        System.out.println(encryption);
        System.out.println(decryption(DESKEY,"ueG4F9e2mmSjFeO+z9D0BqRkj4uyjyRdffaZriH6q5gFkuBSuD/u2LF4PKJh3IIu4Z8ArrpjhvU/2wT5bU5dQtm8gjAL6908FoiK+FpqJOFe4oCCUoBiQw4PLJEn8X7eVTBa2sixdWyT6Mv4k8RQpg=="));
    }

    // 加解密
    public static void mainswm(String[] args) {
        String encryption1 = get3DESEncryptECB("1&1&1&1&114402225&1&1","delivery1234567890123456" );
        System.out.println(encryption1);
        System.out.println(get3DESDecryptECB(encryption1 ,"delivery1234567890123456"));
    }

    public static void main(String[] args) {





        /*List<Map<String ,Object>> list = new ArrayList<>();
        Map<String,Object> map1 = new HashMap<>();
        map1.put("productid","000ecdcf-188f-458a-9b59-848c2eebdf78");
        map1.put("price","19.99");
        map1.put("quantity","1");
        map1.put("hxbnumber","1");
        Map<String,Object> map2 = new HashMap<>();
        map2.put("productid","000f3894-0122-48e0-a74f-f867f96ab379");
        map2.put("price","10.00");
        map2.put("quantity","2");
        map2.put("hxbnumber","2");
        list.add(map1);
        list.add(map2);
        String Productinfo= JSON.toJSONString(list);
        String receiveMobile= "18954111023";
        String totalAmount= "59.99";
        String outOrderId= "FT_0001";
        String goodsTag = "123456";
        String mchid = "0045685790";
        String deliveryType = "0";
        String openid = "1234";
        long timestamp = Long.valueOf("1590112260470").longValue();
        timestamp = System.currentTimeMillis();
        String sign = EncryptionUtil.MD5("secretKey="+SECRETKEY
                +"&Productinfo="+Productinfo
                +"&receiveMobile="+receiveMobile
                +"&totalAmount="+totalAmount
                +"&outOrderId="+outOrderId
                +"&goodsTag="+goodsTag
                +"&mchid="+mchid
                +"&deliveryType="+deliveryType
                +"&openid="+openid
                +"&timestamp="+timestamp
                +"&secretKey=" +SECRETKEY).toUpperCase();
        System.out.println("==========>"+sign);
        Map<String, Object> par = new HashMap<>();
        par.put("Productinfo",Productinfo);
        par.put("receiveMobile",receiveMobile);
        par.put("totalAmount",totalAmount);
        par.put("outOrderId",outOrderId);
        par.put("goodsTag", goodsTag);
        par.put("mchid", mchid);
        par.put("deliveryType", deliveryType);
        par.put("openid", openid);
        par.put("timestamp", timestamp);
        par.put("sign",sign);
        String desStr = encryption(DESKEY, JSON.toJSONString(par));
        JSONObject data = new JSONObject();
        data.put("data",desStr);
        System.out.println(data);
        String desStr2 = decryption(DESKEY, data.get("data").toString());
        System.out.println(">>>:"+desStr2);*/

    }
}