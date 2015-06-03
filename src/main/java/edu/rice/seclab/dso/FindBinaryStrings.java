package edu.rice.seclab.dso;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class FindBinaryStrings {

	// Hash Table mapping hash values to key bytes
	HashMap<Long, BinaryStringInfo> hashToKeys = new HashMap<Long, BinaryStringInfo>();
	HashMap<String, ArrayList<BinaryStringInfo>> msToKeys = new HashMap<String, ArrayList<BinaryStringInfo>>();

	HashMap<String, ArrayList<String>> keysToFileOffset = new HashMap<String, ArrayList<String>>();
	
	Integer myNumThreads = 1;
	Integer myOffsetStart;
	private String myMemDump;
	private String myBinaryStringsFile;
	
	public FindBinaryStrings(String binary_strings_filename,
			String memory_dump_file, Integer offset, Integer numThreads) {
		
		myBinaryStringsFile = binary_strings_filename;
		myMemDump = memory_dump_file;
		myNumThreads = numThreads;
		myOffsetStart = offset;
		readStrings();
		
	}

	void readStrings () {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(myBinaryStringsFile));
		    for(String line; (line = br.readLine()) != null; ) {
		        // process the line.
		    	try {
		    		BinaryStringInfo bsi = new BinaryStringInfo(line);
		    		synchronized (hashToKeys) {
		    			hashToKeys.put(bsi.getHash(), bsi);
					}
		    	} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String binary_strings_filename = args[0];
		String memory_dump_file = args[1];
		String num_scannning_threads = args.length > 3 ? args[3] : "1";
		String offset_start = args.length > 2 ? args[2] : "0";

		Integer numThreads = 1;
		try {
			numThreads = Integer.valueOf(num_scannning_threads);
		} catch (Exception e) {
		}

		Integer offset = 1;
		try {
			offset = Integer.valueOf(offset_start);
		} catch (Exception e) {
		}
		FindBinaryStrings fbs = new FindBinaryStrings(binary_strings_filename,
				memory_dump_file, offset, numThreads);

	}

}
