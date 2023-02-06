package com.hadden.emu.impl;

import java.util.Timer;
import java.util.TimerTask;

import com.hadden.emu.BusAddressRange;
import com.hadden.emu.BusDevice;
import com.hadden.emu.BusIRQ;
import com.hadden.emu.IOSize;
import com.hadden.emu.RaisesIRQ;

public class TimerDevice implements BusDevice, RaisesIRQ
{
	private byte[] bank;
	private BusAddressRange bar;
	private Timer timer = null;
	private BusIRQ irq = null;
	
	public TimerDevice(BusAddressRange bar, int period)
	{	
		int bankSize = bar.getSize();
		this.bank = new byte[bankSize];
		this.bar = bar;
		
		this.timer = new Timer(true);
		this.timer.scheduleAtFixedRate(new TimerTask() 
		{
			@Override
			public void run()
			{
				//System.out.println("TIMER EVENT");
				if(TimerDevice.this.irq!=null)
				{
					//System.out.println("TIMER IRQ");
					irq.raise(0);
				}
			}			
		}, 0, period);
	}

	public TimerDevice(int bankAddress, int period)
	{
		this(new BusAddressRange(bankAddress,2,1),period);
	}

	
	@Override
	public String getName()
	{
		return "TIMER";
	}

	@Override
	public BusAddressRange getBusAddressRange()
	{
		return this.bar;
	}

	@Override
	public void writeAddress(int address, int value, IOSize size)
	{
		this.bank[this.bar.getRelativeAddress(address)] = (byte)(value & 0x0FF);
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
		if(max == -1)
		{
			max = bank.length - 1;
		}
		int bytes = 0;
		for(int i=0;i<max;i++)
		{	
			if(bytes == 0)
				System.out.print(BusAddressRange.makeHexAddress(this.bar.getLowAddress() + i) + ": ");
				
			System.out.print(BusAddressRange.makeHex(bank[i]));
			System.out.print(" ");
			bytes++;
			if(bytes>7)
			{				
				System.out.println();
				bytes = 0;
			}
		}
	}
	
	public static void main(String[] args)
	{
		TimerDevice rd = new TimerDevice(0x00010000,64*1024);
		System.out.println(rd.getName());
		System.out.println(rd.getBusAddressRange().getLowAddressHex()+ ":" +  rd.getBusAddressRange().getHighAddressHex());		
		rd.writeAddress(0x10000, 0xFF, IOSize.IO8Bit);
		rd.writeAddress(0x10001, 0xFE, IOSize.IO8Bit);
		rd.writeAddress(0x10002, 0xFD, IOSize.IO8Bit);
		rd.dumpContents(32);
		
		System.out.println(BusAddressRange.makeHex((byte)rd.readAddressUnsigned(0x10000,IOSize.IO8Bit)));
		System.out.println(BusAddressRange.makeHex((byte)rd.readAddressUnsigned(0x10002,IOSize.IO8Bit)));
		
	}

	@Override
	public void attach(BusIRQ irq)
	{
		this.irq  = irq;
	}
	
	@Override
	public void reset()
	{
		// TODO Auto-generated method stub
		
	}
	
}
