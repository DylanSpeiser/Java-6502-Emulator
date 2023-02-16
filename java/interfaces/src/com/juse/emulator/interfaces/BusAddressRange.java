package com.juse.emulator.interfaces;

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

	static public String makeHex(byte value)
	{
		String addr = Integer.toHexString(Byte.toUnsignedInt(value));
		
		int len = addr.length();
		while(len < 2)
		{
			addr = "0" + addr;
			len = addr.length();
		}
		
		addr = "0x" + addr;
		return addr; 				
	}
	
	static public String makeHexAddress(int address)
	{
		String addr = Integer.toHexString(address);
		
		int len = addr.length();
		while(len < 8)
		{
			addr = "0" + addr;
			len = addr.length();
		}
		
		addr = "0x" + addr;
		return addr; 				
	}	
}