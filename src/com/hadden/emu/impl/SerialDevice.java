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
import com.hadden.emu.BusIRQ;

public class SerialDevice implements BusDevice, RaisesIRQ
{
	private byte[] bank;
	private BusAddressRange bar;
	private Thread threadServer;
	
	private Queue<Byte> sndBuffer = new LinkedList<Byte>();
	private Queue<Byte> rcvBuffer = new LinkedList<Byte>();

	public SerialDevice(BusAddressRange bar)
	{
		int bankSize = bar.getSize();
		this.bank = new byte[bankSize];
		this.bar = bar;

		this.threadServer = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					final ServerSocket server = new ServerSocket(8080);
					System.out.println("Listening for connection on port 8080 ....");
					while (true)
					{
						final Socket clientSocket = server.accept();
						InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
						BufferedReader reader = new BufferedReader(isr);
						int b = 0;
						do
						{
							//b = reader.read();
							
							if(clientSocket.getInputStream().available() > 0)
							{
								b = clientSocket.getInputStream().read();
								System.out.println("DATA:" + b);
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

	public SerialDevice(int bankAddress, int bankSize)
	{
		this(new BusAddressRange(bankAddress, bankSize, 1));
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
		SerialDevice rd = new SerialDevice(0x00010000, 64 * 1024);
		System.out.println(rd.getName());
		System.out.println(
				rd.getBusAddressRange().getLowAddressHex() + ":" + rd.getBusAddressRange().getHighAddressHex());
		rd.writeAddress(0x10000, 0xFF, IOSize.IO8Bit);
		rd.writeAddress(0x10001, 0xFE, IOSize.IO8Bit);
		rd.writeAddress(0x10002, 0xFD, IOSize.IO8Bit);
		rd.dumpContents(32);

		System.out.println(BusAddressRange.makeHex((byte) rd.readAddressUnsigned(0x10000, IOSize.IO8Bit)));
		System.out.println(BusAddressRange.makeHex((byte) rd.readAddressUnsigned(0x10002, IOSize.IO8Bit)));

		Scanner scan = new Scanner(System.in);

		while (true)
		{
			String input = scan.nextLine();
			if(input.length() > 0)
				rd.sndBuffer.add((byte)((int)input.charAt(0)));

		}

	}

	@Override
	public void attach(BusIRQ irq)
	{
		// TODO Auto-generated method stub
		
	}

}
