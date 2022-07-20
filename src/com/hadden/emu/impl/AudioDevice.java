package com.hadden.emu.impl;

import com.hadden.ROMLoader;
import com.hadden.emu.BusAddressRange;
import com.hadden.emu.BusDevice;
import com.hadden.emu.RAM;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioDevice implements BusDevice, RAM
{
	private byte[] bank;
	private BusAddressRange bar;

	public AudioDevice(BusAddressRange bar)
	{
		int bankSize = bar.getSize();
		this.bank = new byte[bankSize];
		this.bar = bar;
	}

	public AudioDevice(int bankAddress, int bankSize)
	{
		this(new BusAddressRange(bankAddress, bankSize, 1));
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
		if (address < 0)
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
					sb.append(
							zeroes.substring(0, Math.max(0, 4 - Integer.toHexString(i).length())) + Integer.toHexString(i) + ": ");
			}
		}

		return sb.toString();
	}

	public static void main(String[] args)
	{
		// AudioDevice rd = new AudioDevice(0x00010000, 64 * 1024);
		// System.out.println(rd.getName());
		// System.out.println(
		// rd.getBusAddressRange().getLowAddressHex() + ":" +
		// rd.getBusAddressRange().getHighAddressHex());
		// rd.writeAddress(0x10000, 0xFF, IOSize.IO8Bit);
		// rd.writeAddress(0x10001, 0xFE, IOSize.IO8Bit);
		// rd.writeAddress(0x10002, 0xFD, IOSize.IO8Bit);
		// rd.dumpContents(32);
		//
		// System.out.println(BusAddressRange.makeHex((byte)
		// rd.readAddressUnsigned(0x10000, IOSize.IO8Bit)));
		// System.out.println(BusAddressRange.makeHex((byte)
		// rd.readAddressUnsigned(0x10002, IOSize.IO8Bit)));

		System.out.println("Make sound");
		try
		{
			byte[] buf = new byte[2];
			int frequency = 44100; // 44100 sample points per 1 second
			AudioFormat af = new AudioFormat((float) frequency, 16, 1, true, false);
			SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
			sdl.open();
			sdl.start();
			
			int durationMs = 2000;
			int numberOfTimesFullSinFuncPerSec = 441; // number of times in 1sec sin
													  // function repeats
			for (int i = 0; i < durationMs * (float) frequency / 1000; i++)
			{ // 1000 ms in 1 second
				float numberOfSamplesToRepresentFullSin = (float) frequency / numberOfTimesFullSinFuncPerSec;
				double angle = i / (numberOfSamplesToRepresentFullSin / 2.0) * Math.PI;
				short a = (short) (Math.sin(angle) * 32767); // 32767 - max value
																// for sample to
																// take (-32767 to
																// 32767)
				buf[0] = (byte) (a & 0xFF); // LO
				buf[1] = (byte) (a >> 8);   // HI
				sdl.write(buf, 0, 2);
			}
			
			
			
			
			durationMs = 1000;
			frequency  = 44000;			
			
			try
			{
				//FileInputStream faudio = new FileInputStream("c:\\Users\\mike.bush\\downloads\\audio.snd");
				//FileOutputStream faudio = new FileOutputStream("c:\\downloads\\audio.snd");
				FileInputStream faudio = new FileInputStream("c:\\downloads\\audio.snd");
				
				//for (int i = 0; i < durationMs * (float) frequency / 1000; i++)
				for (int j = 0; j < 1; j++)
				{
					//for (int i = 0; i < sound.length; i++)
					for (int i = 0; i < durationMs * (float) frequency / 1000; i++)	
					{ // 1000 ms in 1 second
					
						/*
						float numberOfSamplesToRepresentFullSin = (float) frequency / numberOfTimesFullSinFuncPerSec;
						double angle = i / (numberOfSamplesToRepresentFullSin / 2.0) * Math.PI;
						
						short a = (short) (Math.sin(angle) * 32767); // 32767 - max value
																		// for sample to
																		// take (-32767 to
																		// 32767)
						buf[0] = (byte) (a & 0xFF); // LO
						buf[1] = (byte) (a >> 8);   // HI
						System.out.println("{0x" +  Integer.toHexString(Byte.toUnsignedInt(buf[0])) + 
								           ",0x" + Integer.toHexString(Byte.toUnsignedInt(buf[1])) + "},//" + i );
						faudio.write((int)buf[0]);
						faudio.write((int)buf[1]);
						*/
						
												
						//int[] s = sound[i];
						
						//System.out.println("[0x" +  Integer.toHexString((s[0])) + ",0x" + Integer.toHexString((s[1])) + "]," );
						
						faudio.read(buf);
						
						//buf[0] = 0xe;
						//buf[1] = 0x7f;
						
						sdl.write(buf, 0, 2);
					}			
				}
				//faudio.flush();
				faudio.close();
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			sdl.drain();
			sdl.stop();

		
		
		}
		catch (LineUnavailableException e)
		{
			e.printStackTrace();
		}
		System.out.println("Done.");
	}

	@Override
	public void setRAMArray(byte[] array)
	{
		// this.array = array;
		for (int p = 0; p < this.bank.length; p++)
			this.bank[p] = 0x00;

		for (int p = 0; p < array.length; p++)
			this.bank[p] = array[p];

	}

	@Override
	public byte read(short address)
	{
		return (byte) this.readAddressSigned((int) address, IOSize.IO8Bit);
	}

	@Override
	public void write(short address, byte data)
	{
		this.writeAddress((int) address, data, IOSize.IO8Bit);
	}

	@Override
	public String getRAMString()
	{
		return this.toString(8, true);
	}

	@Override
	public void reset()
	{
		for (int p = 0; p < this.bank.length; p++)
			this.bank[p] = 0x00;
	}

}
