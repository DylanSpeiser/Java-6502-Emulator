package com.hadden.emu.c64;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import com.hadden.ROMLoader;
import com.hadden.emu.BusAddressRange;
import com.hadden.emu.BusDevice;
import com.hadden.emu.RaisesIRQ;
import com.hadden.emu.impl.AuxPortImpl;
import com.hadden.emu.BusIRQ;
import com.hadden.emu.HasPorts;
import com.hadden.emu.IOSize;

public class TimerDevice implements BusDevice, RaisesIRQ
{
	private BusAddressRange   	bar;
	private Thread 				threadServer;
	
	private int timerValue;
	private byte hi = 0;
	private byte lo = 0;
			
	private String name;

	public TimerDevice(String name, BusAddressRange bar)
	{
		this.name = name;
		this.bar = bar;
		this.timerValue = 0;

		this.threadServer = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					while(true)
					{
						if(timerValue <= 0xFFFF)
							timerValue++;
						else 
							timerValue = 0;
						Thread.sleep(16);
					}
				} 
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		this.threadServer.start();
	}

	public TimerDevice(String name, int bankAddress)
	{ 
		this(name, new BusAddressRange(bankAddress, 2, 1));
	}

	public TimerDevice(int bankAddress)
	{ 
		this("Timer", new BusAddressRange(bankAddress, 2, 1));
	}

	
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public BusAddressRange getBusAddressRange()
	{
		return this.bar;
	}

	@Override
	public void writeAddress(int address, int value, IOSize size)
	{
		if(bar.getLowAddress() == address)
			lo = (byte)(value & 0xFF);
		else if(bar.getHighAddress() == address)
			hi = (byte)(value & 0xFF);
	}

	@Override
	public int readAddressSigned(int address, IOSize size)
	{
		lo = (byte)(timerValue & 0xFF);
		hi = (byte)((timerValue & 0xFF00) >> 8);
		if(bar.getLowAddress() == address)
		{
			return lo;
		}
		return hi;
	}

	@Override
	public int readAddressUnsigned(int address, IOSize size)
	{
		return readAddressSigned( address, size);
	}

	public void dumpContents(int max)
	{
	}

	public String toString(int bytesPerLine, boolean addresses)
	{
		StringBuilder sb = new StringBuilder();


		return sb.toString();
	}

	
	public static void main(String[] args)
	{
		int timerValue = 0xABCD;
		
		byte lo = (byte)(timerValue & 0xFF);
		byte hi = (byte)((timerValue & 0xFF00) >> 8);

		System.out.println("lo:" + Integer.toHexString(lo & 0xFF) );
		System.out.println("hi:" + Integer.toHexString(hi & 0xFF));
		
		/*
		TimerDevice rd = new TimerDevice(0x00000200);
		
		rd.attach(new BusIRQ() 
		{
			@Override
			public void raise(int source)
			{
				//System.out.println("Serial IRQ");
				int v = rd.readAddressSigned(0x000200, IOSize.IO8Bit);
				System.out.println("Serial IRQ:" + (char)v);
			}
		});
		
		System.out.println(rd.getName());
		System.out.println(rd.getBusAddressRange().getLowAddressHex() + ":" + 
		                   rd.getBusAddressRange().getHighAddressHex());
		rd.writeAddress(0x000201, 'M', IOSize.IO8Bit);
		rd.writeAddress(0x000201, '\r', IOSize.IO8Bit);
		rd.writeAddress(0x000201, '\n', IOSize.IO8Bit);
		rd.dumpContents(4);

		//System.out.println(BusAddressRange.makeHex((byte) rd.readAddressUnsigned(0x10000, IOSize.IO8Bit)));
		//System.out.println(BusAddressRange.makeHex((byte) rd.readAddressUnsigned(0x10002, IOSize.IO8Bit)));

		Scanner scan = new Scanner(System.in);

		while (true)
		{
			String input = scan.nextLine();
			if(input.length() > 0)
			{
				for(int i=0;i<input.length();i++)
					//rd.sndBuffer.add((byte)((int)input.charAt(0)));
					rd.writeAddress(0x000201, (byte)((int)input.charAt(0)), IOSize.IO8Bit);
			}
		}
		*/
	}

	@Override
	public void attach(BusIRQ irq)
	{
		//this.irq = irq;		
	}

	@Override
	public void reset()
	{
		// TODO Auto-generated method stub
		
	}	
	
}
