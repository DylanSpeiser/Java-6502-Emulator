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

public class LCDDevice extends JFrame implements BusDevice, ActionListener
{
	LCDPanel p = new LCDPanel();
	Timer t;
	Timer cursorTimer;
	Font lcdFont;
	Scanner s;

	boolean graphicalCursorBlinkFlag = false;

	boolean debug = false;
	
	
	// Internal flags
	int cursorPos = 0;
	boolean increment = true;
	boolean displayPower = false;
	boolean cursor = false;
	boolean cursorBlink = false;
	boolean fourBitMode = false;

	BusAddressRange bar = null;
	char[] bank = new char[0x50];
	boolean rs = false;

	public LCDDevice(int portAddress)
	{
		this.setSize(565, 185);
		t = new Timer(100, this);
		t.start();
		cursorTimer = new Timer(500, this);
		cursorTimer.start();

		this.bar = new BusAddressRange(portAddress,2,1);
		
		String s = "";

		for (int i = 0; i < 0x50; i++)
		{
			if (i < s.length())
			{
				bank[i] = s.charAt(i);
			}
			else
			{
				bank[i] = ' ';
			}
		}

		this.setTitle("LCD");
		this.setContentPane(p);
		this.setAlwaysOnTop(false);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}


	public void reset()
	{
		String s = "";

		for (int i = 0; i < 0x50; i++)
		{
			if (i < s.length())
			{
				bank[i] = s.charAt(i);
			}
			else
			{
				bank[i] = ' ';
			}
		}

		graphicalCursorBlinkFlag = false;

		cursorPos = 0;
		increment = true;
		displayPower = false;
		cursor = false;
		cursorBlink = false;
		fourBitMode = false;

	}

	// Regular write to LCD
	public void write(boolean regSel, byte data)
	{
		if (regSel == false)
		{
			//System.out.println("regSel == false");
			// INSTRUCTION
			if ((data & 0b00100000) == 0b00100000)
			{
				// FUNCTION SET
				if ((data & 0b00010000) == 0b00010000)
				{
					fourBitMode = false;
				}
				else
				{
					fourBitMode = true;
				}
				if (debug)
					System.out.println("Function: Four Bit Mode: " + fourBitMode);
			}
			else if ((data & 0b00010000) == 0b00010000)
			{
				// SHIFT
				boolean rightleft = false;
				if ((data & 0b00000100) == 0b00000100)
				{
					// RIGHT
					rightleft = true;
				}
				else
				{
					// LEFT
					rightleft = false;
				}
				if ((data & 0b00001000) == 0b00001000)
				{
					// SCREEN
					char[] newCharArray = new char[40];
					Arrays.fill(newCharArray, ' ');
					if (debug)
						System.out.println("Shifted screen to the " + (rightleft ? "right." : "left."));
				}
				else
				{
					// CURSOR
					cursorPos += (rightleft ? 1 : -1);
					if (cursorPos < 0)
						cursorPos = 0;
					if (debug)
					{
						System.out.println("Shifted cursor to the " + (rightleft ? "right." : "left."));
						System.out.println("CursorPos: " + cursorPos);
					}
				}
			}
			else if ((data & 0b00001000) == 0b00001000)
			{
				// DISPLAY CONTROL
				if ((data & 0b00000100) == 0b00000100)
				{
					displayPower = true;
				}
				else
				{
					displayPower = false;
					if (debug)
						System.out.println("Turned the Display off! | " + Integer.toBinaryString(Byte.toUnsignedInt(data)));
				}
				if ((data & 0b00000010) == 0b00000010)
				{
					cursor = true;
				}
				else
				{
					cursor = false;
				}
				if ((data & 0b00000001) == 0b00000001)
				{
					cursorBlink = true;
				}
				else
				{
					cursorBlink = false;
				}
				if (debug)
					System.out.println("Display Control: Power: " + displayPower + " Cursor: " + cursor + " Blink: " + cursorBlink);
			}
			else if ((data & 0b00000100) == 0b00000100)
			{
				// ENTRY MODE SET
				if ((data & 0b00000010) == 0b00000010)
				{
					increment = true;
				}
				else
				{
					increment = false;
				}
				if (debug)
					System.out.println("Set Entry Mode: Increment: " + increment);
			}
			else if ((data & 0b00000010) == 0b00000010)
			{
				// RETURN HOME
				cursorPos = 0;
				if (debug)
					System.out.println("Return Home");
			}
			else if (data == 0b00000001)
			{
				// CLEAR
				cursorPos = 0;
				bank = new char[0x50];
				for (int i = 0; i < 0x50; i++)
				{
					bank[i] = ' ';
				}
				if (debug)
					System.out.println("Cleared!");
			}
		}
		else
		{
			//System.out.println("regSel == true");
			// DATA
			bank[cursorPos] = (char) data;
			int prevCursorPos = cursorPos;
			cursorPos += increment ? 1 : -1;
			if (debug)
				System.out.println("Data: Wrote " + (char) data + " at " + prevCursorPos);
		}
	}

	// Read from LCD
	public byte read(boolean regSel)
	{
		return 0x0;
	}

	public class LCDPanel extends JPanel
	{
		public LCDPanel()
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
			g.setColor(Color.getHSBColor(0.62f, 0.83f, 1f));
			g.fillRect(0, 0, p.getWidth(), p.getHeight());
			g.setColor(Color.getHSBColor(0.62f, 0.87f, 0.78f));
			for (int i = 0; i < 16; i++)
			{
				for (int j = 0; j < 2; j++)
				{
					g.fillRect(12 + 33 * i, 25 + 50 * j, 30, 47);
				}
			}
			if (displayPower)
			{
				g.setColor(Color.white);
				g.setFont(lcdFont);
				for (int i = 0; i < 16; i++)
				{
					for (int j = 0; j < 2; j++)
					{
						g.drawString(String.valueOf(bank[i + j * 40]), 12 + 33 * i, 70 + 50 * j);
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
		return "LCD";
	}

	@Override
	public BusAddressRange getBusAddressRange()
	{
		return this.bar;
	}

	@Override
	public void writeAddress(int address, int value, IOSize size)
	{
		if(address == bar.getLowAddress())
		{
			 this.rs = (value == 1 ? true : false); 
		}
			 
		if(address == bar.getHighAddress())
		{
			 write(this.rs,(byte)value);
			 rs = false;
		}

		//System.out.println("rs enabled = " + Boolean.toString(rs) );
		
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
	
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		LCDDevice lcd = new LCDDevice(0x0000B000);
		System.out.println(lcd.getBusAddressRange().getLowAddressHex()+ ":" +  lcd.getBusAddressRange().getHighAddressHex());
		
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		System.out.println("[Address] [data]");

		lcd.writeAddress(0x0000b000, 0, IOSize.IO8Bit);
		lcd.writeAddress(0x0000b001, 15, IOSize.IO8Bit);
		for(int i=0;i<32;i++)
		{
			lcd.writeAddress(0x0000b000, 1, IOSize.IO8Bit);
			lcd.writeAddress(0x0000b001, 65+i, IOSize.IO8Bit);		
		}
		while (true)
		{
			String input = scan.nextLine();

			String address  = input.substring(0,8);

			String asciiChar = input.substring(8);
			
			int location = Integer.parseInt(address,16);
			int value = Integer.parseInt(asciiChar);
						
			lcd.writeAddress(location, value, IOSize.IO8Bit);
			//lcd.writeAddress(0x0000B001, data, IOSize.IO8Bit);
			
		}
	}

}
