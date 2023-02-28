package com.juse.emulator;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;

import com.hadden.util.system.io.FileMonitor;
import com.juse.emulator.devices.AddressMapImpl;
import com.juse.emulator.devices.DisplayDevice;
import com.juse.emulator.devices.RAMDevice;
import com.juse.emulator.devices.ROMDevice;
import com.juse.emulator.devices.SystemClock;
import com.juse.emulator.devices.TimerDevice;
import com.juse.emulator.devices.cpu.MOS.MOS65C02A;
import com.juse.emulator.interfaces.AddressMap;
import com.juse.emulator.interfaces.Bus;
import com.juse.emulator.interfaces.BusAddressRange;
import com.juse.emulator.interfaces.BusDevice;
import com.juse.emulator.interfaces.BusIRQ;
import com.juse.emulator.interfaces.CPU;
import com.juse.emulator.interfaces.Clock;
import com.juse.emulator.interfaces.ClockLine;
import com.juse.emulator.interfaces.Emulator;
import com.juse.emulator.interfaces.ui.EmulatorFrame;
import com.juse.emulator.util.config.CommandLineHandler;
import com.juse.emulator.util.config.ResourceCopy;
import com.juse.emulator.util.config.SystemConfig;
import com.juse.emulator.util.config.SystemConfigLoader;
import com.juse.emulator.util.loaders.ROMManager;
import com.juse.emulator.util.process.ProcessUtil;

@SuppressWarnings("serial")
public class SystemEmulatorEntry implements Emulator
{
	private static final String DEMO_RESOURCE = "demo";

	private static final String EMULATOR_TITLE = "Java System Emulator";

	public String versionString = "1.0";

	private static SystemEmulatorEntry emu;

	private static String extDir = null;

	private Clock clock = null;

	private BusDevice ram;

	private ROMDevice rom;

	private Bus bus;

	private CPU cpu;

	private FileMonitor monitoredBinary;

	public static class CommandItem
	{
		public String name;
		public Class cls;
		public String help;
		public boolean optional = true;
	}

	public SystemEmulatorEntry(SystemConfig configuration) throws Exception
	{
		init(configuration);
	}

	public SystemEmulatorEntry() throws Exception
	{
		this(null);
	}

