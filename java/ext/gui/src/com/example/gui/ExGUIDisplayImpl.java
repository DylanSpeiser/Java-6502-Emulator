package com.example.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.Timer;


import com.juse.emulator.debug.DebugControl;
import com.juse.emulator.debug.DebugListener;
import com.juse.emulator.interfaces.AddressMap;
import com.juse.emulator.interfaces.BusListener;
import com.juse.emulator.interfaces.Clock;
import com.juse.emulator.interfaces.DeviceDebugger;
import com.juse.emulator.interfaces.Emulator;
import com.juse.emulator.interfaces.IOSize;
import com.juse.emulator.interfaces.Telemetry;
import com.juse.emulator.interfaces.TelemetryInfo;
import com.juse.emulator.interfaces.ui.EmulatorDisplay;
import com.juse.emulator.util.translate.Convert;


public class ExGUIDisplayImpl extends JPanel implements EmulatorDisplay, ActionListener, KeyListener, BusListener, MouseWheelListener
{
	private boolean writeEvent = false;
	private Timer t;
	public int ramPage = 0;
	public int romPage = 0;

	int rightAlignHelper = Math.max(getWidth(), 1334);
	int historyOffset = 0;

	public String ramPageString = "";
	public String romPageString = "";
	
	private int memX = 0;
	private int memY = 0;
	private char[] memHILO = new char[2];
	private int memHILOIndex = 1;
	private int  memAddress = 0;
	private boolean bMemoryEdit = false;
	private boolean bMemoryEnter = false;
	private boolean bMemChange = false;
	
	private String title = "";

	private int defaultResetAddress = 0;
	private int defaultResetVector = 0xFFFC; 
	
	private Emulator emulator;
	private JFrame editorFrame;

	private Map<Integer,String> debugBreaks = new HashMap<Integer,String>();

	private boolean bDebugMode = false;
	
	public interface ApplicationEvent 
	{
	}

	public interface ApplicationEventHandler 
	{
	}
	
	public ExGUIDisplayImpl(Emulator emulator)
	{
		super(null);

		memHILO[0] = ' ';
		memHILO[1] = ' ';
		
		this.emulator = emulator;
		
		//
		// manually setting breaks, needs to be UI driven
		//
		//debugBreaks.put((int)((short)0xFDA3),"0xFDA3");
		//debugBreaks.put((int)((short)0xFD15),"0xFD15");
		//debugBreaks.put((int)((short)0xA000),"0xA000");
		
		
		if(emulator.getCPU() instanceof DebugControl)
		{
			((DebugControl)(emulator.getCPU())).addStepListener(new DebugListener()
			{
				@Override
				public DebugCode debugEvent(DebugReason dr, int data, IOSize size)
				{
					return DebugCode.None;
				}

				@Override
				public DebugCode debugEvent(DebugReason dr, byte data, IOSize size)
				{
					// TODO Auto-generated method stub
					return DebugCode.None;
				}

				@Override
				public DebugCode debugEvent(DebugReason dr, short data, IOSize size)
				{
					if(bDebugMode &&  debugBreaks.containsKey((int)data))
					{
						System.out.println("CPU STEP");
						emulator.getClock().setEnabled(false);
						repaint();
					}
					return DebugCode.None;
				}

				@Override
				public DebugCode debugEvent(DebugReason dr, long data, IOSize size)
				{
					// TODO Auto-generated method stub
					return DebugCode.None;
				}
			});
			((DebugControl)(emulator.getCPU())).enable();
		}
		
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
		this.title = ((Emulator) emulator).getMainTitle() + " Emulator v" + emulator.getSystemVersion();

		/*
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    String[] fonts = env.getAvailableFontFamilyNames();
		for(int i=0;i<fonts.length;i++)
		{
			System.out.println(fonts[i]);
		}
		*/
		t = new javax.swing.Timer(16, this);
		t.start();
		setBackground(Color.green);
		setPreferredSize(new Dimension(1200, 900));

		// romPageString = SystemEmulator.rom.getROMString().substring(romPage * 960,
		// (romPage + 1) * 960);
		ramPageString = emulator.getBus().dumpBytesAsString().substring(ramPage * 960, (ramPage + 1) * 960);

		this.setFocusable(true);
		this.requestFocus();
		this.addKeyListener(this);
		this.addMouseWheelListener(this);

	}
	
