package com.hadden.emulator.ui;
//Original Code by Dylan Speiser

//https://github.com/DylanSpeiser

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.hadden.SystemConfig;
import com.hadden.SystemConfigLoader;
import com.hadden.emu.AddressMap;
import com.hadden.emu.AddressMapImpl;
import com.hadden.emu.Bus;
import com.hadden.emu.BusAddressRange;
import com.hadden.emu.BusDevice;
import com.hadden.emu.BusIRQ;
import com.hadden.emu.BusListener;
import com.hadden.emu.CPU;
import com.hadden.emu.IOSize;
import com.hadden.emu.c64.BASICDevice;
import com.hadden.emu.c64.CIADevice;
import com.hadden.emu.c64.CharacterDevice;
import com.hadden.emu.c64.KernalDevice;
import com.hadden.emu.c64.KeyboardDevice;
import com.hadden.emu.c64.ScreenDevice;
import com.hadden.emu.c64.VICIIDevice;
import com.hadden.emu.impl.DisplayDevice;
import com.hadden.emu.impl.MuxDevice;
import com.hadden.emu.impl.RAMDevice;
import com.hadden.emu.impl.ROMDevice;
import com.hadden.emu.impl.TimerDevice;
import com.hadden.roms.ROMManager;
import com.hadden.util.system.io.FileMonitor;
import com.hadden.emulator.cpu.MOS.MOS65C02A;
import com.hadden.emulator.cpu.Motorola.m68000.MC68000;
import com.hadden.emulator.cpu.Zilog.Z80;
import com.hadden.emulator.project.Project;
import com.hadden.emulator.Clock;
import com.hadden.emulator.ClockLine;
import com.hadden.emulator.Emulator;
import com.hadden.emulator.project.CC65ProjectImpl;
import com.hadden.emulator.project.ProjectImpl;
import com.hadden.emulator.ui.EmulatorDisplay;
import com.hadden.emulator.ui.EmulatorDisplayImpl;


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

	private static String platform;

	

	public EmulatorDisplay emulatorDisplay = null;

	
	private Clock clock = null;

	private BusDevice ram;

	private ROMDevice rom;

	private Bus bus;

	private CPU cpu;

	private FileMonitor monitoredBinary;
	
	
	public MainSystemEmulator(SystemConfig configuration)
	{
		init(configuration);		
		initDisplay();	
	}

	
	public MainSystemEmulator()
	{
		this(null);
	}

	private void initDisplay() 
	{
		emulatorDisplay = new EmulatorDisplayImpl(this);
		((AddressMap)this.getBus()).addBusListener((BusListener)emulatorDisplay);
		
		// map.setBusListener(graphicsPanel);

		// Open .bin file button
//		ROMopenButton.setVisible(true);
//		ROMopenButton.addActionListener(this);
//		ROMopenButton.setBounds(getWidth() - 150, 15, 125, 25);
//		ROMopenButton.setBackground(Color.white);
//		emulatorDisplay.add(ROMopenButton);
//
//		RAMopenButton.setVisible(true);
//		RAMopenButton.addActionListener(this);
//		RAMopenButton.setBounds(getWidth() - 150, 45, 125, 25);
//		RAMopenButton.setBackground(Color.white);
//		emulatorDisplay.add(RAMopenButton);

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
		// separate out impl
		JPanel jp = (JPanel)emulatorDisplay;
		jp.setVisible(true);		
		jp.setSize(new Dimension(1920,1200));
		this.setContentPane(jp);
		//
		this.setUndecorated(false);
		this.setTitle("System Emulator");		
		this.setSize(new Dimension(1920,1080));
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(true);
	}

	@SuppressWarnings("deprecation")
	private void init(SystemConfig configuration)
	{
		if(configuration!=null)
		{
			try
			{
				
				//
				// Use configuration to create the default bus device (RAM typically)
				//
				BusDevice dbd = configuration.initBusDevice(configuration.getDefAddressDevice());
				ram = dbd;
				//
				// Use configuration to create the bus device
				//
				bus = configuration.initBus(dbd, new BusIRQ() 
				{
					@Override
					public void raise(int source)
					{
						if(cpu!=null)
							cpu.interrupt();
					}
				});
				//
				// Iterate through devices defined into the address/bus space
				//
				Map<String, String[]> mapped = configuration.getMapped();
				for(String k : mapped.keySet())
				{
					System.out.println("Initializing bus device:" + k);
					
					if(!k.equals(configuration.getDefAddressDevice()))
					{
						((AddressMap)bus).addBusDevice(configuration.initBusDevice(k));
					}
				}
				//
				// Use configuration to create the CPU
				//
				cpu = configuration.initCPU(bus);

			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			//
			// create default address handler device
			//
			ram = new RAMDevice(0x00000000,64*1024);
			//
			// create standard address bus with default bus device
			//
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
			//
			// default 6502 test setting
			//
			
			map.addBusDevice((BusDevice)ram)
			   .addBusDevice(new ROMDevice(0x00000000,ROMManager.loadROM("file://./demo/projectB/main.bin")))
			   .addBusDevice(new DisplayDevice(0x0000A000,80,25))
			   //.addBusDevice(new LCDDevice(0x0000B000))
			   .addBusDevice(new TimerDevice(0x0000B005,10)) // provide IRQs
			   //.addBusDevice(new SerialDevice(0x00000200))
			   ;
			map.printAddressMap();
			bus = (Bus)map;
			cpu = new MOS65C02A(bus);			
			//
			// testing the c64 setup with code to debug why BASIC is not loading
			//
			/*
			MuxDevice mux = new MuxDevice(0x0000D000, 0x3FF, 0x00000000, 
					new MuxDevice.MuxMapper() 
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
						new CharacterDevice(0x0000D000,"file://./demo/c64/characters.rom"),
						new VICIIDevice(0x0000D000)
					} );
			
			map.addBusDevice(new CIADevice(0x0000DC00))
			   .addBusDevice(new ScreenDevice(0x00000400,40,25))
			   .addBusDevice(mux)
			   //.addBusDevice(new VICIIDevice(0x0000D000))
			   .addBusDevice(new BASICDevice(0x0000A000,"file://./demo/c64/basic.rom"))
			   //.addBusDevice(new CharacterDevice(0x0000D000,ROMManager.loadROM("characters.rom")))
			   .addBusDevice(new KernalDevice(0x0000E000,"file://./demo/c64/kernal.rom"))
			   .addBusDevice(new com.hadden.emu.c64.TimerDevice("TIMER-A", 0x0000DC04))
			   .addBusDevice(new com.hadden.emu.c64.TimerDevice("TIMER-B", 0x0000DC06))
			   .addBusDevice(new com.hadden.emu.c64.TimerDevice("TIMER-A", 0x0000DD04))
			   .addBusDevice(new com.hadden.emu.c64.TimerDevice("TIMER-B", 0x0000DD06))
			   //.addBusDevice(new KeyboardDevice(0x0000DC00))
			   .addBusDevice(new TimerDevice(0x0000B005,5000))
			   ;	
			map.printAddressMap();
			bus = (Bus)map;
			cpu = new MOS65C02A(bus);
			
			//
			// example bus listener
			//			
			((AddressMap)bus).addBusListener(new BusListener()
			{

				@Override
				public void readListener(short address) 
				{
				}

				@Override
				public void writeListener(short address, byte data) 
				{
					if((address & 0x0000FFFF) == 0xA012)
					{
						System.out.println("DEBUG ADDRESS changed:" + AddressMap.toHexAddress(data, IOSize.IO8Bit));
					}
				}
				
			});
			*/
			
			//
			// test monitoring a cc65 based project for changes and auto update and reset CPU
			//
			// this works but needs to be offically implemented
			//
			/*
			monitoredBinary = new FileMonitor(new File("./demo/projectB/main.bin"));
			monitoredBinary.addObserver(new Observer() 
			{
				@Override
				public void update(Observable o, Object arg) 
				{
					System.out.println("monitoredBinary changed!");
					
					clock.setEnabled(false);
					
					((AddressMap)bus).removeDevices();
					
					ram = new RAMDevice(0x00000000,64*1024);
					AddressMap map =  new AddressMapImpl((BusDevice)ram,
				            new BusIRQ() 
							{
								@Override
								public void raise(int source)
								{
									if(cpu!=null)
										cpu.interrupt();
								}
							});				
					map.addBusDevice((BusDevice)ram)
					   .addBusDevice(new ROMDevice(0x00000000,ROMManager.loadROM("file://./demo/projectB/main.bin")))
					   .addBusDevice(new DisplayDevice(0x0000A000,80,25))
					   .addBusDevice(new TimerDevice(0x0000B005,10))
					   ;
					map.printAddressMap();
					bus = (Bus)map;
					if(emulatorDisplay!=null)
					{
						((AddressMap)getBus()).addBusListener(emulatorDisplay);
						emulatorDisplay.refreshBus();
					}
					cpu.setBus(bus);
					MainSystemEmulator.this.repaint();
				}
				
			});
			*/
			
			//SystemEmulator.enableDebug(true);
			
		}
		
		
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
			//if(cpu.getTelemetry().programCounter == 0x0075)
		//		clock.setEnabled(false);
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
		
		
		this.emulatorDisplay.redraw();
	}

	@Override
	public Bus getBus()
	{
		return bus;
	}

	@Override
	public String getMainTitle()
	{
		if(this.getCPU()!=null)
			return this.getCPU().getName();
		else
			return "Loading";
	}

	public static void main(String[] args)
	{
		// Useage of "--config"
		// --config ./demo/Z80.system
		//
		
		String configFile = null;
		String projectDir = null;
		
		SystemConfig sc = null;
		
		MainSystemEmulator.platform = System.getProperty("os.name").toLowerCase();
		
		/*
		if(!MainSystemEmulator.platform.contains("windows"))
		{
			JDialog.setDefaultLookAndFeelDecorated(true);
			JFrame.setDefaultLookAndFeelDecorated(true);		
		}
		*/
		try
		{
			UIManager.setLookAndFeel(new MetalLookAndFeel());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
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
						p.link();
						
					}
				}
				else if("--config".equals(args[i]))
				{
					i++;
					if(i < args.length)
					{
						configFile = args[i];
						sc = SystemConfigLoader.loadConfiguration(configFile);
					}
				}
			
			}
		}
		
		emu = new MainSystemEmulator(sc);		
	}
	
	
}
