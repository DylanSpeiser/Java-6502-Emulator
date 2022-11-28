package com.hadden.emulator.ui;
//Original Code by Dylan Speiser

//https://github.com/DylanSpeiser

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;

import javax.swing.*;

import com.hadden.emu.AddressMap;
import com.hadden.emu.AddressMapImpl;
import com.hadden.emu.Bus;
import com.hadden.emu.BusAddressRange;
import com.hadden.emu.BusDevice;
import com.hadden.emu.BusIRQ;
import com.hadden.emu.BusListener;
import com.hadden.emu.CPU;
import com.hadden.emu.impl.DisplayDevice;
import com.hadden.emu.impl.RAMDevice;
import com.hadden.emu.impl.ROMDevice;
import com.hadden.emu.impl.TimerDevice;
import com.hadden.roms.ROMManager;
import com.hadden.emulator.Clock;
import com.hadden.emulator.ClockLine;
import com.hadden.emulator.Emulator;
import com.hadden.emulator.cpu.MOS.MOS65C02A;
import com.hadden.emulator.project.CC65ProjectImpl;
import com.hadden.emulator.project.Project;
import com.hadden.emulator.project.ProjectImpl;

@SuppressWarnings("serial")
public class MainSystemEmulator extends JFrame implements ActionListener, Emulator
{
	public String versionString = "1.0";

	private static MainSystemEmulator emu;
	
	// Swing Things
	JPanel p = new JPanel();
	JPanel header = new JPanel();
	JFileChooser fc = new JFileChooser();
	public static JButton ROMopenButton = new JButton("Open ROM File");
	public static JButton RAMopenButton = new JButton("Open RAM File");

	

	public EmulatorDisplay emulatorDisplay = null;

	
	private Clock clock = null;

	private RAMDevice ram;

	private ROMDevice rom;

	private Bus bus;

	private MOS65C02A cpu;
	
	
	public MainSystemEmulator()
	{
		init();
		
		emulatorDisplay = new EmulatorDisplay(this);
		((AddressMap)this.getBus()).addBusListener(emulatorDisplay);
		
		// map.setBusListener(graphicsPanel);

		// Open .bin file button
		ROMopenButton.setVisible(true);
		ROMopenButton.addActionListener(this);
		ROMopenButton.setBounds(getWidth() - 150, 15, 125, 25);
		ROMopenButton.setBackground(Color.white);
		emulatorDisplay.add(ROMopenButton);

		RAMopenButton.setVisible(true);
		RAMopenButton.addActionListener(this);
		RAMopenButton.setBounds(getWidth() - 150, 45, 125, 25);
		RAMopenButton.setBackground(Color.white);
		emulatorDisplay.add(RAMopenButton);

		// file chooser
		fc.setVisible(true);
		String binDir = System.getProperty("user.home") + System.getProperty("file.separator") + "Downloads";
		if (System.getenv("SEMU_BIN_DIR") != null && System.getenv("SEMU_BIN_DIR").length() > 0)
			binDir = System.getenv("SEMU_BIN_DIR");

		String ramDir = binDir;
		if (System.getenv("SEMU_RAM_DIR") != null && System.getenv("SEMU_RAM_DIR").length() > 0)
			ramDir = System.getenv("SEMU_RAM_DIR");

		String romDir = binDir;
		if (System.getenv("SEMU_ROM_DIR") != null && System.getenv("SEMU_ROM_DIR").length() > 0)
			romDir = System.getenv("SEMU_ROM_DIR");

		fc.setCurrentDirectory(new File(binDir));


		clock = new SystemClock();
		clock.addClockLine(clockLine);
		
		// Final Setup
		
		emulatorDisplay.setVisible(true);
		
		emulatorDisplay.setSize(new Dimension(1920,1080));
		this.setUndecorated(false);
		this.setTitle("System Emulator");
		this.setContentPane(emulatorDisplay);
		this.setSize(new Dimension(1920,1080));
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(true);
	
	}

