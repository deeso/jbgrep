package edu.rice.seclab.dso;

import java.nio.ByteBuffer;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;


public class Utils {
	private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	
	public static String unsigned_long_str (long val) {
		return UnsignedLong.valueOf(val).toString();
	}
	public static Long first64bits (byte [] x) {
		if (x.length < 8) return null;
		return bytesToLong(x);
		
	}
	public static Long first64bits (String binaryString) {
		byte [] x = unhexlify(binaryString);
		return first64bits(x);
	}
	
	public static long ComputeHash(String data)
	{
		return ComputeHash(unhexlify(data));
	}
	
	public static long ComputeHash(byte[] data)
	{
		long p = 16777619;
		long hash = 2166136261L;

		for (int i = 0; i < data.length; i++)
			hash = (hash ^ data[i]) * p;

		hash += hash << 13;
		hash ^= hash >> 7;
		hash += hash << 3;
		hash ^= hash >> 17;
		hash += hash << 5;
		return hash;
	}
	
	public static byte[] unhexlify(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public static long bytesToLong(byte[] bytes) {
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip 
        return buffer.getLong();
    }
	
	public static Long tryParseHexLongNumber (String value) {
		try {
			return UnsignedLong.valueOf(value, 16).longValue();
		}catch (Exception ex) {}
		return UnsignedLong.valueOf(value.replace("0x", ""), 16).longValue();		
	}
	
	public static Integer tryParseHexNumber (String value) {
		try {
			return UnsignedInteger.valueOf(value, 16).intValue();
		}catch (Exception ex) {}
		return UnsignedInteger.valueOf(value.replace("0x", ""), 16).intValue();		
	}
}
