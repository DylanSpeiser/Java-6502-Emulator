package com.hadden;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import com.hadden.emu.Bus;
import com.hadden.emu.BusDevice;
import com.hadden.emu.CPU;

public class SystemConfig
{
	String clsCPU    = null;	
	String clsBus    = null;
	String defDevice = null;
	String irqDevice = null;
	String slots     = null;
	
	Map<String,Class<BusDevice>> devices = new HashMap<String,Class<BusDevice>>();
	Map<String,String[]>         mapped  = new HashMap<String,String[]>();
	

	public void setCPU(String className)
	{
		this.clsCPU = className;
	}

	public void setIRQ(String className)
	{
		this.irqDevice = className;
	}

	public void setBus(String className)
	{
		this.clsBus = className;
	}	

	public void setDefAddressDevice(String className)
	{
		this.defDevice = className;
	}	
	
	public void addDevice(String name,Class clz)
	{
		devices.put(name,clz);
	}
	
	public void addMapping(String name,String[] arguments)
	{
		mapped.put(name,arguments);
	}	
	
	public BusDevice initBusDevice(String name)
	{
		Class cls = this.devices.get(name);
		Constructor[] cstor = cls.getConstructors();
		for(Constructor c : cstor)
		{
			boolean bClassDetected = false;
			System.out.print("\tcstor:" + c.getName() + ":" + c.getParameterCount());
			
			for(Parameter p : c.getParameters())
				if(p.getType().toString().startsWith("class"))
					bClassDetected = true;
			
			if(!bClassDetected && c.getParameterCount() == mapped.get(name).length - 1)
			{
				int carg = 0;
				Object[] args = new Object[c.getParameterCount()];
				String[] attrs = mapped.get(name);
				
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
				
				
				BusDevice bd = null;
				try
				{
					bd = (BusDevice)c.newInstance(args);
					System.out.println(bd.getName());
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
	
}
