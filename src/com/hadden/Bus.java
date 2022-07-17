package com.hadden;
public interface Bus 
{
	byte read(short address); 	
	void write(short address, byte data);
}
