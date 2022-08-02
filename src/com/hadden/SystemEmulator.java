package com.hadden;
//Original Code by Dylan Speiser

//https://github.com/DylanSpeiser

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;

import com.hadden.emu.AddressMap;
import com.hadden.emu.AddressMapImpl;
import com.hadden.emu.Bus;
import com.hadden.emu.BusDevice;
import com.hadden.emu.BusIRQ;
import com.hadden.emu.CPU;
import com.hadden.emu.RAM;
import com.hadden.emu.ROM;
import com.hadden.emu.VIA;
import com.hadden.emu.c64.BASICDevice;
import com.hadden.emu.c64.CIADevice;
import com.hadden.emu.c64.CharacterDevice;
import com.hadden.emu.c64.KernalDevice;
import com.hadden.emu.c64.KeyboardDevice;
import com.hadden.emu.c64.ScreenDevice;
import com.hadden.emu.c64.VICIIDevice;
import com.hadden.emu.cpu.MOS65C02;
import com.hadden.emu.cpu.MOS65C02A;
import com.hadden.emu.impl.DisplayDevice;
import com.hadden.emu.impl.LCDDevice;
import com.hadden.emu.impl.MuxDevice;
import com.hadden.emu.impl.RAMDevice;
import com.hadden.emu.impl.ROMDevice;
import com.hadden.emu.impl.SerialDevice;
import com.hadden.emu.impl.TimerDevice;
import com.hadden.emu.impl.MuxDevice.MuxMapper;
import com.hadden.roms.ROMManager;


public class SystemEmulator extends JFrame implements ActionListener
{
	public static SystemEmulator emu;
	public static String versionString = "1.0";

	// Swing Things
	JPanel p = new JPanel();
	JPanel header = new JPanel();
	JFileChooser fc = new JFileChooser();
	public static JButton ROMopenButton = new JButton("Open ROM File");
	public static JButton RAMopenButton = new JButton("Open RAM File");

	// Clock Stuff
	public static Thread clockThread;
	public static boolean clockState = false;
	//public static int clocks = 0;
	public static boolean haltFlag = true;
	public static boolean slowerClock = false;

	// Emulator Things
	public static RAM ram = null;//new RAMImpl();//new RAM();
	public static ROM rom = null; //new ROM();
	//public static LCD lcd = new LCD();
	public static VIA via = new VIA();
	//public static Bus bus = new BusImpl();
	public static Bus bus = null;
	
	public static CPU cpu = null;// new CPU();
	private static boolean debugEnabled = false;

	public DisplayPanel GraphicsPanel = null;

