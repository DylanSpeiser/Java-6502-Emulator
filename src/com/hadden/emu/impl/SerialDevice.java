package com.hadden.emu.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import com.hadden.ROMLoader;
import com.hadden.emu.BusAddressRange;
import com.hadden.emu.BusDevice;
import com.hadden.emu.RAM;
import com.hadden.emu.RaisesIRQ;
import com.hadden.emu.BusDevice.IOSize;
import com.hadden.emu.impl.Gfx256Device.GfxPort;
import com.hadden.emu.BusIRQ;
import com.hadden.emu.HasPorts;

public class SerialDevice implements BusDevice, RaisesIRQ, HasPorts
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

	public SerialDevice(BusAddressRange bar)
	{
		this.bank = new byte[4];
		this.bar = bar;

		ports[CONST_READ_BUFFER]  	= new AuxPortImpl("SERIAL_READ_BUFFER",bar.getLowAddress() 	+ CONST_READ_BUFFER);
		ports[CONST_WRITE_BUFFER] 	= new AuxPortImpl("SERIAL_WRITE_BUFFER",bar.getLowAddress()	+ CONST_WRITE_BUFFER);
		ports[CONST_READY] 		  	= new AuxPortImpl("SERIAL_READY",bar.getLowAddress() 		    + CONST_READY);
		ports[CONST_IRQ_STATUS] 		= new AuxPortImpl("SERIAL_IRQ_STATUS",bar.getLowAddress() 	+ CONST_IRQ_STATUS);
		ports[CONST_CONTROL]      	= new AuxPortImpl("SERIAL_CONTROL",bar.getLowAddress() 		+ CONST_CONTROL);
	
		this.threadServer = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					final ServerSocket server = new ServerSocket(11001);
					//System.out.println("Listening for connection on port 11001 ....");
					while (true)
					{
						final Socket clientSocket = server.accept();
						InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
						BufferedReader reader = new BufferedReader(isr);
						int b = 0;
						do
						{
							if(clientSocket.getInputStream().available() > 0)
							{
								b = clientSocket.getInputStream().read();								
								//System.out.println(Integer.toHexString(b));
								rcvBuffer.add((byte)b);
								bank[CONST_READY] = 1;
								bank[CONST_IRQ_STATUS] = 1;
								if(irq!=null)
								{
									irq.raise(0);
									bank[CONST_IRQ_STATUS] = 0;
								}
							}
							if(sndBuffer.size() > 0)
							{
								Byte db = sndBuffer.poll();
								if(db != null)
									clientSocket.getOutputStream().write(db);
							}
						}while(b!='Q');
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

	public SerialDevice(int bankAddress)
	{
		this(new BusAddressRange(bankAddress, CONST_LAST, 1));
	}

	@Override
	public String getName()
	{
		return "SERIAL";
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
			// System.out.print(BusAddressRange.makeHexAddress(address));
		}
		int effectiveAddress = address - this.bar.getLowAddress();
		
		this.bank[effectiveAddress] = (byte) (value & 0x0FF);
		this.sndBuffer.offer(this.bank[effectiveAddress]);
	}

	@Override
	public int readAddressSigned(int address, IOSize size)
	{
		int effectiveAddress = address - this.bar.getLowAddress();
		
		switch(effectiveAddress)
		{
		case CONST_READ_BUFFER:		
			if(this.rcvBuffer.size() > 0)
			{
				Byte b = this.rcvBuffer.poll();
				
				if(this.rcvBuffer.size() > 0)
					bank[CONST_READY] = 1;
				else
					bank[CONST_READY] = 0;
				
				return (b!=null)?(int)b:0;
			}
			break;
		}	
		
		return bank[effectiveAddress];
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

	
	public static void main(String[] args)
	{
		SerialDevice rd = new SerialDevice(0x00000200);
		
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
