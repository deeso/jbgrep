package edu.rice.seclab.dso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

public class BinaryStringInfo {
	Long myHash;
	String myKeyValue;
	byte[] myKeyBytes;
	byte[] _keyBytes;
	
	HashMap<String, HashSet<String>> myLocations = new HashMap<String, HashSet<String>>();
	
	public BinaryStringInfo(String binaryString) {
		myKeyValue = binaryString;
		myKeyBytes = getKeyBytes();
		_keyBytes = new byte[Long.BYTES];
		for (int i = 0; i < Long.BYTES; i++) {
			_keyBytes[i] = myKeyBytes[i];
		}
		myHash = Utils.bytesToLong(_keyBytes);
	}
	
	public BinaryStringInfo(String binaryString, byte[] keyBytes, long hash) {
		myKeyValue = binaryString;
		myKeyBytes = keyBytes;
		_keyBytes = new byte[Long.BYTES];
		for (int i = 0; i < Long.BYTES; i++) {
			_keyBytes[i] = myKeyBytes[i];
		}
		myHash = hash;
	}
	
	public boolean match(byte[] bytes) {
		boolean _match = false;
		if (bytes.length < myKeyBytes.length) return _match;
		for (int i = 0; i < myKeyBytes.length; i++){
			_match = myKeyBytes[i] == bytes[i];
			if (!_match) break;
		}
		return _match;
	}
	
	public boolean matchAdd(byte[] bytes, String filename, long offset) {
		if (match(bytes)) {
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

	public byte[] getKeyBytes(){
		return Utils.unhexlify(myKeyValue);
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
