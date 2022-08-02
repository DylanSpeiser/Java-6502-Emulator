package com.hadden.emu.impl;

import com.hadden.ROMLoader;
import com.hadden.emu.BusAddressRange;
import com.hadden.emu.BusDevice;
import com.hadden.emu.RAM;

public class MMUDevice implements BusDevice, RAM
{
	//private byte[] bank;
	private BusAddressRange bar;
	private RAMDevice[] banks = null;
	private int portAddress = 0x00;
	private int bankId = 0;

	public MMUDevice(BusAddressRange bar, byte cBanks, int portAddress)
	{
		this.bar = bar;
		this.portAddress  = portAddress;

		banks = new RAMDevice[cBanks];
		for(int b=0;b<cBanks;b++)
		{
			banks[b] = new RAMDevice(bar);
		}
	}

	public MMUDevice(int bankAddress, int bankSize, byte cBanks, int portAddress)
	{
		this(new BusAddressRange(bankAddress, bankSize, 1), cBanks, portAddress);
	}

	@Override
	public String getName()
	{
		return "MMU";
	}

	@Override
	public BusAddressRange getBusAddressRange()
	{
		return this.bar;
	}

	@Override
	public void writeAddress(int address, int value, IOSize size)
	{
		if(address < 0)
		{
			address = 0xFFFF + address + 1;
			System.out.print(BusAddressRange.makeHexAddress(address));
		}
		
		if(address == portAddress)
		{
			if(value > -1 && value < banks.length)
				this.bankId = (byte)value;
		}
		else
		{
			this.banks[bankId].writeAddress(address, value, size);
		}
	}

	@Override
	public int readAddressSigned(int address, IOSize size)
	{

		if(address != portAddress)
		{
			return this.banks[bankId].readAddressSigned(address, size);
		}		
		
		return 0;
	}

	@Override
	public int readAddressUnsigned(int address, IOSize size)
	{
		if(address != portAddress)
		{
			return this.banks[bankId].readAddressUnsigned(address, size);
		}		
		
		return 0;
	}

	public void dumpContents(int max)
	{
		this.banks[bankId].dumpContents(max);
	}

	public String toString(int bytesPerLine, boolean addresses)
	{
		return this.banks[bankId].toString(bytesPerLine, addresses);
	}

	public static void main(String[] args)
	{
		MMUDevice rd = new MMUDevice(0x00000000, 256 * 1024, (byte)2, 0x0000B00A);
		System.out.println(rd.getName());
		System.out.println(rd.getBusAddressRange().getLowAddressHex() + 
						   ":" + 
		                   rd.getBusAddressRange().getHighAddressHex());

		System.out.println("Bank 0:");
		rd.dumpContents(32);
		rd.writeAddress(0x0000, 0xFF, IOSize.IO8Bit);
		rd.writeAddress(0x0001, 0xFE, IOSize.IO8Bit);
		rd.writeAddress(0x0002, 0xFD, IOSize.IO8Bit);
		System.out.println("=========================================");
		rd.dumpContents(32);

		//System.out.println(BusAddressRange.makeHex((byte) rd.readAddressUnsigned(0x0000, IOSize.IO8Bit)));
		//System.out.println(BusAddressRange.makeHex((byte) rd.readAddressUnsigned(0x0002, IOSize.IO8Bit)));

		rd.writeAddress(0x0000B00A, 0x01, IOSize.IO8Bit);
		System.out.println("Bank 1:");
		rd.dumpContents(32);
		rd.writeAddress(0x0000, 0xAF, IOSize.IO8Bit);
		rd.writeAddress(0x0001, 0xAE, IOSize.IO8Bit);
		rd.writeAddress(0x0002, 0xAD, IOSize.IO8Bit);
		System.out.println("=========================================");
		rd.dumpContents(32);
		System.out.println("=========================================");
		rd.writeAddress(0x0000B00A, 0x00, IOSize.IO8Bit);
		System.out.println("Bank 0:");
		rd.dumpContents(32);
		rd.writeAddress(0x0000B00A, 0x01, IOSize.IO8Bit);
		System.out.println("Bank 1:");
		rd.dumpContents(32);		
		
	}

	@Override
	public void setRAMArray(byte[] array)
	{
		setRAMArray(0,array);	
	}	
	
	@Override
	public void setRAMArray(int base, byte[] array) 
	{
		this.banks[bankId].setRAMArray(array);		
	}

	@Override
	public byte read(short address)
	{
		return this.banks[bankId].read(address);	
	}

	@Override
	public void write(short address, byte data)
	{
		this.banks[bankId].write(address,data);		
	}

	@Override
	public String getRAMString()
	{
		return  this.banks[bankId].toString(8, true);
	}

	@Override
	public void reset()
	{
		this.banks[bankId].reset();
	}

}
