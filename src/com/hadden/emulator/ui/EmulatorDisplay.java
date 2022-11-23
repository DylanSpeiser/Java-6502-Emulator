package com.hadden.emulator.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.Timer;

import com.hadden.emu.BusListener;
import com.hadden.emu.CPU.Telemetry;
import com.hadden.emulator.Clock;
import com.hadden.emulator.DeviceDebugger;
import com.hadden.emulator.Emulator;
import com.hadden.emulator.util.Convert;

public class EmulatorDisplay extends JPanel implements ActionListener, KeyListener, BusListener, MouseWheelListener
{
	private boolean writeEvent = false;
	private Timer t;
	public int ramPage = 0;
	public int romPage = 0;

	int rightAlignHelper = Math.max(getWidth(), 1334);
	int historyOffset = 0;

	public String ramPageString = "";
	public String romPageString = "";
	
	private String title = "";

	private int defaultResetAddress = 0;
	private int defaultResetVector = 0xFFFC; 
	
	private Emulator emulator;
	private JFrame editorFrame;

	public EmulatorDisplay(Emulator emulator)
	{
		super(null);

		this.emulator = emulator;

		
		if(System.getProperty("emulator.resetJMP") != null)
		{
			String dra = System.getProperty("emulator.resetJMP");
			if(dra.startsWith("0x"))
			{
				dra = dra.replace("0x", "");
				defaultResetAddress = Integer.parseInt(dra,16);
			}
			else
				defaultResetAddress = Integer.parseInt(dra);
		}
		this.title = ((Emulator) emulator).getMainTitle() + " Emulator";

		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    String[] fonts = env.getAvailableFontFamilyNames();
		for(int i=0;i<fonts.length;i++)
		{
			System.out.println(fonts[i]);
		}
		
		t = new javax.swing.Timer(16, this);
		t.start();
		setBackground(Color.blue);
		setPreferredSize(new Dimension(1200, 900));

		// romPageString = SystemEmulator.rom.getROMString().substring(romPage * 960,
		// (romPage + 1) * 960);
		ramPageString = emulator.getBus().dumpBytesAsString().substring(ramPage * 960, (ramPage + 1) * 960);

		this.setFocusable(true);
		this.requestFocus();
		this.addKeyListener(this);
		this.addMouseWheelListener(this);

	}

