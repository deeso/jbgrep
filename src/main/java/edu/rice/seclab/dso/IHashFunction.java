package edu.rice.seclab.dso;

public interface IHashFunction {
	public Long executeHash(byte []data) throws Exception;
	public Long executeHash(byte []data, long offset) throws Exception;
	public long getRequiredBytes();
	public boolean match(byte[] lhs, byte [] rhs);
	public boolean match(byte[] lhs, int lhs_offset, byte [] rhs);
}
