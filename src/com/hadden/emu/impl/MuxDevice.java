package com.hadden.emu.impl;

import com.hadden.ROMLoader;
import com.hadden.emu.AddressMapImpl;
import com.hadden.emu.BusAccessor;
import com.hadden.emu.BusAddressRange;
import com.hadden.emu.BusDevice;
import com.hadden.emu.BusIRQ;
import com.hadden.emu.BusReader;
import com.hadden.emu.BusWriter;
import com.hadden.emu.IOSize;
import com.hadden.emu.RAM;
import com.hadden.emu.RaisesIRQ;
import com.hadden.emu.c64.CIADevice;

public class MuxDevice implements BusDevice, RAM, BusAccessor, RaisesIRQ
{
	//private byte[] bank;
	private BusAddressRange bar;
	private BusDevice[] banks = null;
	private int portAddress = 0x00;
	private int bankId = 0;
	private MuxMapper mapper = null;

	private BusReader busReader = null;
	private BusIRQ muxIRQ = null;

	public interface MuxMapper
	{
		int map(int value);
	}
	
	public MuxDevice(BusAddressRange bar, int portAddress, MuxMapper mapper, BusDevice[] devs)
	{
		this.bar = bar;
		this.portAddress  = portAddress;
		this.mapper = mapper;
		
		banks = new BusDevice[devs.length];
		for(int b=0;b<devs.length;b++)
		{
			banks[b] = devs[b];
			if(banks[b] instanceof RaisesIRQ)
			{
				((RaisesIRQ)banks[b]).attach(new BusIRQ()
				{
					@Override
					public void raise(int source)
					{
						if(muxIRQ!=null)
							muxIRQ.raise(source);
					}
				});
			}
		}
	}

	public MuxDevice(int bankAddress, int bankSize, int portAddress,  MuxMapper mapper,BusDevice[] devs)
	{
		this(new BusAddressRange(bankAddress, bankSize, 1), portAddress, mapper, devs);
	}

	@Override
	public String getName()
	{
		String name = "";
		
		for(int i = 0;i<this.banks.length;i++)
		{
			if(name.length() > 0)
				name+=",";
			name+=banks[i].getName();
		}
		
		return "MUX:" + name ;
	}

	@Override
	public BusAddressRange getBusAddressRange()
	{
		return this.bar;
	}

	@Override
	public void writeAddress(int address, int value, IOSize size)
	{
		int flag = 0;
		
		if(mapper!=null)
			flag = mapper.map(this.busReader.read(this.portAddress));
		else
			flag = (byte) (this.busReader.read(this.portAddress) - 1);

		if(flag < banks.length)
		{
			this.banks[flag].writeAddress(address, value, size);
		}				
	}

	@Override
	public int readAddressSigned(int address, IOSize size)
	{
		int flag = 0;
		
		if(mapper!=null)
			flag = mapper.map(this.busReader.read(this.portAddress));
		else
			flag = (byte) (this.busReader.read(this.portAddress) - 1);
			
		if(flag < banks.length)
		{
			return this.banks[flag].readAddressSigned(address, size);
		}		
		
		return 0;
	}

	@Override
	public int readAddressUnsigned(int address, IOSize size)
	{
		return readAddressSigned(address,size);
	}

	public void dumpContents(int max)
	{
		//this.banks[bankId].dumpContents(max);
	}

	public String toString(int bytesPerLine, boolean addresses)
	{
		return null;//this.banks[bankId].toString(bytesPerLine, addresses);
	}

	public static void main(String[] args)
	{
		AddressMapImpl map =  new AddressMapImpl(new RAMDevice(0x00000000,64*1024),
                new BusIRQ() 
				{
					@Override
					public void raise(int source)
					{
						System.out.println("CPU IRQ");
						
					}
				});
				
		
		MuxDevice mux = new MuxDevice(0x0000D000, 0x3FF, 0x00000000, 
				new MuxMapper() 
				{
					@Override
					public int map(int value)
					{
						if( (((byte)value) & (byte)0x04) == 0x04)
							return 1;
						return 0;
					}
				},new BusDevice[] 
				{
						new RAMDevice(0x0000D000, 0x3FF),
						new RAMDevice(0x0000D000, 0x3FF)
				} );
		
		
		map.addBusDevice(mux);
		
		map.write((short)0x0000,(byte)0x0C);		
		for(short a = (short) 0xD000;a<(short)0xD0A0;a++)
			map.write(a,(byte)0x02);		
		
		map.write((short)0x0000,(byte)0x03);
		for(short a = (short) 0xD000;a<(short)0xD0A0;a++)
			map.write(a,(byte)0x01);		
		
		
		map.write((short)0x0000,(byte)0x05);
		System.out.println("Read[" + map.read((short)0x0000) + "]:" + map.read((short)0xD000));
		map.write((short)0x0000,(byte)0x03);
		System.out.println("Read[" + map.read((short)0x0000) + "]:" + map.read((short)0xD000));

		System.out.println();

	}

	@Override
	public void setRAMArray(byte[] array)
	{
		setRAMArray(0,array);	
	}	
	
	@Override
	public void setRAMArray(int base, byte[] array) 
	{
		//this.banks[bankId].setRAMArray(array);		
	}

	@Override
	public byte read(short address)
	{
		return 0; //this.banks[bankId].read(address);	
	}

	@Override
	public void write(short address, byte data)
	{
		//this.banks[bankId].write(address,data);		
	}

	@Override
	public String getRAMString()
	{
		//return  this.banks[bankId].toString(8, true);
		return null;
	}

	@Override
	public void reset()
	{
		//this.banks[bankId].reset();
	}

	@Override
	public void setReader(BusReader reader)
	{
		this.busReader = reader;	
	}

	@Override
	public void setWriter(BusWriter write)
	{
	}

	@Override
	public void attach(BusIRQ irq)
	{
		this.muxIRQ = irq;
		
	}

}
