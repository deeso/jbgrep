package edu.rice.seclab.dso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

public class BinaryStringInfo {
	Long myHash;
	String myKeyValue;
	byte[] myKeyBytes;
	
	
	HashMap<String, HashSet<String>> myLocations = new HashMap<String, HashSet<String>>();
	private int myKeyLen;
	IHashFunction myHashFn = null;
	
	public BinaryStringInfo(String binaryString, IHashFunction hashFn) {
		myKeyValue = binaryString;
		myKeyBytes = getKeyBytes();
		myKeyLen = myKeyBytes.length;
		myHash = myHashFn.executeHash(myKeyBytes);
	}
	
//	public BinaryStringInfo(String binaryString, byte[] keyBytes, long hash) {
//		myKeyValue = binaryString;
//		myKeyBytes = keyBytes;
//		myHash = hash;
//		myKeyLen = myKeyBytes.length;
//	}
	
	public boolean matchAdd(byte[] bytes, String filename, long offset) {
		return matchAdd(bytes, 0, filename, offset);
	}

	public boolean matchAdd(byte[] bytes, int bufOff, String filename, long offset) {
		if (myHashFn.match(bytes, bufOff, myKeyBytes)) {
			addFileOffset(filename, offset);
			return true;
		}
		return false;
	}
	
	public Long getHash() {
		return myHash;
	}
	
	public String getKey(){
		return myKeyValue;
	}

	public int getKeyLen(){
		return myKeyLen;
	}
	
	public byte[] getKeyBytes(){
		return myKeyBytes;
	}
	
	public void addFileOffset(String filename, Long fileoffset) {
		synchronized (myLocations) {
			if (!myLocations.containsKey(filename)) {
				myLocations.put(filename, new HashSet<String>());
			}
			myLocations.get(filename).add(Utils.unsigned_long_str(fileoffset.longValue()));			
		}
	}
	
	public ArrayList<String> grepableFormatList() {
		ArrayList<String> results = new ArrayList<String>();
		synchronized (myLocations) {
			for (String filename : myLocations.keySet()) {
				for (String location : myLocations.get(filename)) {
					results.add(String.format("%s: %s %s", filename, location, myKeyValue));
				}
			}
		}
		return results;
	}
	
	public ArrayList<String> accumulateFileLocations() {
		ArrayList<String> results = new ArrayList<String>();
		synchronized (myLocations) {
			for (String filename : myLocations.keySet()) {
				results.add(String.format("%s %s:%s", myKeyValue, filename, StringUtils.join(myLocations.get(filename), ", ")));
			}
		}
		return results;
	}
		
	public String toGreppableString(){
		return StringUtils.join(grepableFormatList(), "\n");
	}
	
	public String toByFilenameString(){
		return StringUtils.join(accumulateFileLocations	(), "\n");
	}
}
