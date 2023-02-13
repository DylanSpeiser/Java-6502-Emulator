package com.hadden.emu.c64;

import java.util.Arrays;

import com.hadden.ROMLoader;
import com.hadden.emu.BusAddressRange;
import com.hadden.emu.BusDevice;
import com.hadden.emu.IOSize;
import com.hadden.emu.RAM;
import com.hadden.emu.ROM;
import com.hadden.roms.ROMManager;

public class KernalDevice implements BusDevice, ROM
{
	private byte[] bank;
	private BusAddressRange bar;

	public KernalDevice(BusAddressRange bar, byte[] romImage)
	{
		int bankSize = bar.getSize();
		this.bank = Arrays.copyOf(romImage, romImage.length);
		this.bar = bar;
		
	}

	public KernalDevice(int bankAddress, byte[] romImage)
	{
		this(new BusAddressRange(bankAddress, romImage.length, 1), romImage);
	}

	public KernalDevice(int bankAddress)
	{
		this(new BusAddressRange(bankAddress, 1024, 1), ROMManager.loadROM("kernal.rom"));
	}

	public KernalDevice(int bankAddress, String romName)
	{
		this(bankAddress, ROMManager.loadROM(romName));
	}
	
	public KernalDevice()
	{
		//this(new BusAddressRange(bankAddress, bankSize, 1), romImage);
	}
	
	
	@Override
	public String getName()
	{
		return "KERNAL";
	}

	@Override
	public BusAddressRange getBusAddressRange()
	{
		return this.bar;
	}

	@Override
	public void writeAddress(int address, int value, IOSize size)
	{
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

		byte[] rom = ROMManager.loadROM("kernal.rom");
		
		KernalDevice rd = new KernalDevice(0x0000E000, rom);
		System.out.println(rd.getName());
		System.out.println(
				rd.getBusAddressRange().getLowAddressHex() + ":" + rd.getBusAddressRange().getHighAddressHex());
		
		rd.dumpContents(32);
	}

	@Override
	public void setROMArray(byte[] array) 
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
	public String getROMString()
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
