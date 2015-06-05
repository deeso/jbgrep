package edu.rice.seclab.dso;

public class First64BitsKey implements IHashFunction{
	
	private static final First64BitsKey myInstance = new First64BitsKey();
	public final Integer HASH_SIZE = 8;
	
	private First64BitsKey(){}
	
	public Long executeHash(byte[] data) throws Exception {
		return executeHash(data, 0);
	}

	public long getRequiredBytes() {
		return Long.BYTES;
	}

	public static First64BitsKey getInstance() {
		return myInstance;
	}

	public boolean match(byte[] lhs, byte[] rhs) {
		return match(lhs, 0, rhs);
	}

	public boolean match(byte[] lhs, int lhs_offset, byte[] rhs) {
		boolean _match = false;
		if (lhs.length < rhs.length || lhs.length < rhs.length+lhs_offset ) return _match;
		for (int i = 0; i < rhs.length; i++){
			_match = rhs[i] == lhs[lhs_offset+i];
			if (!_match) break;
		}
		return _match;

	}

	public Long executeHash(byte[] data, long offset) throws Exception {
		return Utils.first64bits(data, offset);
	}

}
