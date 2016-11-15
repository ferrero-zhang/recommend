package com.ifeng.iRecommend.likun.ipush;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	public static String encode(String plainText) { 
		StringBuffer buf = new StringBuffer(""); 
		try { 
			MessageDigest md = MessageDigest.getInstance("MD5"); 
			md.update(plainText.getBytes()); 
			byte b[] = md.digest(); 
	
			int i; 
			for (int offset = 0; offset < b.length; offset++) { 
				i = b[offset]; 
				if(i<0) i+= 256; 
				if(i<16) 
				buf.append("0"); 
				buf.append(Integer.toHexString(i)); 
			} 
	
		} catch (NoSuchAlgorithmException e) { 
			e.printStackTrace(); 
		} 
		return buf.toString().toUpperCase();//32位的加密 
	} 

}
