package com.juse.emulator.util.config;

import java.util.HashMap;
import java.util.Map;

public class CommandLineHandler
{
	public static class CommandItem
	{
		public String name;
		public Class cls;
		public String help;
		public boolean optional = true;
		public String title = null;
	}
	
	private String[][] definedCommandParameters = null;
	private Map<String, Object> acceptedArgs = null;
	
	public CommandLineHandler(String[][] definedCommandParameters)
	{
		this.definedCommandParameters = definedCommandParameters;
	}
	
	private String[][] getDefinedCommands()
	{
		return definedCommandParameters;
	}

	private Map<String, CommandItem> convertDefinedArgs(String[][] definedArgs)
	{
		Map<String, CommandItem> newrgs = new HashMap<String, CommandItem>();

		for (String[] line : definedArgs)
		{

			CommandItem ci = new CommandItem();

			String var = line[0];
			String val = line[1];
			// System.out.println(var + " = " + val);

			try
			{
				if (!val.contains("."))
				{
					val = "java.lang." + new String("" + val.charAt(0)).toUpperCase() + val.toLowerCase().substring(1);
				}

				ci.name = var;
				ci.cls = Class.forName(val);
				if (line.length > 2)
					ci.help = line[2];
				else
					ci.help = "No description available";

				if (line.length > 3)
					ci.title = line[3];
				
				newrgs.put(var, ci);
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}

		}
		return newrgs;
	}

	public Map<String, Object> processCommandArgs(String[] args)
	{
		Map<String, CommandItem> definedArgs = convertDefinedArgs(getDefinedCommands()); 
		
		Map<String, Object> acceptedArgs = new HashMap<String, Object>();

		//
		// set all boolean args to false
		//
		for (String key : definedArgs.keySet())
		{
			CommandItem ci = definedArgs.get(key);
			Class dtype = ci.cls;
			if (dtype == Boolean.class)
			{
				acceptedArgs.put(key, Boolean.FALSE);
			}
		}

		if (args.length > 0)
		{
			for (int i = 0; i < args.length; i++)
			{
				if (definedArgs.containsKey(args[i]))
				{
					CommandItem ci = definedArgs.get(args[i]);

					Class dtype = ci.cls;
					if (dtype == Boolean.class)
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
							acceptedArgs.put(key, dtype.getConstructor(String.class).newInstance(value));
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}

			}
		}
		
		this.acceptedArgs = acceptedArgs;
		
		return acceptedArgs;
	}

	public boolean isSet(String parameter)
	{
		if(acceptedArgs!=null)
		{
			Object pv = acceptedArgs.get(parameter);
			if(pv != null)
			{
				if(pv instanceof Boolean)
				{
					return (Boolean)pv;
				}
				else
				{
					return true;
				}
			}
		}
		return false;
	}

	public String asString(String parameter)
	{
		if(acceptedArgs!=null)
		{
			Object pv = acceptedArgs.get(parameter);
			if(pv != null)
			{
				return pv.toString();
			}
		}
		return null;
	}	


	public int asInt(String parameter)
	{
		if(acceptedArgs!=null)
		{
			Object pv = acceptedArgs.get(parameter);
			if(pv != null)
			{
				return Integer.parseInt(pv.toString());
			}
		}
		return -1;
	}	
	
	
	public Object asObject(String parameter)
	{
		if(acceptedArgs!=null)
		{
			return acceptedArgs.get(parameter);
		}
		return null;
	}	
	
	public void doHelp()
	{
		Map<String, CommandItem> converted = convertDefinedArgs(getDefinedCommands());

		CommandItem help = converted.get("--help");
		
		System.out.println();
		if(help.title!=null)
		{
			System.out.println(help.title);
			System.out.println("===================================");
		}
		
		for (String s : converted.keySet())
		{
			CommandItem ci = converted.get(s);
			String helpText = ci.help;
			if(helpText.contains("\n"))
				helpText = helpText.replace("\n","\n\t\t");
			System.out.println("\t" + s + ":\n\t\t" + helpText + "\n");
		}
		System.exit(0);
	}	
	
}
