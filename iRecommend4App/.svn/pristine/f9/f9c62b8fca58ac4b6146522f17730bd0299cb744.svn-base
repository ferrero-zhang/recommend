package com.ifeng.commen.bloomfilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

import com.ifeng.commen.Utils.commenFuncs;

/**
 * bloomfilter<br>
 * 
 *<p>本bloomfilter设定错误率是0.0001,最佳的hash个数k=13，m取20亿，n预计为1亿;
 *可以支持对1亿项目的排重，误差率是0.0001；
 *需要的内存是256M，分成了两个库（新旧），并在适当情况下进行切换；
 *</p> 
 * 
 * 注意：这一版本的bloomfilter，需要两个库配合，所以多数情况下，肯定是有两个库，所以whichone=2；
 * 具体查询和写入，将按这个规则进行优化，减少计算量；
 * 
 * @algorithm None
 * @author likun 2009-7-10
 * @lastEdit likun 2013-8-10
 * @editDetail 扩大bloom过滤器大小；
 */
public class BloomFilter {
	
	private int defaultSize = 1 << 30;

	private int basic = (defaultSize - 1);

	private static final int[] hash_seeds = {3,5,7,11,13,17,19,23,299,31,37,39,411};
	
	private BitSet bits1,bits2;
	//private int whichone;
	
	private int urlsNum,gotNum;
	
	byte[] bts_write_cache = null;
	int write_cache_size = 1<<15;
	
	
	public BloomFilter() {
		bits1 = new BitSet(defaultSize);
		bits2 = new BitSet(defaultSize);
		bts_write_cache = new byte[write_cache_size];
	}

	/*
	 * 判断key是否存在；
	 */
	public boolean contains(String key) {
		if (key == null) {
			return true;
		}
		boolean res1 = true,res2 = true;
		for (int i = 0; i < hash_seeds.length; i++) {
			int pos = hashkey(key, hash_seeds[i]);
			if (res2 == true && !bits2.get(pos)) {
					res2 = false;
			}
			if (res1 == true && !bits1.get(pos)) {
				res1 = false;
			}
		}
		if(res1 == false && res2 == false)
			return false;
		return true;
		
	}

	/*
	 * 将key加入bf table
	 */
	public void add(String key) {
		if (key == null) {
			return;
		}
		BitSet bits = bits2;
		//int pos1 = HashAlgorithms.FNVHash1(key.getBytes());
		for(int i=0;i < hash_seeds.length;i++)
		{
			int pos = hashkey(key,hash_seeds[i]);
			bits.set(pos);
		}	
	}
	
	private int hashkey(String line,int seed) {
		int h = 0;
		for (int i = 0; i < line.length(); i++) {
			h = seed * h + line.charAt(i);
			//h *= 378551 ;
		}
		return check(h);
	}

	private int check(int h) {
		return basic & h;
	}
	
	public int read() throws IOException{
		File f = new File("bf/behavior_bf_table2.txt");
		if(f.exists())
		{
			FileInputStream in=new FileInputStream(f);
			InputStreamReader inReader=new InputStreamReader(in,"ISO-8859-1");
			BufferedReader buf=new BufferedReader(inReader);
			bits2.clear();
			int bIndex = 0;
			char[] cs = new char[64];
			int len = 0;
			while((len = buf.read(cs)) > 0)
			{
				byte a,b;
				for(int i=0;i < len;i++)
				{
					byte btTmp = (byte)cs[i];
					for(int j=0;j < 8;j++)
					{
						a = (byte) (btTmp>>1);
						b = (byte) (a<<1);
						if(btTmp != b)
							bits2.set(bIndex);
						bIndex++;
						btTmp = a;
					}
					
				}

			}
		
		}

		//System.out.println("whichone:"+whichone);
		if(bits1.length()-1 <= 0)
		{
			f = new File("bf/behavior_bf_table1.txt");
			if(f.exists())
			{
				FileInputStream in=new FileInputStream(f);
				InputStreamReader inReader=new InputStreamReader(in,"ISO-8859-1");
				BufferedReader buf=new BufferedReader(inReader);
				bits1.clear();
				int bIndex = 0;
				char[] cs = new char[64];
				int len = 0;
				while((len = buf.read(cs)) > 0)
				{
					byte a,b;
					for(int i=0;i < len;i++)
					{
						byte btTmp = (byte)cs[i];
						for(int j=0;j < 8;j++)
						{
							a = (byte) (btTmp>>1);
							b = (byte) (a<<1);
							if(btTmp != b)
								bits1.set(bIndex);
							bIndex++;
							btTmp = a;
						}
						
					}
				}
			
			}else
				System.out.println("Error:lib 1 do not exists!");
			
		}
		return 2;
	}
	
