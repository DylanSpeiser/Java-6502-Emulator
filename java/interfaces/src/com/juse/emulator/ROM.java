package com.juse.emulator;



public interface ROM 
{
	public void setROMArray(byte[] array) ;
	public byte read(short address);
	public void write(short address, byte data);
	public String getROMString();
	public void reset();
}
