package com.hadden.emu;
public interface Bus 
{
	byte read(short address); 	
	void write(short address, byte data);
	public String dumpBytesAsString();
}
