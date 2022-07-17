package com.hadden.emu;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.hadden.emu.BusDevice.IOSize;
import com.hadden.emu.impl.DisplayDevice;
import com.hadden.emu.impl.LCDDevice;
import com.hadden.emu.impl.RAMDevice;
import com.hadden.emu.impl.TimerDevice;

public class AddressMap implements Bus
{
	private static AddressMap instance = null;
	
	private NavigableMap<Integer, BusDevice> mappedAddressSpace = new TreeMap<Integer, BusDevice>();

	private BusDevice defaultSpace = null;
	private BusIRQ birq = null;
	
	public AddressMap(BusDevice bd, BusIRQ birq)
	{
		this.defaultSpace  = bd;		
		this.birq = birq;
	}
	

	public AddressMap addBusDevice(BusDevice bd)
	{
		mappedAddressSpace.put(bd.getBusAddressRange().getLowAddress(), bd);
		mappedAddressSpace.put(bd.getBusAddressRange().getHighAddress()+1, defaultSpace);
		
		if(bd instanceof RaisesIRQ)
			((RaisesIRQ)(bd)).attach(this.birq);
		
		return this;
	}
	
	public BusDevice getMemoryMappedDevice(int address)
	{
		BusDevice bd = defaultSpace;
		
		if(mappedAddressSpace.floorEntry(address)!=null)
		{
			bd = mappedAddressSpace.floorEntry(address).getValue();
		}
		
		return bd;
	}

	
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		AddressMap map =  new AddressMap(new RAMDevice(0x00000000,64*1024),
				                         new BusIRQ() 
										{
											@Override
											public void raise(int source)
											{
												System.out.println("CPU IRQ");
												
											}
										});

		map.addBusDevice(new DisplayDevice(0x0000A000,40,10))
		   .addBusDevice(new LCDDevice(0x0000B000))
		   .addBusDevice(new TimerDevice(0x0000B003,60000));
		
		BusDevice bd = map.getMemoryMappedDevice(0x00001000);
		if(bd!=null)
			System.out.println(bd.getName());
		
		bd = map.getMemoryMappedDevice(0x0000A000);
		if(bd!=null)
			System.out.println(bd.getName());
		bd = map.getMemoryMappedDevice(0x0000a18f);
		if(bd!=null)
			System.out.println(bd.getName());

		bd = map.getMemoryMappedDevice(0x0000a190);
		if(bd!=null)
			System.out.println(bd.getName());
		
		bd = map.getMemoryMappedDevice(0x0000B000);
		if(bd!=null)
			System.out.println(bd.getName());		

		bd = map.getMemoryMappedDevice(0x0000B001);
		if(bd!=null)
			System.out.println(bd.getName());	
		
		bd = map.getMemoryMappedDevice(0x0000B003);
		if(bd!=null)
			System.out.println(bd.getName());	
		
		bd = map.getMemoryMappedDevice(0x0000B00A);
		if(bd!=null)
			System.out.println(bd.getName());		
		
		int base = 0x0000A000;
		String msg = "Hello World!";
		for(int c=0;c<msg.length();c++)
			map.getMemoryMappedDevice(base).writeAddress(base++, (byte)(int)msg.charAt(c), IOSize.IO8Bit);
		
		map.getMemoryMappedDevice(0x0000B000).writeAddress(0x0000B000, 0x0, IOSize.IO8Bit);
		map.getMemoryMappedDevice(0x0000B001).writeAddress(0x0000B001, 0xF, IOSize.IO8Bit);
		map.getMemoryMappedDevice(0x0000B000).writeAddress(0x0000B000, 0x1, IOSize.IO8Bit);
		map.getMemoryMappedDevice(0x0000B001).writeAddress(0x0000B001, 'A', IOSize.IO8Bit);
		
	}


	@Override
	public byte read(short address)
	{
		int laddr = address;
		
		if(address < 0)
		{
			laddr = 0xFFFF + address + 1;
			System.out.println(Integer.toHexString(laddr));
		}
		return (byte) getMemoryMappedDevice(laddr).readAddressUnsigned(laddr, IOSize.IO8Bit);
	}


	@Override
	public void write(short address, byte data)
	{
		int laddr = address;
		
		if(address < 0)
		{
			laddr = 0xFFFF + address + 1;
			System.out.println(Integer.toHexString(laddr));
		}		
		
		getMemoryMappedDevice(laddr).writeAddress(laddr, data,IOSize.IO8Bit);		
	}


}