	private void init(SystemConfig configuration) throws Exception
	{
		if (configuration != null)
		{
			try
			{

				//
				// Use configuration to create the default bus device (RAM
				// typically)
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
						if (cpu != null)
							cpu.interrupt();
					}
				});
				//
				// Iterate through devices defined into the address/bus space
				//
				Map<String, String[]> mapped = configuration.getMapped();
				for (String k : mapped.keySet())
				{
					System.out.println("Initializing bus device:" + k);

					if (!k.equals(configuration.getDefAddressDevice()))
					{
						((AddressMap) bus).addBusDevice(configuration.initBusDevice(k));
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
				throw new Exception("Bad system configuration parameters.");
			}
		}
		else
		{
			//
			// create default address handler device for a 6502
			//
			ram = new RAMDevice(0x00000000, 64 * 1024);
			//
			// create standard address bus with default bus device
			//
			AddressMap map = new AddressMapImpl((BusDevice) ram, new BusIRQ()
			{
				@Override
				public void raise(int source)
				{
					// System.out.println("CPU IRQ");
					if (cpu != null)
						cpu.interrupt();
				}
			});
			//
			// default 6502 test setting
			//

			map.addBusDevice((BusDevice) ram)
					.addBusDevice(new ROMDevice(0x00000000, ROMManager.loadROM("file://./demo/projectB/main.bin")))
					.addBusDevice(new DisplayDevice(0x0000A000, 80, 25))
					// .addBusDevice(new LCDDevice(0x0000B000))
					.addBusDevice(new TimerDevice(0x0000B005, 10)) // provide
																	// IRQs
			// .addBusDevice(new SerialDevice(0x00000200))
			;
			map.printAddressMap();
			bus = (Bus) map;
			cpu = new MOS65C02A(bus);
			//
			// testing the c64 setup with code to debug why BASIC is not loading
			//
			/*
			 * MuxDevice mux = new MuxDevice(0x0000D000, 0x3FF, 0x00000000, new
			 * MuxDevice.MuxMapper() {
			 * 
			 * @Override public int map(int value) { if( (((byte)value) &
			 * (byte)0x04) == 0x04) return 1; return 0; } },new BusDevice[] {
			 * new
			 * CharacterDevice(0x0000D000,"file://./demo/c64/characters.rom"),
			 * new VICIIDevice(0x0000D000) } );
			 * 
			 * map.addBusDevice(new CIADevice(0x0000DC00)) .addBusDevice(new
			 * ScreenDevice(0x00000400,40,25)) .addBusDevice(mux)
			 * //.addBusDevice(new VICIIDevice(0x0000D000)) .addBusDevice(new
			 * BASICDevice(0x0000A000,"file://./demo/c64/basic.rom"))
			 * //.addBusDevice(new
			 * CharacterDevice(0x0000D000,ROMManager.loadROM("characters.rom")))
			 * .addBusDevice(new
			 * KernalDevice(0x0000E000,"file://./demo/c64/kernal.rom"))
			 * .addBusDevice(new com.hadden.emu.c64.TimerDevice("TIMER-A",
			 * 0x0000DC04)) .addBusDevice(new
			 * com.hadden.emu.c64.TimerDevice("TIMER-B", 0x0000DC06))
			 * .addBusDevice(new com.hadden.emu.c64.TimerDevice("TIMER-A",
			 * 0x0000DD04)) .addBusDevice(new
			 * com.hadden.emu.c64.TimerDevice("TIMER-B", 0x0000DD06))
			 * //.addBusDevice(new KeyboardDevice(0x0000DC00)) .addBusDevice(new
			 * TimerDevice(0x0000B005,5000)) ; map.printAddressMap(); bus =
			 * (Bus)map; cpu = new MOS65C02A(bus);
			 * 
			 * // // example bus listener //
			 * ((AddressMap)bus).addBusListener(new BusListener() {
			 * 
			 * @Override public void readListener(short address) { }
			 * 
			 * @Override public void writeListener(short address, byte data) {
			 * if((address & 0x0000FFFF) == 0xA012) {
			 * System.out.println("DEBUG ADDRESS changed:" +
			 * AddressMap.toHexAddress(data, IOSize.IO8Bit)); } }
			 * 
			 * });
			 */

			//
			// test monitoring a cc65 based project for changes and auto update
			// and reset CPU
			//
			// this works but needs to be offically implemented
			//
			/*
			 * monitoredBinary = new FileMonitor(new
			 * File("./demo/projectB/main.bin"));
			 * monitoredBinary.addObserver(new Observer() {
			 * 
			 * @Override public void update(Observable o, Object arg) {
			 * System.out.println("monitoredBinary changed!");
			 * 
			 * clock.setEnabled(false);
			 * 
			 * ((AddressMap)bus).removeDevices();
			 * 
			 * ram = new RAMDevice(0x00000000,64*1024); AddressMap map = new
			 * AddressMapImpl((BusDevice)ram, new BusIRQ() {
			 * 
			 * @Override public void raise(int source) { if(cpu!=null)
			 * cpu.interrupt(); } }); map.addBusDevice((BusDevice)ram)
			 * .addBusDevice(new ROMDevice(0x00000000,ROMManager.loadROM(
			 * "file://./demo/projectB/main.bin"))) .addBusDevice(new
			 * DisplayDevice(0x0000A000,80,25)) .addBusDevice(new
			 * TimerDevice(0x0000B005,10)) ; map.printAddressMap(); bus =
			 * (Bus)map; if(emulatorDisplay!=null) {
			 * ((AddressMap)getBus()).addBusListener(emulatorDisplay);
			 * emulatorDisplay.refreshBus(); } cpu.setBus(bus);
			 * MainSystemEmulator.this.repaint(); }
			 * 
			 * });
			 */

			// SystemEmulator.enableDebug(true);

		}
		clock = new SystemClock();
		clock.addClockLine(clockLine);

