package edu.rice.seclab.dso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;

public class ChunkProcessor extends Thread {
	public static final Long CHUNK_SCAN_SIZE = 4096L * 10;
	Boolean keepRunning = true;
	Boolean stillRunning = false;
	RandomAccessFile fhandle = null;
	HashMap<Long, BinaryStringInfo> myBinaryKeyInfo = null;
	private Long myChunkSize = -1L;
	private Long myChunkOffset = 0L;
	private int myMaxKeyLength = 0;
	private long myBaseOffset;
	IHashFunction myHashFn = null;
	String myFilename;
	Integer matches = 0;
	File myFile;
	private boolean myLiveUpdate = false;
	

	public ChunkProcessor(File file, long offset, long chunk_size,
			HashMap<Long, BinaryStringInfo> binaryKInfo, IHashFunction hasher, boolean liveUpdate){
		myChunkSize = chunk_size;
		myFile = file;
		myFilename = myFile.getAbsolutePath();
		myBinaryKeyInfo = binaryKInfo;
		myBaseOffset = offset;
		myHashFn = hasher;
		for (BinaryStringInfo x : myBinaryKeyInfo.values()) {
			int klen = x.getKeyLen();
			if (klen > myMaxKeyLength)
				myMaxKeyLength = klen;
		}
		myLiveUpdate = liveUpdate;

	}

	void foundOne() {
		synchronized (matches) {
			matches += 1;
		}
	}

	private byte[] performRead() throws IOException {
		// 1) update the chunk offset for reading data
		myChunkOffset = myChunkOffset > myMaxKeyLength ? myChunkOffset - myMaxKeyLength + 1 :
							myChunkOffset;
		// 2) calculate the chunk size for reading data
		long sz = myChunkOffset + CHUNK_SCAN_SIZE < myChunkSize ? CHUNK_SCAN_SIZE : myChunkSize - myChunkOffset;

		fhandle.seek(myBaseOffset + myChunkOffset);
		byte[] res = new byte[(int) sz];
		long a_sz = fhandle.read(res);
		if (a_sz != sz) {
			System.err.println(String
					.format("Warning: attempted to read %d bytes but got %d", 
							sz, a_sz));
		}
		return res;
	}
		
	private void incrementOffset(byte [] data) {
		myChunkOffset += CHUNK_SCAN_SIZE;
		//System.out.println(String.format("Incrementing myChunkOffset by %d, points to 0x%08x", CHUNK_SCAN_SIZE, myChunkOffset+myBaseOffset));
	}

	long calculateActualOffset() {
		return myChunkOffset + myBaseOffset;
	}

	long calculateActualOffset(long at) {
		return at + calculateActualOffset();
	}

	private int performComparisonsOnChunk(byte[] data) throws Exception {
		long end = data.length - myHashFn.getRequiredBytes();
		for (int pos = 0; pos <= end; pos += 1) {
			Long hash;
			try {
				//System.out.println(String.format("@ %08x + %08x (base:%08x and offset:%08x) = %08x", pos, myBaseOffset + myChunkOffset, myBaseOffset, myChunkOffset,  pos + myBaseOffset + myChunkOffset));
				hash = myHashFn.executeHash(data, pos);
				//if (hash == 0xe951feffff8d02e8L){
				//	System.out.println("Here.");
				//}
				//System.out.println(String.format("%08x hash @ %08x", hash, pos));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			}
			if (myBinaryKeyInfo.containsKey(hash)) {
				BinaryStringInfo bi = myBinaryKeyInfo.get(hash);
				long offset = calculateActualOffset(pos);
				if (bi.matchAdd(data, pos, myFilename, offset)) {
					foundOne();
					if (myLiveUpdate) Utils.foundKey(bi.getKey(), myFilename, offset);
				}
					
					
			}
		}
		return matches;
	}

	@Override
	public void run() {
		
		stillRunning = true;
		keepRunning = true;
		
		try {
			fhandle = new RandomAccessFile(myFilename, "r");
		} catch (FileNotFoundException e1) {
			keepRunning = false;
			e1.printStackTrace();
		}
		
		while (keepRunning) {
			try {
				byte[] data = performRead();
				performComparisonsOnChunk(data);
				incrementOffset(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				shutdown();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				shutdown();
			}
			if (myChunkOffset >= myChunkSize)
				shutdown();
		}
		stillRunning = false;

	}

	public void shutdown() {
		synchronized (keepRunning) {
			keepRunning = false;
		}
	}

}
