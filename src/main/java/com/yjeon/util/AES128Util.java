package com.yjeon.util;

import java.io.UnsupportedEncodingException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;


public class AES128Util {
	private String iv;
	private Key keySpec;
	private String aes128key;
	
	public AES128Util() throws UnsupportedEncodingException{
		//init
		this.aes128key = "0987654321012345";
		this.iv = aes128key.substring(0, 16);
		byte[] keyBytes = new byte[16];
		byte[] b = aes128key.getBytes("UTF-8");
		
		int leng = b.length;
		if(leng > keyBytes.length) {
			leng = keyBytes.length;
		}
		
		System.arraycopy(b, 0, keyBytes, 0, leng);
		this.keySpec = new SecretKeySpec(keyBytes, "AES");
	}
	
	public String encrypt(String str) throws Exception{
		Cipher c= Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
		byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
		String encStr = new String(Base64.encodeBase64(encrypted));
		return encStr;
	}
	
	public String decrypt(String str) throws Exception{
		Cipher c= Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
		byte[] byteStr = Base64.decodeBase64(str.getBytes());
		String decStr = new String(c.doFinal(byteStr), "UTF-8");
		return decStr;
		
	}
}
