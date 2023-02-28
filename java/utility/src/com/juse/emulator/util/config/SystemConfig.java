package com.juse.emulator.util.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.juse.emulator.interfaces.Bus;
import com.juse.emulator.interfaces.BusDevice;
import com.juse.emulator.interfaces.BusIRQ;
import com.juse.emulator.interfaces.CPU;



public class SystemConfig
{
	String clsCPU    = null;	
	String clsBus    = null;
	String defDevice = null;
	String irqDevice = null;
	String slots     = null;
	String clsUI     = null;
	String extPath   = null;
	
	Map<String,Class<BusDevice>> devices = new HashMap<String,Class<BusDevice>>();
	Map<String,String[]>         mapped  = new LinkedHashMap<String,String[]>();
	
	

	public void setUI(String className)
	{
		this.clsUI = className;
	}
	
	public String getUI()
	{
		return this.clsUI;
	}
	
	public void setCPU(String className)
	{
		this.clsCPU = className;
	}
	
	private String getCPU()
	{
		return this.clsCPU;
	}

	public void setIRQ(String className)
	{
		this.irqDevice = className;
	}

	public String getIRQ()
	{
		return this.irqDevice;
	}
	
	public void setBus(String className)
	{
		this.clsBus = className;
	}	

	public String getBus()
	{
		return this.clsBus;
	}
	
	public void setDefAddressDevice(String className)
	{
		this.defDevice = className;
	}	

	public String getDefAddressDevice()
	{
		return this.defDevice;
	}	
	
	public void addDevice(String name,Class clz)
	{
		devices.put(name,clz);
	}
	
	public void addMapping(String name,String[] arguments)
	{
		mapped.put(name,arguments);
	}	

	public Bus initBus(BusDevice defaultDevice, BusIRQ defaultIRQHandler)
	{
		try
		{
			Class cls = Class.forName(getBus());
			Constructor[] cstor = cls.getConstructors();
			for(Constructor c : cstor)
			{
				boolean bClassDetected = false;
				//System.out.print("\tcstor:" + c.getName() + ":" + c.getParameterCount());
				
				for(Parameter p : c.getParameters())
					if(p.getType().toString().startsWith("class"))
						bClassDetected = true;
				
				if(!bClassDetected && c.getParameterCount() == 2)
				{
					Object[] args = new Object[c.getParameterCount()];
					
					args[0] = defaultDevice;
					args[1] = defaultIRQHandler;
					
					Bus bd = null;
					try
					{
						bd = (Bus)c.newInstance(args);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					
					return (Bus) bd;
				}
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		return null;
	}

	public CPU initCPU(Bus bus) throws Exception
	{
		try
		{
			Class cls = Class.forName(getCPU());
			Constructor[] cstor = cls.getConstructors();
			for(Constructor c : cstor)
			{
				boolean bClassDetected = false;
				//System.out.print("\tcstor:" + c.getName() + ":" + c.getParameterCount());
				
				for(Parameter p : c.getParameters())
					if(p.getType().toString().startsWith("class"))
						bClassDetected = true;
				
				if(c.getParameterCount() == 1)
				{
					Object[] args = new Object[c.getParameterCount()];
					
					args[0] = bus;
					
					CPU cpu = null;
					try
					{
						cpu = (CPU)c.newInstance(args);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					
					return cpu;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Exception("Bad CPU definition.");
		}	
		
		return null;
	}

	public BusDevice initBusDevice(String name)
	{
		Class cls = this.devices.get(name);
		
		int ml = mapped.get(name).length - 1;
		//System.out.println("ml:" + ml);
		
		Constructor[] cstor = cls.getConstructors();
		for(Constructor c : cstor)
		{
			boolean bClassDetected = false;
			//System.out.print("\tcstor:" + c.getName() + ":" + c.getParameterCount());
			
			for(Parameter p : c.getParameters())
			{
				//System.out.print("<" + p.getType().toString() + "> ");
				if(p.getType().toString().startsWith("class"))
					if(!p.getType().toString().startsWith("class java.lang.String"))
						bClassDetected = true;
			}
			//System.out.println();
			
			if(!bClassDetected && c.getParameterCount() == ml)
			{
				int carg = 0;
				Object[] args = new Object[c.getParameterCount()];
				String[] attrs = mapped.get(name);
				
				//System.out.print("* ");
				for(Parameter p : c.getParameters())
				{												
					//System.out.print("<" + p.getType().toString() + "> ");
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
				//System.out.println("");
				
				
				BusDevice bd = null;
				try
				{
					bd = (BusDevice)c.newInstance(args);
					//System.out.println(bd.getName());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				return bd;
			}
		}	
		
		return null;
	}

	public Map<String, String[]> getMapped()
	{
		return mapped;
	}

	public void setExtensionPath(String extPath)
	{
		//System.out.println("setExtensionPath:" + extPath );
		this.extPath = extPath;
	}
	
	public String getExtensionPath()
	{
		//System.out.println("getExtensionPath:" + extPath );
		return this.extPath;
	}
}
