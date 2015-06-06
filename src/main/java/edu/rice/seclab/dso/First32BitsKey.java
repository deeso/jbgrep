package edu.rice.seclab.dso;

public class First32BitsKey implements IHashFunction{
	
		private static final First32BitsKey myInstance = new First32BitsKey();
		public final Integer HASH_SIZE = 8;
		
		private First32BitsKey(){}
		
		public Long executeHash(byte[] data) throws Exception {
			return executeHash(data, 0);
		}

		public long getRequiredBytes() {
			return Integer.BYTES;
		}

		public static First32BitsKey getInstance() {
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
			return Utils.first32bits(data, offset);
		}

}
