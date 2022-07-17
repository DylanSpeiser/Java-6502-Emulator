package com.hadden.emu.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.*;

import com.hadden.emu.BusAddressRange;
import com.hadden.emu.BusDevice;
import com.hadden.emu.BusDevice.IOSize;
import com.hadden.fonts.FontManager;

public class DisplayDevice extends JFrame implements BusDevice, ActionListener
{
	DisplayPanel p = new DisplayPanel();
	Timer t;
	Timer cursorTimer;
	Font lcdFont;
	Scanner s;

	 int baseAddress = 0;
	 int displayColumns = 0;
	 int displayRows = 0;
	
	boolean graphicalCursorBlinkFlag = false;

	boolean debug = true;

	// Internal flags
	int cursorPos = 0;
	boolean increment = true;
	boolean displayPower = true;
	boolean cursor = false;
	boolean cursorBlink = false;
	boolean fourBitMode = false;

	BusAddressRange bar = null;
	char[] bank = null;

	public DisplayDevice(int baseAddress, int displayColumns, int displayRows)
	{
		this.setSize(1000, 900);
		
		this.baseAddress = baseAddress;
		this.displayColumns = displayColumns;
		this.displayRows = displayRows;
		this.bank = new char[displayColumns*displayRows];
		this.bar = new BusAddressRange(baseAddress,displayColumns*displayRows,1);
		
		t = new Timer(100, this);
		t.start();
		cursorTimer = new Timer(500, this);
		cursorTimer.start();


		this.setTitle("Display");
		this.setContentPane(p);
		this.setAlwaysOnTop(false);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public class DisplayPanel extends JPanel
	{
		public DisplayPanel()
		{
			try
			{
				lcdFont = FontManager.loadFont(FontManager.FONT_5x8_LCD);
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				ge.registerFont(lcdFont);
			}
			catch (Exception e)
			{
				System.out.println(e.toString());
			}
		}

		public void paintComponent(Graphics g)
		{
			//g.setColor(Color.getHSBColor(0.62f, 0.83f, 1f));
			g.setColor(Color.getHSBColor(0.62f, 0.87f, 0.78f));
			g.fillRect(0, 0, p.getWidth(), p.getHeight());
			g.setColor(Color.getHSBColor(0.62f, 0.87f, 0.78f));
			for (int i = 0; i < displayColumns ; i++)
			{
				for (int j = 0; j < displayRows; j++)
				{
					g.fillRect(12 + 33 * i, 25 + 50 * j, 30, 47);
				}
			}
			if (displayPower)
			{
				g.setColor(Color.white);
				g.setFont(lcdFont);
				for (int i = 0; i < displayColumns; i++)
				{
					for (int j = 0; j < displayRows; j++)
					{
						char dv = bank[i + j * 40];
						if(dv == 0)
							dv = ' ';
						g.drawString(String.valueOf(dv), 12 + 33 * i, 70 + 50 * j);
						if (i + j * 40 == cursorPos)
						{
							if (graphicalCursorBlinkFlag)
								g.fillRect(12 + 33 * i, 66 + 50 * j, 30, 5);
						}
					}
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (arg0.getSource().equals(t))
		{
			p.repaint();
		}
		if (arg0.getSource().equals(cursorTimer))
		{
			if (cursor)
			{
				if (cursorBlink)
				{
					graphicalCursorBlinkFlag = !graphicalCursorBlinkFlag;
				}
				else
				{
					graphicalCursorBlinkFlag = cursor;
				}
			}
			else
			{
				graphicalCursorBlinkFlag = false;
			}
		}
	}


	@Override
	public String getName()
	{
		return "DISPLAY";
	}

	@Override
	public BusAddressRange getBusAddressRange()
	{
		return this.bar;
	}

	@Override
	public void writeAddress(int address, int value, IOSize size)
	{
		bank[this.bar.getRelativeAddress(address)] = (char) value;
	}

	@Override
	public int readAddressSigned(int address, IOSize size)
	{
		return bank[this.bar.getRelativeAddress(address)];
	}

	@Override
	public int readAddressUnsigned(int address, IOSize size)
	{
		return bank[this.bar.getRelativeAddress(address)];
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
				
			System.out.print(BusAddressRange.makeHex((byte)(int)bank[i]));
			System.out.print(" ");
			bytes++;
			if(bytes>7)
			{				
				System.out.println();
				bytes = 0;
			}
		}
	}

	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		DisplayDevice display = new DisplayDevice(0x0000A000,40,10);
		System.out.println(display.getBusAddressRange().getLowAddressHex()+ ":" +  display.getBusAddressRange().getHighAddressHex());	
		
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		System.out.println("[ADDRESS] [CHAR]");

		while (true)
		{
			String input = scan.nextLine();

			String address  = input.substring(0,8);
			String asciiChar = input.substring(8);
			
			int location = Integer.parseInt(address,16);
			int asciiCode = (int)asciiChar.charAt(0);
						
			display.writeAddress(location, asciiCode, IOSize.IO8Bit);
			display.dumpContents(40);

		}
	}
	
}
