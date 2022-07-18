package com.hadden.emu.impl;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Scanner;
import java.util.Stack;

import javax.swing.*;

import com.hadden.emu.BusAddressRange;
import com.hadden.emu.BusDevice;
import com.hadden.emu.HasPorts;

public class Gfx256Device extends JFrame implements BusDevice, HasPorts, ActionListener
{
	private static final int CONST_WIDTH_SIZE	= 640;
	private static final int CONST_HEIGHT_SIZE 	= 480;
	private static final int CONST_PAGE_SIZE 	= 0x2800;

	private Stack repaints = new Stack();
	
	GfxPanel gfxp = new GfxPanel();

	Scanner s;

	int x_offset[][] = {{0},{256}}; 
	int y_offset[][] = {{0},{6}}; 
	
	int baseAddress = 0;

	boolean debug = true;

	// Internal flags
	int cursorPos = 0;
	boolean increment = true;
	boolean displayPower = true;
	boolean cursor = false;
	boolean cursorBlink = false;
	boolean fourBitMode = false;

	BusAddressRange bar = null;
	byte[][] banks = new byte[CONST_HEIGHT_SIZE/(CONST_PAGE_SIZE/CONST_WIDTH_SIZE)][CONST_PAGE_SIZE];

	private int basePort = 0;

	private BusAddressRange barPort = null;

	private int portValue = 0;

	public Gfx256Device(int baseAddress)
	{
		this.setSize(CONST_WIDTH_SIZE, CONST_HEIGHT_SIZE);

		this.baseAddress = baseAddress;
		this.basePort  = 0x0000B00F;
		this.bar = new BusAddressRange(baseAddress,CONST_PAGE_SIZE,1);
		this.barPort  = new BusAddressRange(basePort,1,1);
		this.portValue  = 0;

		
		this.setTitle("GFX256 - " + CONST_WIDTH_SIZE + "x" + CONST_HEIGHT_SIZE);
		this.setResizable(true);
		this.add(gfxp);
		gfxp.setVisible(true);
		this.setAlwaysOnTop(true);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public class GfxPanel extends  Canvas
	{
		
		Color palette[] = {
							Color.BLACK,
				           Color.BLACK,
				           Color.BLACK,
						   Color.DARK_GRAY,
						   Color.GRAY,
						   Color.LIGHT_GRAY,
						   Color.RED,
						   Color.GREEN,
						   Color.BLUE,
						   Color.CYAN,
						   Color.ORANGE,
						   Color.YELLOW,
						   Color.MAGENTA,
						   Color.PINK,
						   Color.WHITE,
						   Color.WHITE
						  };
		
		public GfxPanel()
		{
			this.setSize(CONST_WIDTH_SIZE, CONST_HEIGHT_SIZE);
			this.setBounds(0,0,CONST_WIDTH_SIZE, CONST_HEIGHT_SIZE);
			this.setVisible(true);
		}

		//public void paintComponent(Graphics g)
		public void paint(Graphics g)
		{
			super.paint(g);
			System.out.println("paintComponent");
			//g.setColor(Color.BLACK);
			//g.fillRect(0, 0, gfxp.getWidth(), gfxp.getHeight());
			//for(int p=0;p<30;p++)
			
			if(!repaints.empty())
			{
				int p = ((Integer)repaints.pop()).intValue();
				System.out.println("Page:" + p);
				for(int i=0;i<banks[p].length;i++)
				{			
					if(banks[p][i] != 0)
					{
						Color color = palette[banks[p][i]];
						int x1 =  ((int)(i % (float)CONST_WIDTH_SIZE));
						int y1 =  ((int)(i / (float)CONST_WIDTH_SIZE)) + (16*p);
	
						//System.out.println("[" + Integer.toHexString(i) + "]x1:" + x1 + "," + y1);
						g.setColor(color);
						g.drawLine(x1,y1,x1,y1);
					}
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{

	}

	@Override
	public String getName()
	{
		return "GFX-256";
	}

	@Override
	public BusDevice[] ports(int baseAddress)
	{
		BusDevice[] ports = {new BusDevice() 
		{

			@Override
			public String getName()
			{
				return "Gfx256Port";
			}

			@Override
			public BusAddressRange getBusAddressRange()
			{
				return barPort;
			}

			@Override
			public void writeAddress(int address, int value, IOSize size)
			{
				if(address == basePort)
					portValue = value;				
			}

			@Override
			public int readAddressSigned(int address, IOSize size)
			{
				if(address == basePort)
					return portValue;
				
				return 0;			
			}

			@Override
			public int readAddressUnsigned(int address, IOSize size)
			{
				if(address == basePort)
					return portValue;
				
				return 0;		
			}
			
		}};
		return ports;
	}	
	
	@Override
	public BusAddressRange getBusAddressRange()
	{
		return this.bar;
	}

	@Override
	public void writeAddress(int address, int value, IOSize size)
	{
		if(address == basePort)
		{
			portValue = value;
			return;
		}
		banks[portValue][this.bar.getRelativeAddress(address)] = (byte)value;
		repaints.push(portValue);
		System.out.println("Push Page:" + portValue);
		gfxp.repaint();
	}

	@Override
	public int readAddressSigned(int address, IOSize size)
	{
		//return bank[this.bar.getRelativeAddress(address)];
		return 0;
	}

	@Override
	public int readAddressUnsigned(int address, IOSize size)
	{
		//return bank[this.bar.getRelativeAddress(address)];
		return 0;
	}

	public void dumpContents(int max)
	{
		/*
		if (max == -1)
		{
			max = bank.length - 1;
		}
		int bytes = 0;
		for (int i = 0; i < max; i++)
		{
			if (bytes == 0)
				System.out.print(BusAddressRange.makeHexAddress(this.bar.getLowAddress() + i) + ": ");

			System.out.print(BusAddressRange.makeHex((byte) (int) bank[i]));
			System.out.print(" ");
			bytes++;
			if (bytes > 7)
			{
				System.out.println();
				bytes = 0;
			}
		}
		*/
	}

	public static void main(String[] args)
	{
		Gfx256Device display = new Gfx256Device(0x0000E000);
		System.out.println(display.getBusAddressRange().getLowAddressHex() + ":" + 
		                   display.getBusAddressRange().getHighAddressHex());
		
		int pc = 3;
		for(int p=0;p<30;p++)
		{
			display.writeAddress(0x0000B00F, p, IOSize.IO8Bit);
			for(int c=0xE000;c< (0xE000 + CONST_PAGE_SIZE);c++)
				display.writeAddress(c, pc, IOSize.IO8Bit);
			
			pc++;
			if(pc>15)
				pc = 3;
		}
		//display.writeAddress(0x0000EFFF, 1, IOSize.IO8Bit);

		//for(int c=640;c<700;c++)
		//	display.writeAddress(0x0000E000 + c, 15, IOSize.IO8Bit);		
		
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		System.out.println("[ADDRESS] [COLOR]");

		while (true)
		{
			String input = scan.nextLine();

			String address = input.substring(0, 8);
			String color   = input.substring(8);

			int location = Integer.parseInt(address, 16);
			int colorCode = (int) Integer.parseInt(color);

			display.writeAddress(location, colorCode, IOSize.IO8Bit);
			//display.dumpContents(40);

		}
	}



}
