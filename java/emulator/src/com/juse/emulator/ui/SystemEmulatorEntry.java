package com.juse.emulator.ui;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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
import com.juse.emulator.interfaces.Project;
import com.juse.emulator.util.config.ResourceCopy;
import com.juse.emulator.util.config.SystemConfig;
import com.juse.emulator.util.config.SystemConfigLoader;
import com.juse.emulator.util.loaders.ROMManager;
import com.juse.emulator.util.project.CC65ProjectImpl;


@SuppressWarnings("serial")
public class SystemEmulatorEntry implements Emulator
{
	public String versionString = "1.0";

	private static SystemEmulatorEntry emu;
	
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
	
	
	public SystemEmulatorEntry(SystemConfig configuration)
	{
		init(configuration);		
		//initDisplay();	
	}

	
	public SystemEmulatorEntry()
	{
		this(null);
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
			// create default address handler device for a 6502
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
		clock = new SystemClock();
		clock.addClockLine(clockLine);
		
		// Swing Stuff:
		//System.setProperty("sun.java2d.opengl", "true");
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
		
		bus.reset();
		//this.emulatorDisplay.redraw();
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args)
	{
		// Usage of "--config"
		// --config ./demo/Z80.system
		//
		String configFile = null;
		String projectDir = null;
		
		SystemConfig sc = null;

		
		String uiClass = "com.juse.emulator.ui.EmulatorFrameImpl";
		
//		Map<String,Class> definedArgs = new HashMap<String,Class>();
//		
//		definedArgs.put("--demo", Boolean.class);
//		definedArgs.put("--project", String.class);
//		definedArgs.put("--ui", String.class);
//		definedArgs.put("--config", String.class);
//
//		
//		Map<String,String> dargs = processArgs(args,definedArgs);
		
		String[][] definedArgArray = new String[][] 
		{
			{"--help","boolean", "Provides this informational message."},
			{"--demo","boolean", "Extracts the demonstration projects and ROM images to the 'demo' directory."},
			{"--demo1","boolean", "Fake parameter 1"},
			{"--demo2","boolean"},
			{"--project","com.juse.emulator.util.project.CC65ProjectImpl", "Specifies a project associated with the emulator to build and run code."},
			{"--ui","string",  "Specifies an optional UI class to override the default GUI"},
			{"--config","string", "Specifies a configuration for the emulator to use."},
		};
		
		
		Map<String, CommandItem> converted = convertDefinedArgs(definedArgArray);
		Map<String,Object> sargs = processArgs(args,converted);

		
		if((Boolean)sargs.get("--help"))
		{
			System.out.println();
			System.out.println("Java Universal System Emulator Help");
			System.out.println("===================================");
			for(String s : converted.keySet())
			{
				CommandItem ci = converted.get(s);
				String helpText = ci.help;
				System.out.println("\t" + s + ":\n\t\t" + helpText + "\n");
			}
			return;
		}		
		
		if((Boolean)sargs.get("--demo"))
		{
			doDemo();
		}
		if(sargs.get("--config") != null)
		{
			sc = SystemConfigLoader.loadConfiguration((String)sargs.get("--config"));
		}
		if(sargs.get("--ui") != null)
		{
			uiClass = (String)sargs.get("--ui");
		}
		/*
		if(args.length > 0)
		{
			for(int i=0;i<args.length;i++)
			{
				//System.out.println("Args:" + args[i]);
				if("--ui".equals(args[i]))
				{
					i++;
					uiClass = args[i];
				}

				
				if("--demo".equals(args[i]))
				{
				    doDemo();
				}				
				
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
		*/
		
		try
		{
			Class UIClass = Class.forName(uiClass);
			
			//
			// create emulator from configuration or default
			//
			emu = new SystemEmulatorEntry(sc);
			//
			// dynamically load frame
			//
			Constructor ctor = UIClass.getConstructor(String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
			if(ctor!=null)
			{
				//
				// create requested UI
				//
				Object newFrame = ctor.newInstance("Java System Emulator", -1, -1, 1920,1200);				
				EmulatorFrame ef = (EmulatorFrame) newFrame;
				if(ef!=null)
				{			
					if(emu!=null)
					{
						//
						// set emulator into UI
						//
						ef.initFrame(emu);
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	protected static void doDemo()
	{
		URL fromURL = SystemEmulatorEntry.class.getClassLoader().getResource("demo");
		File toPath = new File("./demo");
		
		String url = fromURL.toString();
		String jarName = url.substring(0,url.indexOf('!')).replace("jar:file:","");
		
		try
		{				    	 
		    //System.out.println("fromURL:" + fromURL);
		    //System.out.println("toPath:" + toPath.getAbsolutePath());
		    System.out.println("Copying demo directory:\nFrom: " + jarName + "\nTo  : " + toPath.getCanonicalPath());
		    
			ResourceCopy rc = new ResourceCopy();
			rc.copyResourceDirectory(new JarFile(jarName), "demo", toPath);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static Map<String, Object> processArgs(String[] args, String[][] definedArgs)
	{
		return processArgs(args, convertDefinedArgs(definedArgs));
	}


	protected static Map<String, CommandItem> convertDefinedArgs(String[][] definedArgs)
	{
		Map<String,CommandItem> newrgs = new HashMap<String,CommandItem>();
		
		
		for(String[] line : definedArgs)
		{			
			
			CommandItem ci = new CommandItem();
			
			String var = line[0];
			String val = line[1];
			//System.out.println(var + " = " + val);
			
			try
			{
				if(!val.contains("."))
				{
					val = "java.lang." + new String("" + val.charAt(0)).toUpperCase() + val.toLowerCase().substring(1);
				}
				
				ci.name = var;
				ci.cls  = Class.forName(val);
				if(line.length > 2)
					ci.help = line[2];
				else
					ci.help = "No description available";
				
				newrgs.put(var, ci);
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			
		}
		return newrgs;
	}
	
	private static Map<String, Object> processArgs(String[] args, Map<String,CommandItem> definedArgs)
	{
		Map<String, Object> acceptedArgs = new HashMap<String, Object>();
		
		//
		// set all boolean args to false
		//
		for(String key : definedArgs.keySet())
		{
			CommandItem ci = definedArgs.get(key);
			Class dtype = ci.cls;
			if(dtype == Boolean.class)
			{
				acceptedArgs.put(key, Boolean.FALSE);
			}			
		}
		
		
		if(args.length > 0)
		{
			for(int i=0;i<args.length;i++)
			{
				if( definedArgs.containsKey(args[i]) )
				{
					CommandItem ci = definedArgs.get(args[i]);
					
					Class dtype = ci.cls;
					if(dtype == Boolean.class)
					{
						acceptedArgs.put(args[i], Boolean.TRUE);
					}
					else
					{
						String key = args[i];
						i++;
						
						String value = args[i];
						try
						{
							acceptedArgs.put(key,dtype.getConstructor(String.class).newInstance(value));
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}

			}
		}
		return acceptedArgs;
	}
	
	
}
