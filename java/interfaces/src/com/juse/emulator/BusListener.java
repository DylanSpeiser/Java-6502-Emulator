package com.juse.emulator;
public interface BusListener 
{
	void readListener(short address); 	
	void writeListener(short address, byte data);
	void busReset();
}