	public SystemEmulator()
	{
		ram = new RAMDevice(0x00000000,64*1024);
		rom = new ROMDevice(0x00008000);
		
		AddressMap map =  new AddressMapImpl((BusDevice)ram,
	            new BusIRQ() 
				{
					@Override
					public void raise(int source)
					{
						//System.out.println("CPU IRQ");	
						if(cpu!=null)
							cpu.interrupt();
					}
				});
		

		map.addBusDevice((BusDevice)rom)
		   //.addBusDevice(new DisplayDevice(0x0000A000,40,10))
		   .addBusDevice(new DisplayDevice(0x0000A000,80,25))
		   //.addBusDevice(new LCDDevice(0x0000B000))
		   //.addBusDevice(new TimerDevice(0x0000B003,60000))
		   //.addBusDevice(new TimerDevice(0x0000B005,5000))
		   //.addBusDevice(new SerialDevice(0x00000200))
		   ;
		
		
		/*
		MuxDevice mux = new MuxDevice(0x0000D000, 0x3FF, 0x00000000, 
				new MuxMapper() 
				{
					@Override
					public int map(int value)
					{
						if( (((byte)value) & (byte)0x04) == 0x04)
							return 1;
						return 0;
					}
				},new BusDevice[] 
				{
					new CharacterDevice(0x0000D000,ROMManager.loadROM("characters.rom")),
					new VICIIDevice(0x0000D000)
				} );
		
		map.addBusDevice(new CIADevice(0x0000DC00))
		   .addBusDevice(new ScreenDevice(0x00000400,40,25))
		   .addBusDevice(mux)
		   //.addBusDevice(new VICIIDevice(0x0000D000))
		   .addBusDevice(new BASICDevice(0x0000A000,ROMManager.loadROM("basic.rom")))
		   //.addBusDevice(new CharacterDevice(0x0000D000,ROMManager.loadROM("characters.rom")))
		   .addBusDevice(new KernalDevice(0x0000E000,ROMManager.loadROM("kernal.rom")))
		   .addBusDevice(new com.hadden.emu.c64.TimerDevice("TIMER-A", 0x0000DC04))
		   .addBusDevice(new com.hadden.emu.c64.TimerDevice("TIMER-B", 0x0000DC06))
		   .addBusDevice(new com.hadden.emu.c64.TimerDevice("TIMER-A", 0x0000DD04))
		   .addBusDevice(new com.hadden.emu.c64.TimerDevice("TIMER-B", 0x0000DD06))
		   .addBusDevice(new KeyboardDevice(0x0000DC00))
		   .addBusDevice(new TimerDevice(0x0000B005,1000))
		   ;		
		*/
		map.printAddressMap();
		System.out.println(  ((Bus)map).dumpBytesAsString());
		
		SystemEmulator.enableDebug(true);
		
		bus = (Bus)map;
		cpu = new MOS65C02A(bus);
		// Swing Stuff:
		System.setProperty("sun.java2d.opengl", "true");
		this.setSize(1500, 1000);

		GraphicsPanel = new DisplayPanel(cpu.getName() + " Emulator");
		map.setBusListener(GraphicsPanel);
		
		// Open .bin file button
		ROMopenButton.setVisible(true);
		ROMopenButton.addActionListener(this);
		ROMopenButton.setBounds(getWidth() - 150, 15, 125, 25);
		ROMopenButton.setBackground(Color.white);
		GraphicsPanel.add(ROMopenButton);

		RAMopenButton.setVisible(true);
		RAMopenButton.addActionListener(this);
		RAMopenButton.setBounds(getWidth() - 150, 45, 125, 25);
		RAMopenButton.setBackground(Color.white);
		GraphicsPanel.add(RAMopenButton);

		// file chooser
		fc.setVisible(true);
		String binDir = System.getProperty("user.home") + System.getProperty("file.separator") + "Downloads";
		if(System.getenv("SEMU_BIN_DIR")!=null && System.getenv("SEMU_BIN_DIR").length() > 0)
			binDir = System.getenv("SEMU_BIN_DIR");

		String ramDir = binDir;
		if(System.getenv("SEMU_RAM_DIR")!=null && System.getenv("SEMU_RAM_DIR").length() > 0)
			ramDir = System.getenv("SEMU_RAM_DIR");

		String romDir = binDir;
		if(System.getenv("SEMU_ROM_DIR")!=null && System.getenv("SEMU_ROM_DIR").length() > 0)
			romDir = System.getenv("SEMU_ROM_DIR");

		
		fc.setCurrentDirectory(new File(binDir));

		// Clock thread setup
		clockThread = new Thread(() -> {
			while (true)
			{
				try
				{
					if (SystemEmulator.clockState)
						cpu.clock();
					System.out.print("");
					if (slowerClock)
					{
						try
						{
							Thread.sleep(1);
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		clockThread.start();

		// Final Setup
		GraphicsPanel.setVisible(true);
		this.setTitle("System Emulator");
		this.setContentPane(GraphicsPanel);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public static Bus getBus()
	{
		return bus;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource().equals(ROMopenButton))
		{
			fc.setSelectedFile(new File(""));
			int returnVal = fc.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				rom.setROMArray(ROMLoader.readROM(fc.getSelectedFile()));
			}
			GraphicsPanel.requestFocus();
			GraphicsPanel.romPageString = SystemEmulator.rom.getROMString().substring(GraphicsPanel.romPage * 960,
					SystemEmulator.rom.getROMString().length());
			
			ram.setRAMArray(new byte[] {0x4C, 0x00, (byte) 0x80});
			
			cpu.reset();
		}
		else if (e.getSource().equals(RAMopenButton))
		{
			fc.setSelectedFile(new File(""));
			int returnVal = fc.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File selectedFile = fc.getSelectedFile();
				
				Integer[] baseAddress = new Integer[1];
				
				byte[] array = ROMLoader.readROM(selectedFile,baseAddress);
				
				if(baseAddress[0] == null)
					baseAddress[0] = 0;
				ram.setRAMArray(baseAddress[0],array);
				
				if(baseAddress[0] > 0)
				{
					ram.write((short)0x0000,(byte)0x4C);
					ram.write((short)0x0001,(byte)(baseAddress[0] & 0xFF));
					ram.write((short)0x0002, (byte)((baseAddress[0] & 0xFF00) >> 8));
				}
			}
			GraphicsPanel.requestFocus();
			GraphicsPanel.ramPageString = SystemEmulator.ram.getRAMString().substring(GraphicsPanel.ramPage * 960,
					(GraphicsPanel.ramPage + 1) * 960);
			cpu.reset();
		}
	}

	public static boolean enableDebug(boolean enabled)
	{
		boolean mode = debugEnabled;
		debugEnabled = enabled;
		return mode;
	}
	
	public static void debug(String message)
	{
		if(debugEnabled)
			System.out.println(message);
	}
	
	public static void main(String[] args)
	{
		emu = new SystemEmulator();
	}
}
