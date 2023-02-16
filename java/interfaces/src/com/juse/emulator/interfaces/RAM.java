package com.juse.emulator.interfaces;

public interface RAM 
{
	public void setRAMArray(byte[] array) ;
	public void setRAMArray(int base, byte[] array) ;
	public byte read(short address);
	public void write(short address, byte data);
	public String getRAMString();
	public void reset();
}
