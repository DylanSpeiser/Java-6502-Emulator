package com.juse.emulator.devices.c64;


import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import com.juse.emulator.util.loaders.ROMLoader;
import com.juse.emulator.devices.BusAddressRangeImpl;
import com.juse.emulator.interfaces.BusAddressRange;
import com.juse.emulator.interfaces.BusDevice;
import com.juse.emulator.interfaces.BusIRQ;
import com.juse.emulator.interfaces.IOSize;
import com.juse.emulator.interfaces.RaisesIRQ;


public class KeyboardDevice implements BusDevice, RaisesIRQ
{
	private static final int CONST_MATRIX_ROW 	= 0x01;
	private static final int CONST_MATRIX_COL 	= 0x00;

	
	
	private byte[] bank;
	private BusAddressRange   	bar;
	private Thread 				threadServer;
	
	private Queue<Byte> rcvBuffer = new LinkedList<Byte>();

	private BusIRQ irq;

	public KeyboardDevice(BusAddressRange bar)
	{
		this.bank = new byte[2];
		this.bar = bar;

		this.threadServer = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					final ServerSocket server = new ServerSocket(11002);
					//System.out.println("Listening for connection on port 11001 ....");
					while (true)
					{
						final Socket clientSocket = server.accept();
						//InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
						//BufferedReader reader = new BufferedReader(isr);
						int b = 0;
						do
						{
							if(clientSocket.getInputStream().available() > 0)
							{
								b = clientSocket.getInputStream().read();								
								//System.out.println(Integer.toHexString(b));
								rcvBuffer.add((byte)b);
								bank[CONST_MATRIX_ROW] = 2;
								bank[CONST_MATRIX_COL] = 1;
								if(irq!=null)
								{
									irq.raise(0);
									//bank[CONST_IRQ_STATUS] = 0;
								}
							}

						}while(b!='~');
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

	public KeyboardDevice(int bankAddress)
	{
		this(new BusAddressRangeImpl(bankAddress, 2, 1));
	}

	@Override
	public String getName()
	{
		return "KEYBOARD";
	}

	@Override
	public BusAddressRange getBusAddressRange()
	{
		return this.bar;
	}

	@Override
	public void writeAddress(int address, int value, IOSize size)
	{
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
		return readAddressSigned( address, size);
	}

	public void dumpContents(int max)
	{
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
		KeyboardDevice rd = new KeyboardDevice(0x0000DC00);
		
		rd.attach(new BusIRQ() 
		{
			@Override
			public void raise(int source)
			{
				//System.out.println("Serial IRQ");
				int col = rd.readAddressSigned(0x0000DC00, IOSize.IO8Bit);
				int row = rd.readAddressSigned(0x0000DC01, IOSize.IO8Bit);
				System.out.println("col:" + col);
				System.out.println("row:" + row);
			}
		});
		/*
		System.out.println(rd.getName());
		System.out.println(rd.getBusAddressRange().getLowAddressHex() + ":" + 
		                   rd.getBusAddressRange().getHighAddressHex());
		rd.writeAddress(0x000201, 'M', IOSize.IO8Bit);
		rd.writeAddress(0x000201, '\r', IOSize.IO8Bit);
		rd.writeAddress(0x000201, '\n', IOSize.IO8Bit);
		rd.dumpContents(4);
		*/
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
