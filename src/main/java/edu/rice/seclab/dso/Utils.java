package edu.rice.seclab.dso;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;


public class Utils {
	
	public static First64BitsKey FIRST_64_BITS_HASH = First64BitsKey.getInstance();
	
	public static IHashFunction DefaultHasher() {
		return FIRST_64_BITS_HASH;
	}
	
	public static String unsigned_long_str (long val) {
		return UnsignedLong.valueOf(val).toString();
	}
	public static Long first64bits (byte [] x) {
		if (x.length < 8) return null;
		return bytesToLong_be(x);
		
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
	
	public static long bytesToLong_le(byte[] bytes) {
		int len = Long.BYTES;
		byte[] tbytes = bytes;
		if (bytes.length != len)
			tbytes = new byte[len];
			for (int i = 0; i < len && i < bytes.length; i++)
				tbytes[i] = bytes[i];
		ByteBuffer byteBuffer = ByteBuffer.wrap(tbytes);
        //byteBuffer.flip();//need flip
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer.getLong();
    }
	
	public static long bytesToLong_be(byte[] bytes) {
		int len = Long.BYTES;
		byte[] tbytes = bytes;
		if (bytes.length != len)
			tbytes = new byte[len];
			for (int i = 0; i < len && i < bytes.length; i++)
				tbytes[i] = bytes[i];
		ByteBuffer byteBuffer = ByteBuffer.wrap(tbytes);
        //byteBuffer.flip();//need flip
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer.getLong();
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
	
	public static ArrayList<File> readDirectoryFilenames(String filename) {
		return readDirectoryFilenames(new File(filename));
	}
	
	public static ArrayList<File> readDirectoryFilenames(File file) {
		ArrayList<File> resultFiles = new ArrayList<File>();
		if (file.exists() && file.isFile()) {
			resultFiles.add(file);
			return resultFiles;
		} else if (file.exists() && file.isDirectory()) {
			File [] listOfFiles = file.listFiles();
			for (File f : listOfFiles) {
				resultFiles.addAll(readDirectoryFilenames(f));
			}
		}
		return resultFiles;
		
	}
}
