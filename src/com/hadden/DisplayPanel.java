package com.hadden;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.hadden.emu.VIA;

public class DisplayPanel extends JPanel implements ActionListener, KeyListener
{
	Timer t;
	public int ramPage = 0;
	public int romPage = 0;

	int rightAlignHelper = Math.max(getWidth(), 1334);

	public String ramPageString = "";
	public String romPageString = "";
	private String title = "";

	public DisplayPanel(String title)
	{
		super(null);

		this.title  = title;	
		t = new javax.swing.Timer(16, this);
		t.start();
		setBackground(Color.blue);
		setPreferredSize(new Dimension(1200, 900));

		romPageString = SystemEmulator.rom.ROMString.substring(romPage * 960, (romPage + 1) * 960);
		ramPageString = SystemEmulator.ram.getRAMString().substring(ramPage * 960, (ramPage + 1) * 960);

		this.setFocusable(true);
		this.requestFocus();
		this.addKeyListener(this);
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.setColor(Color.white);
		// g.drawString("Render Mode: paintComponent",5,15);

//		g.setColor(getBackground());
//		g.fillRect(0, 0, SystemEmulator.getWindows()[1].getWidth(), SystemEmulator.getWindows()[1].getHeight());
//      g.setColor(Color.white);
//      g.drawString("Render Mode: fillRect",5,15);

		rightAlignHelper = Math.max(getWidth(), 1334);

		// Title
		g.setFont(new Font("Calibri Bold", 50, 50));
		//g.drawString("Ben Eater 6502 Emulator", 40, 50);
		g.drawString(title, 40, 50);
		

		// Version
		g.setFont(new Font("Courier New Bold", 20, 20));
		g.drawString("v" + SystemEmulator.versionString, 7, 1033);

		// Clocks
		g.drawString("Clocks: " + SystemEmulator.clocks, 40, 80);
		g.drawString(
				"Speed: " + SystemEmulator.cpu.ClocksPerSecond + " Hz" + (SystemEmulator.slowerClock ? " (Slow)" : ""),
				40, 110);

		// PAGE INDICATORS
		g.drawString("(K) <-- " + ROMLoader.byteToHexString((byte) (romPage + 0x80)) + " --> (L)",
				rightAlignHelper - 304, Math.max(getHeight() - 91, 920));
		g.drawString("(H) <-- " + ROMLoader.byteToHexString((byte) ramPage) + " --> (J)", rightAlignHelper - 704,
				Math.max(getHeight() - 91, 920));

		// ROM
		g.drawString("ROM", rightAlignHelper - 214, 130);
		drawString(g, romPageString, rightAlignHelper - 379, 150);

		// Stack Pointer Underline
		if (ramPage == 1)
		{
			g.setColor(new Color(0.7f, 0f, 0f));
			g.fillRect(rightAlignHelper - 708 + 36 * (Byte.toUnsignedInt(SystemEmulator.cpu.stackPointer) % 8),
					156 + 23 * ((int) Byte.toUnsignedInt(SystemEmulator.cpu.stackPointer) / 8), 25, 22);
			g.setColor(Color.white);
		}

		// RAM
		g.drawString("RAM", rightAlignHelper - 624, 130);
		drawString(g, ramPageString, rightAlignHelper - 779, 150);

		// CPU
		g.drawString("CPU Registers:", 50, 140);
		g.drawString("A: "
				+ ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(SystemEmulator.cpu.a)), 8)
				+ " (" + ROMLoader.byteToHexString(SystemEmulator.cpu.a) + ")", 35, 170);
		g.drawString("X: "
				+ ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(SystemEmulator.cpu.x)), 8)
				+ " (" + ROMLoader.byteToHexString(SystemEmulator.cpu.x) + ")", 35, 200);
		g.drawString("Y: "
				+ ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(SystemEmulator.cpu.y)), 8)
				+ " (" + ROMLoader.byteToHexString(SystemEmulator.cpu.y) + ")", 35, 230);
		g.drawString("Stack Pointer: "
				+ ROMLoader.padStringWithZeroes(
						Integer.toBinaryString(Byte.toUnsignedInt(SystemEmulator.cpu.stackPointer)), 8)
				+ " (" + ROMLoader.byteToHexString(SystemEmulator.cpu.stackPointer) + ")", 35, 260);
		g.drawString(
				"Program Counter: "
						+ ROMLoader.padStringWithZeroes(
								Integer.toBinaryString(Short.toUnsignedInt(SystemEmulator.cpu.programCounter)), 16)
						+ " ("
						+ ROMLoader.padStringWithZeroes(Integer
								.toHexString(Short.toUnsignedInt(SystemEmulator.cpu.programCounter)).toUpperCase(), 4)
						+ ")",
				35, 290);
		g.drawString("Flags:             (" + ROMLoader.byteToHexString(SystemEmulator.cpu.flags) + ")", 35, 320);

		g.drawString("Absolute Address: "
				+ ROMLoader.padStringWithZeroes(
						Integer.toBinaryString(Short.toUnsignedInt(SystemEmulator.cpu.addressAbsolute)), 16)
				+ " (" + ROMLoader.byteToHexString((byte) (SystemEmulator.cpu.addressAbsolute / 0xFF))
				+ ROMLoader.byteToHexString((byte) SystemEmulator.cpu.addressAbsolute) + ")", 35, 350);
		g.drawString("Relative Address: "
				+ ROMLoader.padStringWithZeroes(
						Integer.toBinaryString(Short.toUnsignedInt(SystemEmulator.cpu.addressRelative)), 16)
				+ " (" + ROMLoader.byteToHexString((byte) (SystemEmulator.cpu.addressRelative / 0xFF))
				+ ROMLoader.byteToHexString((byte) SystemEmulator.cpu.addressRelative) + ")", 35, 380);
		g.drawString("Opcode: " + SystemEmulator.cpu.lookup[Byte.toUnsignedInt(SystemEmulator.cpu.opcode)] + " ("
				+ ROMLoader.byteToHexString(SystemEmulator.cpu.opcode) + ")", 35, 410);
		g.drawString("Cycles: " + SystemEmulator.cpu.cycles, 35, 440);

		int counter = 0;
		String flagsString = "NVUBDIZC";
		for (char c : ROMLoader
				.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(SystemEmulator.cpu.flags)), 8)
				.toCharArray())
		{
			g.setColor((c == '1') ? Color.green : Color.red);
			g.drawString(String.valueOf(flagsString.charAt(counter)), 120 + 16 * counter, 320);
			counter++;
		}

		g.setColor(Color.white);
		// VIA
		g.drawString("VIA Registers:", 50, 490);
		g.drawString("PORT A: "
				+ ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(SystemEmulator.via.PORTA)), 8)
				+ " (" + ROMLoader.byteToHexString(SystemEmulator.via.PORTA) + ")", 35, 520);
		g.drawString("PORT B: "
				+ ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(SystemEmulator.via.PORTB)), 8)
				+ " (" + ROMLoader.byteToHexString(SystemEmulator.via.PORTB) + ")", 35, 550);
		g.drawString("DDR  A: "
				+ ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(SystemEmulator.via.DDRA)), 8)
				+ " (" + ROMLoader.byteToHexString(SystemEmulator.via.DDRA) + ")", 35, 580);
		g.drawString("DDR  B: "
				+ ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(SystemEmulator.via.DDRB)), 8)
				+ " (" + ROMLoader.byteToHexString(SystemEmulator.via.DDRB) + ")", 35, 610);
		g.drawString("   PCR: "
				+ ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(SystemEmulator.via.PCR)), 8)
				+ " (" + ROMLoader.byteToHexString(SystemEmulator.via.PCR) + ")", 35, 640);
		g.drawString("   IFR: "
				+ ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(SystemEmulator.via.IFR)), 8)
				+ " (" + ROMLoader.byteToHexString(SystemEmulator.via.IFR) + ")", 35, 670);
		g.drawString("   IER: "
				+ ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(SystemEmulator.via.IER)), 8)
				+ " (" + ROMLoader.byteToHexString(SystemEmulator.via.IER) + ")", 35, 700);

		// Controls
		g.drawString("Controls:", 50, 750);
		g.drawString("C - Toggle Clock", 35, 780);
		g.drawString("Space - Pulse Clock", 35, 810);
		g.drawString("R - Reset", 35, 840);
		g.drawString("S - Toggle Slower Clock", 35, 870);
		g.drawString("I - Trigger VIA CA1", 35, 900);
	}

	public static void drawString(Graphics g, String text, int x, int y)
	{
		for (String line : text.split("\n"))
			g.drawString(line, x, y += g.getFontMetrics().getHeight());
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource().equals(t))
		{
			ramPageString = SystemEmulator.ram.getRAMString().substring(ramPage * 960, (ramPage + 1) * 960);
			SystemEmulator.ROMopenButton.setBounds(rightAlignHelper - 150, 15, 125, 25);
			SystemEmulator.RAMopenButton.setBounds(rightAlignHelper - 150, 45, 125, 25);
			this.repaint();
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0)
	{

	}

	@Override
	public void keyReleased(KeyEvent arg0)
	{

	}

	@Override
	public void keyTyped(KeyEvent arg0)
	{
		switch (arg0.getKeyChar())
		{
		case 'l':
			if (romPage < 0x80)
			{
				romPage += 1;
				romPageString = SystemEmulator.rom.ROMString.substring(romPage * 960, (romPage + 1) * 960);
			}
			break;
		case 'k':
			if (romPage > 0)
			{
				romPage -= 1;
				romPageString = SystemEmulator.rom.ROMString.substring(romPage * 960, (romPage + 1) * 960);
			}
			break;
		case 'j':
			if (ramPage < 0x80)
			{
				ramPage += 1;
				ramPageString = SystemEmulator.ram.getRAMString().substring(ramPage * 960, (ramPage + 1) * 960);
			}
			break;
		case 'h':
			if (ramPage > 0)
			{
				ramPage -= 1;
				ramPageString = SystemEmulator.ram.getRAMString().substring(ramPage * 960, (ramPage + 1) * 960);
			}
			break;
		case 'r':
			SystemEmulator.cpu.reset();
			//SystemEmulator.lcd.reset();
			SystemEmulator.via = new VIA();
			//SystemEmulator.ram = new RAM();
			SystemEmulator.ram.reset();
			ramPageString = SystemEmulator.ram.getRAMString().substring(ramPage * 960, (ramPage + 1) * 960);
			System.out.println("Size: " + this.getWidth() + " x " + this.getHeight());
			break;
		case ' ':
			SystemEmulator.cpu.clock();
			break;
		case 'c':
			SystemEmulator.clockState = !SystemEmulator.clockState;
			break;
		case 's':
			SystemEmulator.slowerClock = !SystemEmulator.slowerClock;
			break;
		case 'i':
			SystemEmulator.via.CA1();
			break;
		}
	}
}