		// Swing Stuff:
		// System.setProperty("sun.java2d.opengl", "true");
	}

	@Override
	public String getSystemVersion()
	{
		return this.versionString;
	}

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
			// System.out.println("MainSystemEmulator::ClockLine");
			cpu.clock();
			// if(cpu.getTelemetry().programCounter == 0x0075)
			// clock.setEnabled(false);
		}
	};

	private String extPath;

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

		AddressMap map = (AddressMap) bus;
		if (map != null)
		{
			for (BusDevice bd : map.getDevices())
			{
				if (bd != null)
					bd.reset();
			}
		}

		bus.reset();
	}

	@Override
	public Bus getBus()
	{
		return bus;
	}

	@Override
	public String getMainTitle()
	{
		if (this.getCPU() != null)
			return this.getCPU().getName();
		else
			return "Loading";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args)
	{
		// 
		// Define valid parameters
		//		
		CommandLineHandler cli = new CommandLineHandler(new String[][] 
		{ 
			{ "--help", "boolean", "Provides this informational message.","Java Universal System Emulator Help"}, 
			{ "--ext", "string","Specifies a directory to use for additional extensions to the emulator.\nThe directory is scanned for all jars to be added." },
			{ "--demo", "boolean", "Extracts the demonstration projects and ROM images to the 'demo' directory." },
			{ "--project", "com.juse.emulator.util.project.CC65ProjectImpl","Specifies a project associated with the emulator to build and run code." },
			{ "--ui", "string", "Specifies an optional UI class to override the default GUI" },
			{ "--config", "string", "Specifies a configuration for the emulator to use." }, 
		});

		// Usage of "--config"
		// --config ./demo/Z80.system
		//
		SystemConfig sc = null;
        //
		// Known default main frame
		//
		String uiClass = "com.juse.emulator.ui.EmulatorFrameImpl";
		//		
		//
		// Process the arguments passed in
		//
		cli.processCommandArgs(args);
		//
		// Do we need help
		//
		if(cli.isSet("--help"))
		{
			cli.doHelp();
		}
		//
		// Do we need the dom contents
		//
		if(cli.isSet("--demo"))
		{
			doDemo();
		}
		//
		// Do we want to use external libraries
		//
		if(cli.isSet("--ext"))
		{
			ProcessUtil.relauchWithExt((Class)MethodHandles.lookup().lookupClass(),
					                   cli.asString("--ext"), 
					                   Arrays.asList(args));
			return;
		}
		
		//
		// Less fancy arguments
		// What system configuration do we want
		//
		if(cli.isSet("--config"))
		{
			sc = SystemConfigLoader.loadConfiguration(cli.asString("--config"));
			//
			// if an extension dir is provided, relaunch with it, 
			// but check to see if we are already relaunched. 
			//
			if(System.getProperty("juse.relaunch")==null &&  sc.getExtensionPath()!=null)
			{
				ProcessUtil.relauchWithExt((Class)MethodHandles.lookup().lookupClass(),
		                   sc.getExtensionPath(), 
		                   Arrays.asList(args), false);
				return;				
			}
		}
		//
		// What GUI do we want to use
		//
		if(cli.isSet("--ui"))
		{
			uiClass = cli.asString("--ui");
		}

		
		//
		// Start the emulation
		//
		try
		{
			//
			// create emulator from configuration or default
			//
			emu = new SystemEmulatorEntry(sc);
			//
			// dynamically load frame
			//
			if(sc!=null && sc.getUI()!=null)
				uiClass = sc.getUI();
				
			Class UIClass = Class.forName(uiClass);			
			Constructor ctor = UIClass.getConstructor(String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
			if (ctor != null)
			{
				//
				// create requested UI
				//
				Object newFrame = ctor.newInstance(EMULATOR_TITLE, -1, -1, 1200, 1000);
				EmulatorFrame ef = (EmulatorFrame) newFrame;
				if (ef != null)
				{
					if (emu != null)
					{
						//
						// set emulator into UI
						//
						ef.initFrame(emu);
						//
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}

	protected static void doDemo()
	{
		ProcessUtil.extractResources(SystemEmulatorEntry.class, DEMO_RESOURCE, DEMO_RESOURCE);
	}

	@Override
	public String getExtensionsPath()
	{
		return extPath;
	}

	@Override
	public void setExtensionsPath(String extPath)
	{
		this.extPath = extPath;		
	}


}