	public void mouseWheelMoved(MouseWheelEvent e) 
	{
		int notches = e.getWheelRotation();		
		if (notches < 0)
	    {
		    	this.historyOffset -= 1;
		    	if(this.historyOffset < 0)
		    		this.historyOffset = 0;
		    	System.out.println("Offset:" + historyOffset);
	    }
	    else
	    {
		    	this.historyOffset += 1;
		    	System.out.println("Offset:" + historyOffset);
	    }

	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.setColor(Color.white);
		// g.drawString("Render Mode: paintComponent",5,15);

		// g.setColor(getBackground());
		// g.fillRect(0, 0, SystemEmulator.getWindows()[1].getWidth(),
		// SystemEmulator.getWindows()[1].getHeight());
		// g.setColor(Color.white);
		// g.drawString("Render Mode: fillRect",5,15);

		rightAlignHelper = Math.max(getWidth(), 1334);

		// Title
		g.setFont(new Font("Calibri Bold", 50, 50));
		// g.drawString("Ben Eater 6502 Emulator", 40, 50);
		g.drawString(title, 40, 50);

		// Version
		if(System.getProperty("os.name").toLowerCase().contains("windows"))
			g.setFont(new Font("Courier New Bold", Font.BOLD, 20));
		else
			g.setFont(new Font("Monospaced", Font.BOLD, 20));
		
		g.drawString("v" + emulator.getSystemVersion(), 7, 1033);

		Telemetry t = emulator.getCPU().getTelemetry();

		// Clocks
		g.drawString("Clocks: " + t.clocks, 40, 80);
		if (t.clocksPerSecond > 1000000.0)
		{
			g.drawString("Speed: " + (int) t.clocksPerSecond / 1000000 + " MHz"
					+ (emulator.getClock().isSlow() ? " (Slow)" : ""), 40, 110);
		}
		else
			g.drawString("Speed: " + (int) t.clocksPerSecond + " Hz" + (emulator.getClock().isSlow() ? " (Slow)" : ""),
					40, 110);

		// PAGE INDICATORS
		g.drawString("(K) <-- " + Convert.byteToHexString((byte) (romPage + 0x80)) + " --> (L)", rightAlignHelper - 304,
				Math.max(getHeight() - 91, 920));
		g.drawString("(H) <-- " + Convert.byteToHexString((byte) ramPage) + " --> (J)", rightAlignHelper - 704,
				Math.max(getHeight() - 91, 920));

		// Stack Pointer Underline
		if (ramPage == 1)
		{
			// g.setColor(new Color(0.7f, 0f, 0f));
			g.setColor(Color.red);
			g.fillRect(rightAlignHelper - 708 + 36 * (Byte.toUnsignedInt(t.stackPointer) % 8),
					156 + 23 * ((int) Byte.toUnsignedInt(t.stackPointer) / 8), 25, 22);
			g.setColor(Color.white);
		}

		// OPS
		g.drawString("Instructions", rightAlignHelper - 1215, 130);
		this.historyOffset = drawString(g, t.history, rightAlignHelper - 1365, 150, historyOffset);

		// g.drawLine(rightAlignHelper - 784, 150, rightAlignHelper - 784, 1000);

		// RAM
		g.drawString("RAM", rightAlignHelper - 624, 130);
		drawString(g, ramPageString, rightAlignHelper - 779, 150);

		// ROM
		g.drawString("ROM", rightAlignHelper - 214, 130);
		drawString(g, romPageString, rightAlignHelper - 379, 150);

		// CPU
		g.drawString("CPU Registers:", 50, 140);
		g.drawString("A: " + Convert.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(t.a)), 8) + " ("
				+ Convert.byteToHexString(t.a) + ")", 35, 170);
		g.drawString("X: " + Convert.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(t.x)), 8) + " ("
				+ Convert.byteToHexString(t.x) + ")", 35, 200);
		g.drawString("Y: " + Convert.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(t.y)), 8) + " ("
				+ Convert.byteToHexString(t.y) + ")", 35, 230);
		g.drawString(
				"Stack Pointer: "
						+ Convert.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(t.stackPointer)), 8)
						+ " (" + Convert.byteToHexString(t.stackPointer) + ")["
						+ Integer.toHexString(((int) t.stackPointer & 0x000000FF) + 0x00000100).toUpperCase() + "]",
				35, 260);
		g.drawString(
				"Program Counter: "
						+ Convert.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt(t.programCounter)), 16)
						+ " ("
						+ Convert.padStringWithZeroes(
								Integer.toHexString(Short.toUnsignedInt(t.programCounter)).toUpperCase(), 4)
						+ ")",
				35, 290);
		g.drawString("Flags:             (" + Convert.byteToHexString(t.flags) + ")", 35, 320);

		g.drawString(
				"Absolute Address: "
						+ Convert.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt(t.addressAbsolute)),
								16)
						+ " (" + Convert.byteToHexString((byte) ((short) t.addressAbsolute / 0xFF))
						+ Convert.byteToHexString((byte) t.addressAbsolute) + ")",
				35, 350);
		g.drawString("Relative Address: "
				+ Convert.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt((short) t.addressRelative)),
						16)
				+ " (" + Convert.byteToHexString((byte) ((short) t.addressRelative / 0xFF))
				+ Convert.byteToHexString((byte) t.addressRelative) + ")", 35, 380);
		g.drawString("Opcode: " + t.opcodeName + " (" + Convert.byteToHexString(t.opcode) + ")", 35, 410);
		g.drawString("Cycles: " + t.cycles, 35, 440);
		g.drawString("IRQs  : " + t.irqs, 35, 470);

		g.drawString("Reset JMP: ", 35, 500);
		g.drawString("0x" + Convert.toHex16String(defaultResetAddress), 160, 500);
		g.drawString("Reset Vector: ", 35, 530);
		g.drawString("0x" + Convert.toHex16String(defaultResetVector), 200, 530);
		
		int counter = 0;
		String flagsString = "NVUBDIZC";
		for (char c : Convert.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(t.flags)), 8).toCharArray())
		{
			g.setColor((c == '1') ? Color.green : Color.red);
			g.drawString(String.valueOf(flagsString.charAt(counter)), 120 + 16 * counter, 320);
			counter++;
		}

		g.setColor(Color.white);
		/*
		 * // VIA g.drawString("VIA Registers:", 50, 495); g.drawString("PORT A: " +
		 * ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(
		 * SystemEmulator.via.PORTA)), 8) + " (" +
		 * ROMLoader.byteToHexString(SystemEmulator.via.PORTA) + ")", 35, 520);
		 * g.drawString("PORT B: " +
		 * ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(
		 * SystemEmulator.via.PORTB)), 8) + " (" +
		 * ROMLoader.byteToHexString(SystemEmulator.via.PORTB) + ")", 35, 550);
		 * g.drawString("DDR  A: " +
		 * ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(
		 * SystemEmulator.via.DDRA)), 8) + " (" +
		 * ROMLoader.byteToHexString(SystemEmulator.via.DDRA) + ")", 35, 580);
		 * g.drawString("DDR  B: " +
		 * ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(
		 * SystemEmulator.via.DDRB)), 8) + " (" +
		 * ROMLoader.byteToHexString(SystemEmulator.via.DDRB) + ")", 35, 610);
		 * g.drawString("   PCR: " +
		 * ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(
		 * SystemEmulator.via.PCR)), 8) + " (" +
		 * ROMLoader.byteToHexString(SystemEmulator.via.PCR) + ")", 35, 640);
		 * g.drawString("   IFR: " +
		 * ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(
		 * SystemEmulator.via.IFR)), 8) + " (" +
		 * ROMLoader.byteToHexString(SystemEmulator.via.IFR) + ")", 35, 670);
		 * g.drawString("   IER: " +
		 * ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(
		 * SystemEmulator.via.IER)), 8) + " (" +
		 * ROMLoader.byteToHexString(SystemEmulator.via.IER) + ")", 35, 700);
		 * 
		 */
		// Controls
		g.drawString("Controls:", 50, 750);
		g.drawString("C - Toggle Clock", 35, 780);
		g.drawString("Space - Pulse Clock", 35, 810);
		g.drawString("R - Reset System", 35, 840);
		g.drawString("P - Reset CPU", 35, 870);
		g.drawString("S - Toggle Slower " + (emulator.getClock().isSlow() ? "Disable" : "Enable"), 35, 900);
		g.drawString("I - Toggle Interrupt "+ 
		             (((DeviceDebugger) emulator.getCPU()).isEnabled("interrupt-hold") ? "Enable" : "Disable"),
				     35, 930);
		
		if (!emulator.getClock().isEnabled())
			g.drawString("Cursors(Wheel) - Scroll History", 35, 960);
		else
			g.drawString("Cursors(Wheel) - Scroll History Disabled", 35, 960);
		
		g.drawString("< & > - Default Reset Address", 35, 990);
	}

	public static void drawString(Graphics g, String text, int x, int y)
	{
		Color c = g.getColor();
		for (String line : text.split("\n"))
		{
			// g.drawString(line, x, y += g.getFontMetrics().getHeight());

			if (line.length() > 0)
			{
				String[] part = line.split(":");
				try
				{
					g.setColor(Color.CYAN);
					g.drawString(part[0] + ":", x, y + g.getFontMetrics().getHeight());
					g.setColor(c);
					g.drawString(part[1], x + g.getFontMetrics().charWidth('0') * 5,
							y += g.getFontMetrics().getHeight());
				} catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	public static int drawString(Graphics g, java.util.List<String> list, int x, int y, int offset)
	{
		int bound = 32;
		int size = 0;
		if (list != null)
		{
			size = list.size();
			if (size > 0)
			{
				if (offset < 0)
					offset = 0;

				if (bound > size)
					bound = 0;

				int top = size - bound - offset;
				if (top < 0)
					top = 0;

				int bottom = size - offset;
				if (bottom > size)
					bottom = size;

				try
				{
					for (int i = top; i < bottom; i++)
						g.drawString(list.get(i), x, y += g.getFontMetrics().getHeight());
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		if (offset < 0)
			return 0;
		if (offset > size)
			return size;

		return offset;

		// for (String line : text.split("\n"))
		// g.drawString(line, x, y += g.getFontMetrics().getHeight());
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource().equals(t))
		{
			if (this.writeEvent)
			{
				// System.out.println("UPDATE PAGE:" + ramPage);
				ramPageString = emulator.getBus().dumpBytesAsString().substring(ramPage * 960, (ramPage + 1) * 960);
				writeEvent = false;
			}
			this.repaint();
		}
	}

	@Override
	public void readListener(short address)
	{
	}

	@Override
	public void writeListener(short address, byte data)
	{
		int updatePage = ((0x0000FFFF & (address - 1)) / 0xFF);
		// System.out.println("PAGE[" + Integer.toHexString(0x0000FFFF & address) + "]:"
		// + updatePage);
		if (ramPage == updatePage)
			this.writeEvent = true;
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_DOWN)
		{
			this.historyOffset -= 1;
			if (this.historyOffset < 0)
				this.historyOffset = 0;
			System.out.println("Offset:" + historyOffset);
			// this.repaint();
		}
		else if (keyCode == KeyEvent.VK_UP)
		{
			this.historyOffset += 1;
			// this.repaint();
			System.out.println("Offset:" + historyOffset);
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0)
	{

	}

	@Override
	public void keyTyped(KeyEvent arg0)
	{
		Clock c = this.emulator.getClock();

		switch (arg0.getKeyChar())
		{
		case 'e':
			
			if(editorFrame == null)
			{
				editorFrame = SystemCodeEditor.createAndShowGUI();
				
				editorFrame.addWindowListener(new WindowAdapter()
				{
				    @Override
				    public void windowClosing(WindowEvent e)
				    {
				      System.out.println("Code Window Closed");
				      e.getWindow().dispose();
				      editorFrame = null;
				    }
				});
			}
			break;
		case 'c':
			c.setEnabled(!c.isEnabled());
			this.historyOffset = 0;
			break;
		case ' ':
			c.pulse();
			break;
		case 's':
			c.setSlow(!c.isSlow());
			break;
		case 'i':
			if (emulator.getCPU() instanceof DeviceDebugger)
			{
				((DeviceDebugger) emulator.getCPU()).setEnabled("interrupt-hold",
						!((DeviceDebugger) emulator.getCPU()).isEnabled("interrupt-hold"));
			}
			break;
		case 'r':
			emulator.reset();
			// ramPageString = SystemEmulator.getBus().dumpBytesAsString().substring(ramPage
			// * 960, (ramPage + 1) * 960);

			System.out.println("Size: " + this.getWidth() + " x " + this.getHeight());
			break;
		case 'p':
			emulator.getCPU().reset();
			break;
		case 'j':
			if (ramPage < 0xFF)
			{
				ramPage += 1;
				// ramPageString = SystemEmulator.ram.getRAMString().substring(ramPage * 960,
				// (ramPage + 1) * 960);
				ramPageString = emulator.getBus().dumpBytesAsString().substring(ramPage * 960, (ramPage + 1) * 960);
			}
			break;
		case 'h':
			if (ramPage > 0)
			{
				ramPage -= 1;
				// ramPageString = SystemEmulator.ram.getRAMString().substring(ramPage * 960,
				// (ramPage + 1) * 960);
				ramPageString = emulator.getBus().dumpBytesAsString().substring(ramPage * 960, (ramPage + 1) * 960);
			}
			break;

		case '<':
			defaultResetAddress--;
			if (defaultResetAddress < 0)
			{
				defaultResetAddress = 0x0000FFFF;
			}
			
			emulator.getBus().write((short)defaultResetVector,  (byte)((defaultResetAddress) & 0x000000FF));
			emulator.getBus().write((short)(defaultResetVector+1),  (byte)((defaultResetAddress >> 8) & 0x000000FF));
			
			System.out.print("0x" + Convert.toHex8String(emulator.getBus().read((short)(defaultResetVector+1))));
			System.out.println(Convert.toHex8String(emulator.getBus().read((short)defaultResetVector)));
			
			break;
		case '>':
			defaultResetAddress++;
			if (defaultResetAddress > 0x0000FFFF)
			{
				defaultResetAddress = 0x00000000;
			}

			emulator.getBus().write((short)defaultResetVector,  (byte)((defaultResetAddress) & 0x000000FF));
			emulator.getBus().write((short)(defaultResetVector+1),  (byte)((defaultResetAddress >> 8) & 0x000000FF));
			
			
			System.out.print("0x" + Convert.toHex8String(emulator.getBus().read((short)(defaultResetVector+1))));
			System.out.println(Convert.toHex8String(emulator.getBus().read((short)defaultResetVector)));
			
			break;
		case ',':
			defaultResetVector--;
			if (defaultResetVector < 0)
			{
				defaultResetVector = 0x0000FFFF;
			}
			
			
			break;
		case '.':
			defaultResetVector++;
			if (defaultResetVector > 0x0000FFFF)
			{
				defaultResetVector = 0x00000000;
			}
			break;

		default:
			System.out.println("Key:" + arg0.getKeyCode() + ":" + arg0.getKeyChar());
			break;

		}
		/*
		 * switch (arg0.getKeyChar()) { case 'l': if (romPage < 0x80) { romPage += 1;
		 * romPageString = SystemEmulator.getBus().dumpBytesAsString().substring(romPage
		 * * 960, (romPage + 1) * 960); } break; case 'k': if (romPage > 0) { romPage -=
		 * 1; //romPageString = SystemEmulator.rom.getROMString().substring(romPage *
		 * 960, (romPage + 1) * 960); romPageString =
		 * SystemEmulator.getBus().dumpBytesAsString().substring(ramPage * 960, (ramPage
		 * + 1) * 960); } break; case 'j': if (ramPage < 0xFF) { ramPage += 1;
		 * //ramPageString = SystemEmulator.ram.getRAMString().substring(ramPage * 960,
		 * (ramPage + 1) * 960); ramPageString =
		 * SystemEmulator.getBus().dumpBytesAsString().substring(ramPage * 960, (ramPage
		 * + 1) * 960); } break; case 'h': if (ramPage > 0) { ramPage -= 1;
		 * //ramPageString = SystemEmulator.ram.getRAMString().substring(ramPage * 960,
		 * (ramPage + 1) * 960); ramPageString =
		 * SystemEmulator.getBus().dumpBytesAsString().substring(ramPage * 960, (ramPage
		 * + 1) * 960); } break; case 'r': SystemEmulator.cpu.reset();
		 * //SystemEmulator.lcd.reset(); SystemEmulator.via = new VIA();
		 * //SystemEmulator.ram = new RAM(); SystemEmulator.ram.reset(); //ramPageString
		 * = SystemEmulator.ram.getRAMString().substring(ramPage * 960, (ramPage + 1) *
		 * 960); ramPageString =
		 * SystemEmulator.getBus().dumpBytesAsString().substring(ramPage * 960, (ramPage
		 * + 1) * 960);
		 * 
		 * System.out.println("Size: " + this.getWidth() + " x " + this.getHeight());
		 * break; case ' ': SystemEmulator.cpu.clock(); break; case 'c':
		 * SystemEmulator.clockState = !SystemEmulator.clockState; break; case 's':
		 * SystemEmulator.slowerClock = !SystemEmulator.slowerClock; break; case 'i':
		 * SystemEmulator.via.CA1(); break; }
		 */
	}

}
