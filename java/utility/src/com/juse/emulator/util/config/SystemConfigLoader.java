package com.juse.emulator.util.config;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.juse.emulator.interfaces.AddressMap;
import com.juse.emulator.interfaces.Bus;
import com.juse.emulator.interfaces.BusDevice;
import com.juse.emulator.interfaces.BusIRQ;
import com.juse.emulator.interfaces.CPU;
import com.juse.emulator.interfaces.ui.EmulatorFrame;



public class SystemConfigLoader
{
	private SystemConfigLoader()
	{
		
	}	
	
	private static String readFile(String path, Charset encoding) throws IOException
	{
	  byte[] encoded = Files.readAllBytes(Paths.get(path));
	  return new String(encoded, encoding);
	}	
	
	public static AddressMap loadSystem(String configPathName)
	{
		CPU cpu = null;
		Class<CPU> cpuClass = null;
		Class<EmulatorFrame> uiClass = null;
		AddressMap addressMap = null;
		BusIRQ irqbus = null;
		String defaultAddressor = null;
		
		Map<String,Class> devices = new HashMap<String,Class>();
		Map<String,BusDevice> loadedDevices = new HashMap<String,BusDevice>();
		Map<String,String[]> bus     = new HashMap<String,String[]>();
		
		
		File configFile = new File(configPathName);
		if(configFile!=null && configFile.exists())
		{
			try
			{
				try
				{
					String content =  readFile(configPathName,Charset.forName("utf-8"));
					//System.out.println(content);
					
					String[] lines = content.split("\n");
					for(String line : lines)
					{
						line = line.trim();
						if(!line.startsWith("#"))
						{
							line = line.replace("\r","");
							// System.out.println(line);
							String[] parsed = line.split("=");
							if(parsed.length == 2)
							{
								String key   = parsed[0]; 
								String value = parsed[1];
								
								System.out.println("[" + key + "][" + value + "]");
								
								if("device".equalsIgnoreCase(key))
								{
									String[] attrs = value.split(",");
									if(attrs.length > 1)
									{
										System.out.print("DEVICE ATTR:");
										for(String attr : attrs)
											System.out.print("[" + attr + "]");
										System.out.println();
										devices.put(attrs[0], Class.forName(attrs[1]));
									}
								}
								else if("bus".equalsIgnoreCase(key))
								{
									String[] attrs = value.split(",");
									if(attrs.length > 1)
									{
										System.out.print("BUS ATTR:");
										for(String attr : attrs)
											System.out.print("[" + attr + "]");
										System.out.println();
										bus.put(attrs[0], attrs);
									}
								}
								else if("addressbus".equalsIgnoreCase(key))
								{
									addressMap = (AddressMap)(Class.forName(value).newInstance());
									System.out.println("addressMap:" + addressMap.getClass().getCanonicalName());
								}							
								else if("irqbus".equalsIgnoreCase(key))
								{
									irqbus = (BusIRQ)(Class.forName(value).newInstance());
									System.out.println("irqbus:" + irqbus.getClass().getCanonicalName());
								}	
								else if("cpu".equalsIgnoreCase(key))
								{
									cpuClass = (Class<CPU>) (Class.forName(value));
									System.out.println("CPU:" + cpuClass.getClass().getCanonicalName());
								}	
								else if("ui".equalsIgnoreCase(key))
								{
									uiClass = (Class<EmulatorFrame>) (Class.forName(value));
									System.out.println("UI:" + uiClass.getClass().getCanonicalName());
								}	
								else if("default".equalsIgnoreCase(key))
								{
									defaultAddressor = value;
									System.out.println("defaultAddressor:" + defaultAddressor);
								}									
							}
						}
					}
					

					// load bus
					
					for(String name : bus.keySet())
					{
						if(devices.containsKey(name))
						{
							System.out.println("Found " + name + " in devices.");
							Class cls = devices.get(name);
							Constructor[] cstor = cls.getConstructors();
							for(Constructor c : cstor)
							{
								boolean bClassDetected = false;
								System.out.print("\tcstor:" + c.getName() + ":" + c.getParameterCount());
								
								for(Parameter p : c.getParameters())
									if(p.getType().toString().startsWith("class"))
										bClassDetected = true;
								
								if(!bClassDetected && c.getParameterCount() == bus.get(name).length - 1)
								{
									int carg = 0;
									Object[] args = new Object[c.getParameterCount()];
									String[] attrs = bus.get(name);
									
									System.out.print("* ");
									for(Parameter p : c.getParameters())
									{												
										System.out.print("<" + p.getType().toString() + "> ");
										if("int".equals(p.getType().toString()))
										{
											int radix = 10;
											
											String value = attrs[carg+1];
											if(value.startsWith("0x"))
											{
												radix = 16;
												value = value.replace("0x","");
											}
												
											args[carg] = Integer.parseInt(value,radix);
											
											carg++;
										}	
										else
										{
											args[carg++] = attrs[carg];
										}
									}
									System.out.println("");
									
									
									BusDevice bd = (BusDevice)c.newInstance(args);
									System.out.println(bd.getName());
									loadedDevices.put(name, bd);
									
								}
								else
									System.out.println("");
							}
						}
						else
						{
							System.out.println("Missing " + name + " in devices.");
						}
					}					
					
					if(addressMap!=null)
					{
						System.out.println("Using AddressMap:" + addressMap.getClass().getCanonicalName());
						if(defaultAddressor!=null)
						{
							System.out.println("Using defaultAddressor:" + loadedDevices.get(defaultAddressor).getClass().getCanonicalName());
							addressMap.setDefaultDevice(loadedDevices.get(defaultAddressor));
						}

						if(irqbus!=null)
						{
							System.out.println("Using irqbus:" + irqbus.getClass().getCanonicalName());
							addressMap.setIRQHandler(irqbus);
						}	
						
						for(String name : bus.keySet())
						{
							System.out.println("Adding device " + name + " to bus.");
							addressMap.addBusDevice(loadedDevices.get(name));
						}
						
					
						addressMap.printAddressMap();
					}					
					
					if(cpuClass!=null)
					{
						cpu = cpuClass.getConstructor(Bus.class).newInstance(addressMap);
						
					}
					
				}
				finally
				{
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}
		else
		{
			System.out.println("System configuration file missing");
		}
		
		
		return addressMap;
	}

	
	public static SystemConfig loadConfiguration(String configPathName)
	{
		SystemConfig sc = new SystemConfig(); 
		
		File configFile = new File(configPathName);
		if(configFile!=null && configFile.exists())
		{
			try
			{
				try
				{
					String content =  readFile(configPathName,Charset.forName("utf-8"));
					//System.out.println(content);
					
					String[] lines = content.split("\n");
					for(String line : lines)
					{
						line = line.trim();
						if(!line.startsWith("#"))
						{
							line = line.replace("\r","");
							// System.out.println(line);
							String[] parsed = line.split("=");
							if(parsed.length == 2)
							{
								String key   = parsed[0]; 
								String value = parsed[1];
								
								//System.out.println("[" + key + "][" + value + "]");
								
								if("device".equalsIgnoreCase(key))
								{
									String[] attrs = value.split(",");
									if(attrs.length > 1)
									{
										//System.out.print("DEVICE ATTR:");
										//for(String attr : attrs)
										//	System.out.print("[" + attr + "]");
										//System.out.println();

										
										sc.addDevice(attrs[0], Class.forName(attrs[1]));
										
									}
								}
								else if("bus".equalsIgnoreCase(key))
								{
									String[] attrs = value.split(",");
									if(attrs.length > 1)
									{
										//System.out.print("BUS ATTR:");
										//for(String attr : attrs)
										//	System.out.print("[" + attr + "]");
										//System.out.println();
										sc.addMapping(attrs[0], attrs);
										
									}
								}
								else if("addressbus".equalsIgnoreCase(key))
								{
									//addressMap = (AddressMap)(Class.forName(value).newInstance());
									sc.setBus(value);
									//System.out.println("addressMap:" + addressMap.getClass().getCanonicalName());
								}							
								else if("irqbus".equalsIgnoreCase(key))
								{
									//irqbus = (BusIRQ)(Class.forName(value).newInstance());
									sc.setIRQ(value);
									//System.out.println("irqbus:" + irqbus.getClass().getCanonicalName());
								}	
								else if("cpu".equalsIgnoreCase(key))
								{
									sc.setCPU(value);
								}	
								else if("ui".equalsIgnoreCase(key))
								{
									sc.setUI(value);
								}	
								else if("ext".equalsIgnoreCase(key))
								{
									if(value.startsWith("."))
									{
										String scfn = configFile.getName();
										String gn = configFile.getCanonicalPath();
										value = gn.replace(scfn, value);
										
										File nxt = new File(value);
										if(nxt.exists())
										{
											value = nxt.getCanonicalPath();
										}	
										else
										{
											value = null;
										}										
									}
									sc.setExtensionPath(value);
								}
								else if("default".equalsIgnoreCase(key))
								{
									//defaultAddressor = value;
									//System.out.println("defaultAddressor:" + defaultAddressor);
									sc.setDefAddressDevice(value);
								}									
							}
						}
					}
					

					// load bus
					/*
					for(String name : bus.keySet())
					{
						if(devices.containsKey(name))
						{
							System.out.println("Found " + name + " in devices.");
							Class cls = devices.get(name);
							Constructor[] cstor = cls.getConstructors();
							for(Constructor c : cstor)
							{
								boolean bClassDetected = false;
								System.out.print("\tcstor:" + c.getName() + ":" + c.getParameterCount());
								
								for(Parameter p : c.getParameters())
									if(p.getType().toString().startsWith("class"))
										bClassDetected = true;
								
								if(!bClassDetected && c.getParameterCount() == bus.get(name).length - 1)
								{
									int carg = 0;
									Object[] args = new Object[c.getParameterCount()];
									String[] attrs = bus.get(name);
									
									System.out.print("* ");
									for(Parameter p : c.getParameters())
									{												
										System.out.print("<" + p.getType().toString() + "> ");
										if("int".equals(p.getType().toString()))
										{
											int radix = 10;
											
											String value = attrs[carg+1];
											if(value.startsWith("0x"))
											{
												radix = 16;
												value = value.replace("0x","");
											}
												
											args[carg] = Integer.parseInt(value,radix);
											
											carg++;
										}	
										else
										{
											args[carg++] = attrs[carg];
										}
									}
									System.out.println("");
									
									
									BusDevice bd = (BusDevice)c.newInstance(args);
									System.out.println(bd.getName());
									loadedDevices.put(name, bd);
									
								}
								else
									System.out.println("");
							}
						}
						else
						{
							System.out.println("Missing " + name + " in devices.");
						}
					}					
					*/
					
					/*
					if(addressMap!=null)
					{
						System.out.println("Using AddressMap:" + addressMap.getClass().getCanonicalName());
						if(defaultAddressor!=null)
						{
							System.out.println("Using defaultAddressor:" + loadedDevices.get(defaultAddressor).getClass().getCanonicalName());
							addressMap.setDefaultDevice(loadedDevices.get(defaultAddressor));
						}

						if(irqbus!=null)
						{
							System.out.println("Using irqbus:" + irqbus.getClass().getCanonicalName());
							addressMap.setIRQHandler(irqbus);
						}	
						
						for(String name : bus.keySet())
						{
							System.out.println("Adding device " + name + " to bus.");
							addressMap.addBusDevice(loadedDevices.get(name));
						}
						
					
						addressMap.printAddressMap();
					}					

					if(cpuClass!=null)
					{
						cpu = cpuClass.getConstructor(Bus.class).newInstance(addressMap);
						
					}

					*/
					
				}
				finally
				{
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}
		else
		{
			System.out.println("System configuration file missing");
		}
		
		
		return sc;
	}
	
	
	public static void main(String[] args)
	{
		if(args.length > 1)
		{
			for(int i=0;i<args.length;i++)
			{
				String s = args[i];
				//System.out.println(s);
				if("--config".equals(s))
				{
					i++;
					if(i < args.length)
					{						
						String configPathName = args[i];
						SystemConfig sc = loadConfiguration(configPathName);
						if(sc!=null)
						{
							for(String dname : sc.getMapped().keySet())
							{
								//BusDevice bd = sc.initBusDevice(dname);
								System.out.println("DEVICE:" + dname);
							}
						}
					}
					System.exit(0);
				}
			}
		}
		else
		{
			String configPathName = System.getenv("SEMU_CFG_DIR") + File.separator + "default.system";
			loadSystem(configPathName);
		}
		
		
	}
}
