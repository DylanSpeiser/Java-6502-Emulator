package com.juse.emulator;

public interface BusAddressRange
{

	int getSize();

	int getSizeBytes();

	int getSizeKB();

	int getSizeMB();

	int getHighAddress();

	int getLowAddress();

	int getRelativeAddress(int address);

	String getHighAddressHex();

	String getLowAddressHex();

}