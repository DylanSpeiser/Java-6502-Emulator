package com.juse.emulator.devices.c64;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import com.juse.emulator.util.loaders.ROMLoader;
import com.juse.emulator.devices.AuxPortImpl;
import com.juse.emulator.devices.BusAddressRangeImpl;
import com.juse.emulator.interfaces.BusAddressRange;
import com.juse.emulator.interfaces.BusDevice;
import com.juse.emulator.interfaces.BusIRQ;
import com.juse.emulator.interfaces.HasPorts;
import com.juse.emulator.interfaces.IOSize;
import com.juse.emulator.interfaces.RaisesIRQ;

public class VICIIDevice implements BusDevice, RaisesIRQ
{
	private static final int CONST_READ_BUFFER 	= 0;
	private static final int CONST_WRITE_BUFFER 	= 1;
	private static final int CONST_READY 		= 2;
	private static final int CONST_IRQ_STATUS 	= 3;
	private static final int CONST_CONTROL    	= 4;
	private static final int CONST_LAST    	    = 5;
	
	private byte[] bank;
	private BusAddressRange   	bar;
	private BusDevice[] 	ports = new BusDevice[CONST_LAST];
	private Thread 				threadServer;
	
	private Queue<Byte> sndBuffer = new LinkedList<Byte>();
	private Queue<Byte> rcvBuffer = new LinkedList<Byte>();
	private BusIRQ irq;
	private int rasterLine;
	
	byte irqFlag = 0;
	byte borderColor = 0;
	byte backgroundColor = 0;
	

	public VICIIDevice(BusAddressRange bar)
	{
		this.bank = new byte[4];
		this.bar = bar;
		this.rasterLine = 0;
		
		ports[CONST_READ_BUFFER]  	= (BusDevice) new AuxPortImpl("SERIAL_READ_BUFFER",bar.getLowAddress() 	+ CONST_READ_BUFFER);
		ports[CONST_WRITE_BUFFER] 	= (BusDevice) new AuxPortImpl("SERIAL_WRITE_BUFFER",bar.getLowAddress()	+ CONST_WRITE_BUFFER);
		ports[CONST_READY] 		  	= (BusDevice) new AuxPortImpl("SERIAL_READY",bar.getLowAddress() 		    + CONST_READY);
		ports[CONST_IRQ_STATUS]     = (BusDevice) new AuxPortImpl("SERIAL_IRQ_STATUS",bar.getLowAddress() 	+ CONST_IRQ_STATUS);
		ports[CONST_CONTROL]      	= (BusDevice) new AuxPortImpl("SERIAL_CONTROL",bar.getLowAddress() 		+ CONST_CONTROL);
	
		this.threadServer = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					while(true)
					{
						if(rasterLine < 262)
							rasterLine++;
						else 
						{
							rasterLine = 0;
							writeAddress(0xDC0D,0x80,IOSize.IO8Bit);
							if(irq!=null)
							{
								System.out.println("VICII IRQ");
								irq.raise(0);
							}
						}
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

	public VICIIDevice(int bankAddress)
	{
		this(new BusAddressRangeImpl(bankAddress, bankAddress + 0x3FF));
	}

	@Override
	public String getName()
	{
		return "VICII";
	}

	@Override
	public BusAddressRange getBusAddressRange()
	{
		return this.bar;
	}

	@Override
	public void writeAddress(int address, int value, IOSize size)
	{
		//SystemEmulator.debug("VIC WRITE[" + Integer.toHexString(address & 0xFFFF) + "]:" + Integer.toHexString(value & 0xFF));
		if((address & 0xFFFF) == 0xD011)
		{
			//System.out.println("VIC WRITE[" + Integer.toHexString(address & 0xFFFF) + "]:" + Integer.toHexString(value & 0xFF));
			
		}
		else if((address & 0xFFFF) == 0xD012)
		{
			
			
		}
		else if((address & 0xFFFF) == 0xD013)
		{
			
			
		}
		else if((address & 0xFFFF) == 0xD020)
		{
			borderColor = (byte)(value & 0xFF);			
		}
		else if((address & 0xFFFF) == 0xD021)
		{
			backgroundColor = (byte)(value & 0xFF);
		}
		else if((address & 0xFFFF) == 0xDC0D) 
		{
			System.out.println("VIC WRITE[" + Integer.toHexString(address & 0xFFFF) + "]:" + Integer.toHexString(value & 0xFF));
			irqFlag = (byte) (value & 0xFF);
		}
		
		
		
	}

	@Override
	public int readAddressSigned(int address, IOSize size)
	{
		if(0xDC0D == (address & 0xFFFF))
		{
			//SystemEmulator.debug("VIC READ[" + Integer.toHexString(address & 0xFFFF) + "]:" + Integer.toHexString(rasterLine & 0xFF));
			int temp = irqFlag;
			irqFlag = 0;
			return temp;
		}	
		else if((address & 0xFFFF) == 0xD020)
		{
			return borderColor;	
		}
		else if((address & 0xFFFF) == 0xD021)
		{
			return backgroundColor;
		}

		return rasterLine & 0xFF;
	}

	@Override
	public int readAddressUnsigned(int address, IOSize size)
	{
		return readAddressSigned( address, size);
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


	/*
	@Override
	public BusDevice[] ports(int baseAddress)
	{
		BusDevice[] auxPorts = {
				ports[CONST_READ_BUFFER],
				ports[CONST_WRITE_BUFFER],
				ports[CONST_READY],
				ports[CONST_IRQ_STATUS],
				ports[CONST_CONTROL],	
		};
		return auxPorts;
	}	
	*/

	
	public static void main(String[] args)
	{
		VICIIDevice rd = new VICIIDevice(0x00000200);
		
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

	}

	@Override
	public void attach(BusIRQ irq)
	{
		this.irq = irq;		
	}

	@Override
	public void reset()
	{
		// TODO Auto-generated method stub
		
	}	
	
}