	private void init()
	{
		ram = new RAMDevice(0x00000000,64*1024);
		//rom = new ROMDevice(0x00008000,ROMManager.loadROM("demo.rom"));
		
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
		
		map.addBusDevice((BusDevice)ram)
		   //.addBusDevice(new DisplayDevice(0x0000A000,40,10))
		   //.addBusDevice(new ROMDevice(0x00000000,ROMManager.loadROM("demo.rom")))   
		   .addBusDevice(new ROMDevice(0x00000000,ROMManager.loadROM("file://./demo/main.bin")))
		   //.addBusDevice(new ROMDevice(0x00000200,ROMManager.loadROM("file://C:\\Users\\mike.bush\\devprojects\\Java-System-Emulator\\demo\\multia-prg.bin")))
		   //.addBusDevice(new ROMDevice(0x0000FFFA,ROMManager.loadROM("file://C:\\Users\\mike.bush\\devprojects\\Java-System-Emulator\\demo\\multia-irq.bin")))
		   //.addBusDevice(new ROMDevice(0x00000200,ROMManager.loadROM("file://C:\\Users\\mike.bush\\devprojects\\Java-System-Emulator\\demo\\cdemo.bin")))
		   //.addBusDevice(new ROMDevice(0x00000200,ROMManager.loadROM("file://C:\\Users\\mike.bush\\devprojects\\Java-System-Emulator\\demo\\multia.bin")))
		   .addBusDevice(new DisplayDevice(0x0000A000,80,25))
		   //.addBusDevice(new LCDDevice(0x0000B000))
		   //.addBusDevice(new TimerDevice(0x0000B003,60000))
		   .addBusDevice(new TimerDevice(0x0000B005,10))
		   //.addBusDevice(new SerialDevice(0x00000200))
		   ;
		

		map.printAddressMap();
		//System.out.println(  ((Bus)map).dumpBytesAsString());
		
		//SystemEmulator.enableDebug(true);
		
		bus = (Bus)map;
		cpu = new MOS65C02A(bus);
		
		// Swing Stuff:
		System.setProperty("sun.java2d.opengl", "true");
	}

	@Override
	public String getSystemVersion()
	{
		return this.versionString;
	}

	/*
	public static Bus getBus()
	{
		return bus;
	}
	*/

	@Override
	public void actionPerformed(ActionEvent e)
	{
		/*
		if (e.getSource().equals(ROMopenButton))
		{
			fc.setSelectedFile(new File(""));
			int returnVal = fc.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				rom.setROMArray(ROMLoader.readROM(fc.getSelectedFile()));
			}
			graphicsPanel.requestFocus();
			graphicsPanel.romPageString = SystemEmulator.rom.getROMString().substring(graphicsPanel.romPage * 960,
					SystemEmulator.rom.getROMString().length());

			ram.setRAMArray(new byte[] { 0x4C, 0x00, (byte) 0x80 });

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

				byte[] array = ROMLoader.readROM(selectedFile, baseAddress);

				if (baseAddress[0] == null)
					baseAddress[0] = 0;
				ram.setRAMArray(baseAddress[0], array);

				if (baseAddress[0] > 0)
				{
					ram.write((short) 0x0000, (byte) 0x4C);
					ram.write((short) 0x0001, (byte) (baseAddress[0] & 0xFF));
					ram.write((short) 0x0002, (byte) ((baseAddress[0] & 0xFF00) >> 8));
				}
			}
			graphicsPanel.requestFocus();
			graphicsPanel.ramPageString = SystemEmulator.ram.getRAMString().substring(graphicsPanel.ramPage * 960,
					(graphicsPanel.ramPage + 1) * 960);
			cpu.reset();
		}
		*/
	}

	/*
	public static boolean enableDebug(boolean enabled)
	{
		boolean mode = debugEnabled;
		debugEnabled = enabled;
		return mode;
	}

	public static void debug(String message)
	{
		if (debugEnabled)
			System.out.println(message);
	}
	*/
	@Override
	public Clock getClock()
	{
		return clock;
	}

	private ClockLine clockLine = new ClockLine()
	{
		@Override
		public void pulse()
		{
			//System.out.println("MainSystemEmulator::ClockLine");
			cpu.clock();
		}
	};


	@Override
	public CPU getCPU()
	{
		return cpu;
	}

	@Override
	public void reset()
	{
		cpu.reset();
		BusAddressRange addrSpace = ram.getBusAddressRange();
		
		System.out.println("MainSystemEmulator::LowAddress:" + addrSpace.getLowAddress());
		System.out.println("MainSystemEmulator::HighAddress:" + addrSpace.getHighAddress());
		
		
		AddressMap map = (AddressMap)bus;
		if(map!=null)
		{
			for(BusDevice bd : map.getDevices())
			{
				if(bd!=null)
					bd.reset();
			}
		}
		
		
		this.emulatorDisplay.repaint();
	}

	@Override
	public Bus getBus()
	{
		return bus;
	}

	@Override
	public String getMainTitle()
	{
		return this.getCPU().getName();
	}

	public static void main(String[] args)
	{
		String projectDir = null;
		
		if(args.length > 0)
		{
			for(int i=0;i<args.length;i++)
			{
				//System.out.println("Args:" + args[i]);
				if("--project".equals(args[i]))
				{
					i++;
					if(i < args.length)
					{
						projectDir = args[i];
						Project p = new CC65ProjectImpl(projectDir);
						
						p.getFiles();
						p.compile();
						
					}
				}
			}
		}
		
		emu = new MainSystemEmulator();		
	}
	
	
}
