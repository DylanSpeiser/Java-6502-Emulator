package com.hadden.emu.impl;

import com.hadden.emu.BusAddressRange;
import com.hadden.emu.BusDevice;
import com.hadden.emu.IOSize;



public class AuxPortImpl implements BusDevice
{
	private String name;
	private BusAddressRange bar;
	private int portValue = 0;


	public AuxPortImpl(String name, int portAddress)
	{
		this.name = name;
		this.bar  = new BusAddressRange(portAddress,1,1);
	}
	
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public BusAddressRange getBusAddressRange()
	{
		return bar;
	}

	@Override
	public void writeAddress(int address, int value, IOSize size)
	{
		if(address == bar.getLowAddress())
			portValue = value;				
	}

	@Override
	public int readAddressSigned(int address, IOSize size)
	{
		if(address == bar.getLowAddress())
			return portValue;
		
		return 0;			
	}

	@Override
	public int readAddressUnsigned(int address, IOSize size)
	{
		if(address == bar.getLowAddress())
			return portValue;
		
		return 0;		
	}

	@Override
	public void reset()
	{
		// TODO Auto-generated method stub
		
	}
	
	
}
