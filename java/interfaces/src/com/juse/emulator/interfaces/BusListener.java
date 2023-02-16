package com.juse.emulator.interfaces;
public interface BusListener 
{
	void readListener(short address); 	
	void writeListener(short address, byte data);
	void busReset();
}
