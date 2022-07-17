package com.hadden;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.*;

public class LCD extends JFrame implements ActionListener
{
	LCDPanel p = new LCDPanel();
	Timer t;
	Timer cursorTimer;
	Font lcdFont;
	Scanner s;

	boolean graphicalCursorBlinkFlag = false;

	boolean debug = true;

	// Internal flags
	int cursorPos = 0;
	boolean increment = true;
	boolean displayPower = false;
	boolean cursor = false;
	boolean cursorBlink = false;
	boolean fourBitMode = false;

	char[] text = new char[0x50];

	public LCD()
	{
		this.setSize(565, 185);
		t = new Timer(100, this);
		t.start();
		cursorTimer = new Timer(500, this);
		cursorTimer.start();

		String s = "";

		for (int i = 0; i < 0x50; i++)
		{
			if (i < s.length())
			{
				text[i] = s.charAt(i);
			}
			else
			{
				text[i] = ' ';
			}
		}

		this.setTitle("LCD");
		this.setContentPane(p);
		this.setAlwaysOnTop(false);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		LCD lcd = new LCD();

		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		System.out.println("[RS] [data]");

		while (true)
		{
			String input = scan.nextLine();

			boolean rs = input.charAt(0) == '1' ? true : false;
			byte data = Byte.parseByte(input.substring(2, 10), 2);

			System.out.println(rs ? "Data" : "Instruction" + ": 0x" + ROMLoader.byteToHexString(data));

			lcd.write(rs, data);
		}
	}

	public void reset()
	{
		String s = "";

		for (int i = 0; i < 0x50; i++)
		{
			if (i < s.length())
			{
				text[i] = s.charAt(i);
			}
			else
			{
				text[i] = ' ';
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
						System.out.println(
								"Turned the Display off! | " + Integer.toBinaryString(Byte.toUnsignedInt(data)));
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
					System.out.println("Display Control: Power: " + displayPower + " Cursor: " + cursor + " Blink: "
							+ cursorBlink);
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
				text = new char[0x50];
				for (int i = 0; i < 0x50; i++)
				{
					text[i] = ' ';
				}
				if (debug)
					System.out.println("Cleared!");
			}
		}
		else
		{
			// DATA
			text[cursorPos] = (char) data;
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
				lcdFont = Font.createFont(Font.TRUETYPE_FONT, new File("5x8_lcd_hd44780u_a02.ttf")).deriveFont(47f);
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				ge.registerFont(lcdFont);
			} catch (Exception e)
			{
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
						g.drawString(String.valueOf(text[i + j * 40]), 12 + 33 * i, 70 + 50 * j);
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
}