	public void refreshBus()
	{
		ramPage = 0;
		ramPageString = emulator.getBus().dumpBytesAsString().substring(ramPage * 960, (ramPage + 1) * 960);
		this.repaint();
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
		g.setFont(new Font("Calibri Bold", 50, 38));
		// g.drawString("Ben Eater 6502 Emulator", 40, 50);
		g.drawString(title, 40, 50);

		// Version
		if(System.getProperty("os.name").toLowerCase().contains("windows"))
			g.setFont(new Font("Courier New Bold", Font.BOLD, 20));
		else
			g.setFont(new Font("Monospaced", Font.BOLD, 20));
		

		TelemetryInfo ti = null;
		Telemetry t = emulator.getCPU().getTelemetry();
		if(t instanceof TelemetryInfo)
		{
			ti = (TelemetryInfo)t;
		}

		
		
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
		//g.drawString("(K) <-- " + Convert.byteToHexString((byte) (romPage + 0x80)) + " --> (L)", rightAlignHelper - 304,
		//		Math.max(getHeight() - 91, 920));
		//g.drawString("(H) <-- " + Convert.byteToHexString((byte) ramPage) + " --> (J)", rightAlignHelper - 704,
		//		Math.max(getHeight() - 91, 920));

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
		if(bMemoryEdit)
		{
			g.setColor(Color.red);
			g.fillRect(rightAlignHelper - 624, 130 - g.getFontMetrics().getHeight() + g.getFontMetrics().getDescent(),
					   g.getFontMetrics().getWidths()[32] * 3,  22);
			g.setColor(Color.white);
		}
		g.drawString("RAM", rightAlignHelper - 624, 130);
		//g.drawString("(H) <- RAM " + AddressMap.toHexAddress((int)(0x00FF & ramPage),IOSize.IO16Bit) + " -> (J)", 
		//		rightAlignHelper - 714, 130);
		
		drawString(g, ramPageString, rightAlignHelper - 779, 150);
		
		// ROM
		if(bDebugMode)
		{
			g.setColor(Color.red);
			g.fillRect(rightAlignHelper - 214, 130 - g.getFontMetrics().getHeight() + g.getFontMetrics().getDescent(),
					   g.getFontMetrics().getWidths()[32] * 5,  22);
			g.setColor(Color.white);
		}
		g.drawString("DEBUG", rightAlignHelper - 214, 130);
		drawString(g, romPageString, rightAlignHelper - 379, 150);

		int readoutOffset = 140;
		// CPU
		g.drawString("CPU Registers:", 35, readoutOffset);
		if(ti!=null)
		{
			int xoffset = 35;

			readoutOffset+=30;
			
			int col = 0;
			for(String r : ti.registerInfo.keySet())
			{				
				Integer tiv = ti.registerInfo.get(r);
				g.drawString(r + ": " + Convert.padStringWithZeroes(Integer.toBinaryString(tiv & 0xFF), 8) + " ("
						+ Convert.byteToHexString((byte)(tiv & 0xFF)) + ")", xoffset + (220 * col), readoutOffset);
				col++;
				if(col > 1)
				{
					col = 0;
					readoutOffset+=30;
				}
			}
			
			g.drawString("Flags:             (" + Convert.byteToHexString(t.flags) + ")", 35, readoutOffset);
			for(String f : ti.flagInfo.keySet())
			{
				Integer fiv = ti.flagInfo.get(f);
				int counter = 0;
				String flagsString = f;
				for (char c : Convert.padStringWithZeroes(Integer.toBinaryString(fiv & 0xFF), 8).toCharArray())
				{
					g.setColor((c == '1') ? Color.green : Color.red);
					g.drawString(String.valueOf(flagsString.charAt(counter)), 120 + 16 * counter, readoutOffset);
					counter++;
				}			
			}			
			g.setColor(Color.white);
			
			g.drawString(
					"Stack Pointer: "
							+ Convert.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(t.stackPointer)), 8)
							+ " (" + Convert.byteToHexString(t.stackPointer) + ")["
							+ Integer.toHexString(((int) t.stackPointer & 0x0000FFFF)).toUpperCase() + "]",
					35, readoutOffset+=30);
			g.drawString(
					"Program Counter: "
							+ Convert.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt(t.programCounter)), 8)
							+ " ("
							+ Convert.padStringWithZeroes(
									Integer.toHexString(Short.toUnsignedInt(t.programCounter)).toUpperCase(), 4)
							+ ")",
					35, readoutOffset+=30);			
			
			if(ti.addressInfo.size() > 0)
			{
				for(String ad : ti.addressInfo.keySet())
				{				
					Integer adv = ti.registerInfo.get(ad);
					g.drawString(ad + ": " + Convert.padStringWithZeroes(Integer.toBinaryString(adv & 0xFFFF), 8) + " ("
							+ Convert.byteToHexString((byte)(adv & 0xFFFF)) + ")", xoffset + (220 * col), readoutOffset+=30);
				}				
			}
		}
		else
		{
			
			g.drawString("A: " + Convert.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(t.a)), 8) + " ("
					+ Convert.byteToHexString(t.a) + ")", 35, readoutOffset+=30);
			g.drawString("X: " + Convert.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(t.x)), 8) + " ("
					+ Convert.byteToHexString(t.x) + ")", 35, readoutOffset+=30);
			g.drawString("Y: " + Convert.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(t.y)), 8) + " ("
					+ Convert.byteToHexString(t.y) + ")", 35, readoutOffset+=30);
			g.drawString(
					"Stack Pointer: "
							+ Convert.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(t.stackPointer)), 8)
							+ " (" + Convert.byteToHexString(t.stackPointer) + ")["
							+ Integer.toHexString(((int) t.stackPointer & 0x000000FF) + 0x00000100).toUpperCase() + "]",
					35, readoutOffset+=30);
			g.drawString(
					"Program Counter: "
							+ Convert.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt(t.programCounter)), 16)
							+ " ("
							+ Convert.padStringWithZeroes(
									Integer.toHexString(Short.toUnsignedInt(t.programCounter)).toUpperCase(), 4)
							+ ")",
					35, readoutOffset+=30);
			g.drawString("Flags:             (" + Convert.byteToHexString(t.flags) + ")", 35, readoutOffset+=30);
			int counter = 0;
			String flagsString = "NVUBDIZC";
			for (char c : Convert.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(t.flags)), 8).toCharArray())
			{
				g.setColor((c == '1') ? Color.green : Color.red);
				g.drawString(String.valueOf(flagsString.charAt(counter)), 120 + 16 * counter, 320);
				counter++;
			}		
			g.setColor(Color.white);
			
			g.drawString(
					"Absolute Address: "
							+ Convert.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt(t.addressAbsolute)),
									16)
							+ " (" + Convert.byteToHexString((byte) ((short) t.addressAbsolute / 0xFF))
							+ Convert.byteToHexString((byte) t.addressAbsolute) + ")",
					35, readoutOffset+=30);
			g.drawString("Relative Address: "
					+ Convert.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt((short) t.addressRelative)),
							16)
					+ " (" + Convert.byteToHexString((byte) ((short) t.addressRelative / 0xFF))
					+ Convert.byteToHexString((byte) t.addressRelative) + ")", 35, readoutOffset+=30);
		}
		g.drawString("Opcode: " + t.opcodeName + " (" + Convert.byteToHexString(t.opcode) + ")", 35, readoutOffset+=30);
		g.drawString("Cycles: " + t.cycles, 35, readoutOffset+=30);
		g.drawString("IRQs  : " + t.irqs, 35, readoutOffset+=30);

		
		g.drawString("Reset JMP: ", 35, readoutOffset+=30);
		g.drawString("0x" + Convert.toHex16String(defaultResetAddress), 160, readoutOffset);
		g.drawString("Reset Vector: ", 35, readoutOffset+=30);
		g.drawString("0x" + Convert.toHex16String(defaultResetVector), 200, readoutOffset);
		readoutOffset+=30;


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
		int ctop = readoutOffset;
		g.drawString("Controls:", 35, ctop+=30);
		g.drawString("C - Toggle Clock", 35, ctop+=30);
		g.drawString("Space - Pulse Clock", 35, ctop+=30);
		g.drawString("R - Reset System", 35, ctop+=30);
		g.drawString("P - Reset CPU", 35, ctop+=30);
		if(bDebugMode)
			g.drawString("S - Step Debugger", 35, ctop+=30);
		else
			g.drawString("S - Toggle Slower " + (emulator.getClock().isSlow() ? "Disable" : "Enable"), 35, ctop+=30);
		g.drawString("I - Toggle Interrupt "+ 
		             (((DeviceDebugger) emulator.getCPU()).isEnabled("interrupt-hold") ? "Enable" : "Disable"),
				     35, ctop+=30);
		g.drawString("Arrows Navigate", 35, ctop+=30);
		g.drawString("CTL-E - Edit Mode (Enter Select/Accept)", 35, ctop+=30);
		g.drawString("CTL-D - Debug Mode", 35, ctop+=30);
		
		ctop+=30;
		if (!emulator.getClock().isEnabled())
			g.drawString("Cursors(Wheel) - Scroll History", 35, ctop);
		else
			g.drawString("Cursors(Wheel) - Scroll History Disabled", 35, ctop);
		
		g.drawString("< & > - Default Reset Address", 35, ctop+=30);
		//g.drawString("v" + emulator.getSystemVersion(), 7, ctop+=30);
	}

	public void drawString(Graphics g, String text, int x, int y)
	{
		int cLine = 0;
		
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
					
					
					
					if(bMemoryEdit && (cLine == memY))
					{						
						g.setColor(Color.GRAY);
						
						memAddress = (ramPage * 0x00000100) + (memY * 0x00000008) + memX;
						//System.out.println("EDIT ADDRESS:" + AddressMap.toHexAddress(editAddress,IOSize.IO16Bit));
						
						g.fillRect(x + g.getFontMetrics().charWidth('0') * 6 + (g.getFontMetrics().charWidth('0') * 3 *memX),
								   y + g.getFontMetrics().getDescent(),
								   g.getFontMetrics().getWidths()[32] * 2,  
								   22);
					}					
					y += g.getFontMetrics().getHeight();
					
					g.setColor(c);
					
					StringBuffer ptext = new StringBuffer(part[1]);
					
					if(bMemoryEnter && (cLine == memY))
					{
						ptext.setCharAt(3*memX + 1, memHILO[1]);
						ptext.setCharAt(3*memX + 2, memHILO[0]);
					}
					
					g.drawString(ptext.toString(), 
							     x + g.getFontMetrics().charWidth('0') * 5,
							     //y += g.getFontMetrics().getHeight()
							     y
							     );
					
				} catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			cLine++;
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
					//bound = 0;
					bound = size;

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
			if(bMemoryEdit)
			{
				this.memY++;
				if(this.memY > 31)
					this.memY = 31;
				System.out.println(memX + ":" +memY);
			}
			else
			{
				this.historyOffset -= 1;
				if (this.historyOffset < 0)
					this.historyOffset = 0;
				System.out.println("Offset:" + historyOffset);
				// this.repaint();
			}
		}
		else if (keyCode == KeyEvent.VK_UP)
		{
			if(bMemoryEdit)
			{
				this.memY--;
				if(this.memY < 0)
					this.memY = 0;
				System.out.println(memX + ":" +memY);
				
			}
			else
			{
				this.historyOffset += 1;
				// this.repaint();
				System.out.println("Offset:" + historyOffset);
			}
		}
		else if (keyCode == KeyEvent.VK_RIGHT)
		{
			if(bMemoryEdit)
			{
				this.memX++;
				if(this.memX > 7)
					this.memX = 7;
				System.out.println(memX + ":" +memY);
			}
			else
			{

			}
		}
		else if (keyCode == KeyEvent.VK_LEFT)
		{
			if(bMemoryEdit)
			{
				this.memX--;
				if(this.memX < 0)
					this.memX = 0;
				System.out.println(memX + ":" +memY);
			}
			else
			{

			}
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

		
		//System.out.println("Key:" + arg0.getKeyCode() + ":" + arg0.getKeyChar() + "(" + (int)arg0.getKeyChar() + ") " + arg0.getModifiers());
		
		if(bMemoryEnter)
		{
			switch( arg0.getKeyChar() )
			{
			case 10:
				if(bMemoryEdit)
				{					
					bMemoryEnter = false;
					if(this.memHILO[0]!=' ' && this.memHILO[1]!=' ')
					{
						byte b = (byte) AddressMap.toByte(this.memHILO[1],this.memHILO[0]);
						emulator.getBus().write((short)memAddress, b);
						this.memHILO[1] = ' ';
						this.memHILO[0] = ' ';
					}
					System.out.println("bMemoryEnter:" + bMemoryEnter);
				}
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case 'A':
			case 'a':
			case 'B':
			case 'b':
			case 'C':
			case 'c':
			case 'D':
			case 'd':
			case 'E':
			case 'e':
			case 'F':
			case 'f':
				char u = Character.toUpperCase(arg0.getKeyChar());

				if(memHILOIndex < 0)
				{	
					memHILOIndex = 1;
					memHILO[1] = u;
					memHILO[0] = ' ';
				}
				else
				{
					memHILO[memHILOIndex] = u;					
				}
				memHILOIndex--;
				System.out.println("bMemoryEnter VALUE[" + memHILOIndex + "]:" + arg0.getKeyChar());
				
				break;				
			}			
			
			return;
		}
		
		switch (arg0.getKeyChar())
		{
		case 'e':
			
			break;
		case 'c':
			c.setEnabled(!c.isEnabled());
			this.historyOffset = 0;
			break;
		case ' ':
			c.pulse();
			this.historyOffset = 0;
			break;
		case 's':
			if(bDebugMode)
				System.out.println("DO STEP");
			else
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

		case '8':
			if(!bMemoryEdit)
				break;
			this.memY--;
			if(this.memY < 0)
				this.memY = 0;
			System.out.println(memX + ":" +memY);
			break;
		case '2':
			if(!bMemoryEdit)
				break;
			this.memY++;
			if(this.memY > 31)
				this.memY = 31;
			System.out.println(memX + ":" +memY);
			break;
		case '6':
			if(!bMemoryEdit)
				break;
			this.memX++;
			if(this.memX > 7)
				this.memX = 7;
			System.out.println(memX + ":" +memY);
			break;
		case '4':
			if(!bMemoryEdit)
				break;
			this.memX--;
			if(this.memX < 0)
				this.memX = 0;
			System.out.println(memX + ":" +memY);
			break;
		default:
			switch( (int)arg0.getKeyChar() )
			{
			case 10:
				if(bMemoryEdit)
				{
					bMemoryEnter = true;
				}
				break;
			case 4:
				bDebugMode = !bDebugMode;
				System.out.println("DebugMode:" + bDebugMode);
				break;
			case 5:
				//System.out.println("CTL-e");				
				if (!emulator.getClock().isEnabled())
				{
					bMemoryEdit = !bMemoryEdit;
				}
				
				System.out.println("MemoryMode:" + bMemoryEdit);
				break;
			}			
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

	@Override
	public void redraw()
	{
		this.repaint();		
	}

	@Override
	public void busReset()
	{
		redraw();		
	}

}
