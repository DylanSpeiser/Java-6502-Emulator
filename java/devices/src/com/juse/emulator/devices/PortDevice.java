package com.juse.emulator.devices;

import com.juse.emulator.util.loaders.ROMLoader;
import com.juse.emulator.interfaces.BusAddressRange;
import com.juse.emulator.interfaces.BusDevice;
import com.juse.emulator.interfaces.IOSize;
import com.juse.emulator.interfaces.RAM;

public class PortDevice implements BusDevice, RAM
{
	private byte[] bank;
	private BusAddressRange bar;

	public PortDevice(BusAddressRange bar)
	{
		int bankSize = bar.getSize();
		this.bank = new byte[bankSize];
		this.bar = bar;
	}

	public PortDevice(int bankAddress, int bankSize)
	{
		this(new BusAddressRangeImpl(bankAddress, bankSize, 1));
	}

	@Override
	public String getName()
	{
		return "RAM";
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
		int effectiveAddress = address - this.bar.getLowAddress();
		this.bank[effectiveAddress] = (byte) (value & 0x0FF);
	}

	@Override
	public int readAddressSigned(int address, IOSize size)
	{
		int effectiveAddress = address - this.bar.getLowAddress();
		return bank[effectiveAddress];
	}

	@Override
	public int readAddressUnsigned(int address, IOSize size)
	{
		int effectiveAddress = address - this.bar.getLowAddress();
		return bank[effectiveAddress];
	}

	public void dumpContents(int max)
	{
		if (max == -1)
		{
			max = bank.length - 1;
		}
		int bytes = 0;
		for (int i = 0; i < max; i++)
		{
			if (bytes == 0)
				System.out.print(BusAddressRange.makeHexAddress(this.bar.getLowAddress() + i) + ": ");

			System.out.print(BusAddressRange.makeHex(bank[i]));
			System.out.print(" ");
			bytes++;
			if (bytes > 7)
			{
				System.out.println();
				bytes = 0;
			}
		}
	}

	public String toString(int bytesPerLine, boolean addresses)
	{
		StringBuilder sb = new StringBuilder();

		if (addresses)
			sb.append("0000: ");

		for (int i = 1; i <= bank.length; i++)
		{
			if ((i % bytesPerLine != 0) || (i == 0))
			{
				sb.append(ROMLoader.byteToHexString(bank[i - 1]) + " ");
			}
			else
			{
				String zeroes = "0000";
				sb.append(ROMLoader.byteToHexString(bank[i - 1]) + "\n");
				if (addresses)
					sb.append(zeroes.substring(0, Math.max(0, 4 - Integer.toHexString(i).length()))
							+ Integer.toHexString(i) + ": ");
			}
		}

		return sb.toString();
	}

	public static void main(String[] args)
	{
		PortDevice rd = new PortDevice(0x00010000, 64 * 1024);
		System.out.println(rd.getName());
		System.out.println(
				rd.getBusAddressRange().getLowAddressHex() + ":" + rd.getBusAddressRange().getHighAddressHex());
		rd.writeAddress(0x10000, 0xFF, IOSize.IO8Bit);
		rd.writeAddress(0x10001, 0xFE, IOSize.IO8Bit);
		rd.writeAddress(0x10002, 0xFD, IOSize.IO8Bit);
		rd.dumpContents(32);

		System.out.println(BusAddressRange.makeHex((byte) rd.readAddressUnsigned(0x10000, IOSize.IO8Bit)));
		System.out.println(BusAddressRange.makeHex((byte) rd.readAddressUnsigned(0x10002, IOSize.IO8Bit)));

	}

	@Override
	public void setRAMArray(byte[] array)
	{
		setRAMArray(0,array);	
	}	
	
	@Override
	public void setRAMArray(int base, byte[] array) 
	{
		//this.array = array;
		for(int p=0;p<this.bank.length;p++)
			this.bank[p] = 0x00;
		
		for(int p=0;p<array.length;p++)
			this.bank[p] = array[p];
		
	}

	@Override
	public byte read(short address)
	{
		return (byte)this.readAddressSigned((int)address, IOSize.IO8Bit);
	}

	@Override
	public void write(short address, byte data)
	{
		this.writeAddress((int)address, data, IOSize.IO8Bit);		
	}

	@Override
	public String getRAMString()
	{
		return  this.toString(8, true);
	}

	@Override
	public void reset()
	{
		for(int p=0;p<this.bank.length;p++)
			this.bank[p] = 0x00;		
	}

}
