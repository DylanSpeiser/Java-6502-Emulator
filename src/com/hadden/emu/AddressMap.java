package com.hadden.emu;

import java.util.Collection;

import com.hadden.emu.BusDevice.IOSize;

public interface AddressMap
{
	public void setDefaultDevice(BusDevice bd);
	public void setIRQHandler(BusIRQ busIRQ);
	public AddressMap addBusDevice(BusDevice bd);
	public BusDevice getMemoryMappedDevice(int address);
	public void printAddressMap();
	public void addBusListener(BusListener listener);
	public Collection<BusDevice> getDevices();
	public void removeDevices();
	
	static public String toHexAddress(int address, IOSize size)
	{
		String padding =  "00000000";
		String hex = "";
		
		if(size == IOSize.IO8Bit)
		{
			hex = Integer.toHexString(address & 0xFF);
			if(hex.length() < 2 )
				hex = padding.substring(0, 2 - hex.length() ) + hex;
		}
		else if(size == IOSize.IO16Bit) 
		{
			hex = Integer.toHexString(address & 0xFFFF);
			if(hex.length() < 4 )
				hex = padding.substring(0, 4 - hex.length() ) + hex;

		}
		else if(size == IOSize.IO32Bit)
		{
			hex = Integer.toHexString(address & 0xFFFFFFFF);
			if(hex.length() < 8 )
				hex = padding.substring(0, 8 - hex.length() ) + hex;			
		}
		
		return "0x" + hex.toUpperCase();
	}
	
}
