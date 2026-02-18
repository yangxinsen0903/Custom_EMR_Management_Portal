package com.sunbox.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class AESUtils {
	/**
	 * 加密
	 *
	 * @param content
	 *            需要加密的内容
	 * @param password
	 *            加密密码
	 * @return
	 */
	// private static String Key = "1234567890abcdef";

	// private static byte[] _key1 = { 0x12, 0x34, 0x56, 0x78, (byte) 0x90,
	// (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12, 0x34, 0x56, 0x78,
	// (byte) 0x90, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF };

	private static String _key1 = "L%n67}G/Mk@k%:~Y";

//	private static String key_paddiing_type = "AES/CBC/PKCS7Padding";
	private static String key_paddiing_type = "AES/CBC/PKCS5Padding";

	public static String encode(String stringToEncode, String key)
			throws NullPointerException {

		try {
			SecretKeySpec skeySpec = getKey(key);
			byte[] clearText = stringToEncode.getBytes("UTF8");
			IvParameterSpec ivParameterSpec = new IvParameterSpec(
					_key1.getBytes());
			byte[] iv = ivParameterSpec.getIV();
//			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			Cipher cipher = Cipher.getInstance(key_paddiing_type);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
			// String encrypedValue = Base64.encodeToString(
			// cipher.doFinal(clearText), Base64.DEFAULT);

			String encrypedValue = Base64Util.encode(cipher.doFinal(clearText));

			return encrypedValue;

		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * AES解密
	 *
	 * @param encryptBytes
	 *            待解密的byte[]
	 * @param key
	 *            解密密钥
	 * @return 解密后的String
	 * @throws Exception
	 *             08-10 09:24:04.796: I/(19189):
	 *             =========encode=====GzO419AM8euZzQf+Ot7Ecw==
	 */
	public static String aesDecryptByBytes(String encryptBytes, String key)
			throws Exception {
		SecretKeySpec skeySpec = getKey(key);
		byte[] bytes = Base64Util.decode(encryptBytes);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(_key1.getBytes());
//		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		Cipher cipher = Cipher.getInstance(key_paddiing_type);
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
		String decryptString = new String(cipher.doFinal(bytes), "UTF-8")
				.trim();
		return decryptString;
	}

	private static SecretKeySpec getKey(String key) {
		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
		return skeySpec;
	}

	public static String getRandomString(int length) { //length表示生成字符串的长度
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {

		/*String content = "R+mzSChFJ3S5vvgGWOVnUa6F00FQhvs/S29yZCa++FHX4OBb97bAw2h1nQ4R5P37wzP7G985jesb2VW3UlG/M7/bYmHxwzBj0Gsq/3Q5pd+N5heLMV9e++O8G9Rhl+MXBfEwJRtZvSqcNHwzt3frVYoGwVkIqSYA8ZK1Wsjh0OKBsdJ54AsCFAk6xgYN5nVB+t6oJj98uthXTZySzOOPyJfg8R+/Z4gU/szlTHcWBd9kkdzF5A+IC0FYEw3ogLNbISDWGGzI+YZnK0qMlwZG2G93PEMDV0Wwj/COgKxe7SxFMiyohkgHTJt+8Tlz1eHaPB0JemM031NoaVklRc6REMV/4BwB8iGgTGTjBxt/uXh5Nryq8+7jWPEM4Ke0O+yNIrcCayl0u3yE9Ii5aDCv+3Uba3ZpDx1JcPgHWu42OF7CIkf5rxRAn2JDw8bT2HknvbGkW9S+lsj5J1d/BcyXTapA7/6jt+ACx7rZcbqt3NUyr2P+Zb/cvhm7UTfg9Nuu";
		String key = "1234567890123456";
//加密
		System.out.println("加密key：" + key);
//		System.out.println("加密前：" + content);
//		String encode = encode(content, key);
//		String s1 = Base64.encode(encode);
//		String encryptResultStr = parseByte2HexStr(encryptResult);
//		System.out.println("加密后：" + encode);
//解密
//		byte[] decryptFrom = parseHexStr2Byte(encryptResultStr);
		String s = aesDecryptByBytes(content, key);
		System.out.println("解密后：" + s);*/
		/*String key = AESUtils.getRandomString(16);
		System.out.println(new String(key));

		Date date = new Date();
		date.setTime(1479368204485l);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(sdf.format(date));*/

//		System.out.println(AESUtils.aesDecryptByBytes("l9wwbboDGpHY7OUcPizQpA==", "UQPeKvEOAbnWP4FJ"));
//		System.out.println(AESUtils.encode("signtype","UQPeKvEOAbnWP4FJ"));
//		System.out.println(AESUtils.aesDecryptByBytes("1", "1koncmzidcussamb"));
		//2018-05-01
//		System.out.println(URLEncoder.encode(DESUtil.encrypt("b26644e6c459492caaccea6b706aaba0", "caB2dfD4E5F60708"), "UTF-8"));  fail
//		System.out.println(URLEncoder.encode(DESUtil.encrypt("43625edfc79046de8ca36f1440bc53c2", "caB2dfD4E5F60708"), "UTF-8")); fail
//		System.out.println(URLEncoder.encode(DESUtil.encrypt("e1816b7829c4425f8acf590fd3503a66", "caB2dfD4E5F60708"), "UTF-8")); not
//		System.out.println(URLEncoder.encode(DESUtil.encrypt("afa002d7fae04d088e2360895d1b0085", "caB2dfD4E5F60708"), "UTF-8")); not
//		System.out.println(URLEncoder.encode(DESUtil.encrypt("1b705cf71509454aa161cce95199d99d", "caB2dfD4E5F60708"), "UTF-8")); not
//		System.out.println(URLEncoder.encode(DESUtil.encrypt("87f5ddf13ca748f981a1758cab851e50", "caB2dfD4E5F60708"), "UTF-8"));
//		System.out.println(URLEncoder.encode(DESUtil.encrypt("65e8fdc99b354a49a763b576944cbaf7", "caB2dfD4E5F60708"), "UTF-8")); fail
//		System.out.println(URLEncoder.encode(DESUtil.encrypt("e1816b7829c4425f8acf590fd3503a66", "caB2dfD4E5F60708"), "UTF-8")); not
//		System.out.println(URLEncoder.encode(DESUtil.encrypt("4ca903fe2738415dad0d4a4be005cc2d", "caB2dfD4E5F60708"), "UTF-8")); not 2018-04-28
//		System.out.println(URLEncoder.encode(DESUtil.encrypt("44aa42f2d4124286b162158025d66282", "caB2dfD4E5F60708"), "UTF-8")); fail








	}
}