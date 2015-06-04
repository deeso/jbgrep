package edu.rice.seclab.dso;

public interface IHashFunction {
	public Long executeHash(byte []data);
	public long getRequiredBytes();
	public boolean match(byte[] lhs, byte [] rhs);
	public boolean match(byte[] lhs, int lhs_offset, byte [] rhs);
	//public IHashFunction getInstance();
}