	//mem still wasted, need change
	public void writeback() throws UnsupportedEncodingException{
		if(bts_write_cache == null)
			bts_write_cache = new byte[write_cache_size];
		
		BitSet bits = null;
		String filename = "";
		bits  = bits2;
		filename = "behavior_bf_table2.txt";
		byte[] bts = new byte[8];
		Arrays.fill(bts, (byte)0);
		Arrays.fill(bts_write_cache, (byte)0);
		
		//先清空存档
		int res = commenFuncs.writeResult("bf/", filename, "", "ISO-8859-1", false, null);
		if(res < 0)
			System.out.println("error:write back behaviors");	
		
		int bIndex = 0;
		int tmpindex = 0; //for write cache
		int i = bits.nextSetBit(0);
		for (; i >= 0; i = bits.nextSetBit(i + 1)) {
			if(i > (bIndex+63))
			{
				//full
				if(tmpindex >= write_cache_size)
				{
					String content = new String(bts_write_cache,"ISO-8859-1");
					res = commenFuncs.writeResult("bf/", filename, content, "ISO-8859-1", true, null);
					if(res < 0)
						System.out.println("error:write back behaviors");
		
					Arrays.fill(bts_write_cache, (byte)0);
					tmpindex = 0;
				}
				System.arraycopy(bts, 0, bts_write_cache, tmpindex, 8);		
				bIndex += 64;
				tmpindex += 8;
				Arrays.fill(bts, (byte)0);
				int times = (i-bIndex)/64;
				while(times-- > 0)
				{
					bIndex += 64;
					//full
					if(tmpindex >= write_cache_size)
					{
						String content = new String(bts_write_cache,"ISO-8859-1");
						res = commenFuncs.writeResult("bf/", filename, content, "ISO-8859-1", true, null);
						if(res < 0)
							System.out.println("error:write back behaviors");
						Arrays.fill(bts_write_cache, (byte)0);
						tmpindex = 0;
					}
					tmpindex += 8;
				}
			}	
			//set bt
			int bIndexReal = i - bIndex;
			int b = bIndexReal/8;
			int b1 = bIndexReal%8;
			bts[b] = (byte) (bts[b]|(1<<b1));
		}
		//end treat
		{
			if(tmpindex >= write_cache_size)
			{
				String content = new String(bts_write_cache,"ISO-8859-1");
				res = commenFuncs.writeResult("bf/", filename, content, "ISO-8859-1", true, null);
				if(res < 0)
					System.out.println("error:write back behaviors");
				Arrays.fill(bts_write_cache, (byte)0);
				tmpindex = 0;
			}
			System.arraycopy(bts, 0, bts_write_cache, tmpindex, 8);		
			bIndex += 64;
			tmpindex += 8;
			i = defaultSize -1;
			if(i > bIndex)
			{
				int times = (i-bIndex)/64 +1;
				while(times-- > 0)
				{
					//full
					if(tmpindex >= write_cache_size)
					{
						String content = new String(bts_write_cache,"ISO-8859-1");
						res = commenFuncs.writeResult("bf/", filename, content, "ISO-8859-1", true, null);
						if(res < 0)
							System.out.println("error:write back behaviors");
						Arrays.fill(bts_write_cache, (byte)0);
						tmpindex = 0;
					}
					tmpindex += 8;
				}		
			}
			//to string
			if(tmpindex > 0)
			{
				String content = new String(bts_write_cache,"ISO-8859-1");
				res = commenFuncs.writeResult("bf/", filename, content, "ISO-8859-1", true, null);
				if(res < 0)
					System.out.println("error:write back behaviors");
				
			}
		}

	}
	
	public void checkBits(){
		System.out.println("bit1 size:"+bits1.size());
		System.out.println("max num:"+(bits1.length()-1));
		int i = 0;
		while(bits1.get(i) == false && ++i < bits1.size())
			;
		System.out.println("non zero num:"+bits1.cardinality());
		System.out.println("min num:"+i);
		System.out.println("urls num:"+urlsNum);
		System.out.println("--------");
		System.out.println("bit2 size:"+bits2.size());
		System.out.println("max num:"+(bits2.length()-1));
		
		i = 0;
		while(bits2.get(i) == false && ++i < bits2.size())
			;
		System.out.println("non zero num:"+bits2.cardinality());
		System.out.println("min num:"+i);
		System.out.println("urls num:"+urlsNum);
	}
	
	/*
	 * 检查bloomfilter的状态并进行动新库扩充
	 */
	public String checkBits2() {
		//...
		StringBuffer sbTmp = new StringBuffer();
		sbTmp.append("bit2 size:").append(bits2.size());
		sbTmp.append("max num:").append(bits2.length() - 1);
		int i = 0;
		while (bits2.get(i) == false && ++i < bits2.size())
			;
		sbTmp.append("non zero num:").append(bits2.cardinality());
		sbTmp.append("min num:").append(i);
		sbTmp.append("urls num:").append(urlsNum);
		sbTmp.append(";");

		sbTmp.append("bit1 size:").append(bits1.size());
		sbTmp.append("max num:").append(bits1.length() - 1);
		i = 0;
		while (bits1.get(i) == false && ++i < bits1.size())
			;
		sbTmp.append("non zero num:").append(bits1.cardinality());
		sbTmp.append("min num:").append(i);
		sbTmp.append("urls num:").append(urlsNum);

		return sbTmp.toString();
		
	}
	
	public static void main(String arg[]) {	
//		//test
//		int k = (int) ((Math.log(2)/Math.log(Math.E))*(Math.log(Math.E)/Math.log(2))*(Math.log(10000)/Math.log(2)));
//		System.out.println("k="+k);
//		
//		int n = 50000000;
//		float fn = n/(float)(8*1024*1024);
//		int m = (int) (fn * (Math.log(Math.E)/Math.log(2))*(Math.log(10000)/Math.log(2)));
//		System.out.println(m+" M");
//		
//		System.out.println("m/n="+(Math.log(Math.E)/Math.log(2))*(Math.log(10000)/Math.log(2)));
//		
		
		BloomFilter bf = new BloomFilter();
		try {
			bf.read();
			bf.add("d8e3d67c6839c218c393fcb140aaae074f549538");
			System.out.println(bf.contains("d8e3d67c6839c218c393fcb140aaae074f549538"));
//			bf.checkBits();
//			bf.test();
//			bf.writeback();		
			////////////////////////
			
//			bf.readurls();
//			bf.checkBits();
//			bf.writeback();			
//			bf.test();
//			Thread.sleep(600000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}