package com.ifeng.iRecommend.kedm.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StringCompress {
	public static String compress(String str){
		if(str == null || str.length() == 0){
			return str;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = null;
		GZIPOutputStream gzip = null;
		try{
			//in = new ByteArrayInputStream(str.getBytes());
			gzip = new GZIPOutputStream(out);
			gzip.write(str.getBytes());
			/*byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = in.read(buffer)) != -1) {
				gzip.write(buffer, 0, offset);
			}*/
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(gzip != null){
					gzip.finish();
					gzip.flush();
					gzip.close();
				}
				if(out != null){
					out.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return new sun.misc.BASE64Encoder().encode(out.toByteArray());
		
	}
	public static String uncompress(String str){
		if(str == null || str.length() == 0){
			return str;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = null;
		GZIPInputStream ginzip = null; 
		byte[] compress = null;
		String decompress = null;
		try{
			compress = new sun.misc.BASE64Decoder().decodeBuffer(str);
			in = new ByteArrayInputStream(compress);
			ginzip = new GZIPInputStream(in);
			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = ginzip.read(buffer)) != -1) {
			out.write(buffer, 0, offset);
			}
			decompress = out.toString();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(ginzip != null){
					ginzip.close();
				}
				if(in != null){
					in.close();
				}
				if(out != null){
					out.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return decompress;
	}
	public static void main(String[] args){
		String str = "{\"12335\":\"sy12232\"}";
		System.out.println(uncompress(compress(str)));
	}

}
