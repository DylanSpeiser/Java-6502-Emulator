package com.hadden.emulator.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class CC65ProjectImpl implements Project
{
	private String projectDir;
	private File fileProject = null;
		
	private Properties project = new Properties();
	
	private static class CCConfig
	{
		Map<String,Map<String,String>> mapSections = new HashMap<String,Map<String,String>>();
		
		public CCConfig(File configFile)
		{
			try
			{
				String source = "";
				String line = null;
				InputStream pin = new FileInputStream(configFile);
				
				BufferedReader bin = new BufferedReader(new InputStreamReader(pin));
				while(( line = bin.readLine())!=null)
				{
					String cline = line.replace(" ","");
					//System.out.println("CFGLINE:" + cline);
					source+=cline;
				}
				//System.out.println("CFGSRC:" + source);
				
				String[] sections = source.split("}");
				
				for(String s : sections)
				{
					//System.out.println("SECTIONSRC:" + s);
					String[] subsec = s.split("\\{");
					
					String name = subsec[0];
					String data = subsec[1];
					
					//System.out.println("\tSNAME:" + name);
					Map<String,String> sectionData = new HashMap<String,String>();
					mapSections.put(name,sectionData);
					//System.out.println("\tSDATA:" + data);
					for(String ds : data.split(";"))
					{
						
						//System.out.println("\tSS:" + ds);
						String[] named = ds.substring(ds.indexOf(":")+1).split(",");
						for(String dds : named)
						{
							//System.out.println("\t\tDDS:" + dds);
							String[] kvs = dds.split("=");

							//System.out.println("\t\t\t" + kvs[0] + "=" + kvs[1]);
							sectionData.put(kvs[0],kvs[1]);
						}
					}				
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
			System.out.println();
		}
	}
	
	public CC65ProjectImpl(String projectDir)
	{
		fileProject = new File(projectDir);
		
		try
		{
			System.out.println("ProjectImpl::ProjectImpl:" + fileProject.getCanonicalPath());
		} 
		catch (IOException e2)
		{
			e2.printStackTrace();
		}
		
		this.projectDir = projectDir;
		
		File file = new File(this.projectDir);
		if(file.exists())
		{
			File[] files = file.listFiles();
			for(File f : files)
			{
				try
				{
					System.out.println("FILE:" + f.getCanonicalPath());
				} 
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(f.getName().toLowerCase().endsWith(".proj"))
				{
					FileInputStream fin;
					try
					{
						fin = new FileInputStream(f);
						try
						{
							project.load(fin);
							System.out.println("PROJECT NAME:" + project.getProperty("project.name"));
							System.out.println("PROJECT BIN:"  + project.getProperty("project.tool.bin"));
							
						} 
						catch (Exception e)
						{
							e.printStackTrace();
						}
						finally
						{
							try
							{
								fin.close();
							} 
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
					} 
					catch (FileNotFoundException e)
					{
						e.printStackTrace();
					}
					
					
				}
			}
		}
	}

	@Override
	public File[] getFiles()
	{
		File[] files = null;
		
		File file = new File(this.projectDir);
		if(file.exists())
		{
			files = file.listFiles();
		}
		
		return files;
	}

	@Override
	public int compile()
	{
		File[] files = getFiles();
		
		List<String> commandline = new Vector<String>();
		
		
		commandline.add(project.getProperty("project.tool.bin") + File.separator + "ca65");
		commandline.add("--debug");
		for(File s : files)
		{
			try
			{
				if(s.getName().endsWith(".s"))
				{
					commandline.add(s.getName());
					commandline.add("-o");
					commandline.add(s.getName().replace(".s",".o"));
				}
			} 
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//System.out.println("SRC:" + src);
		
		//commandline.add(cfg);
		for(String s : commandline)
			System.out.println("commandline:" + s);
		
		ProcessBuilder pb = new ProcessBuilder(commandline.toArray(new String[commandline.size()]));
		try
		{
			pb.directory(new File(this.projectDir));
			pb.redirectErrorStream(true);
			
			Process p = pb.start();	

			String line = null;
			
			InputStream pin = p.getInputStream();
			
			BufferedReader bin = new BufferedReader(new InputStreamReader(pin));
			while(( line = bin.readLine())!=null)
			{
				System.out.println("COMPILE:" + line);
			}
			p.waitFor();
			
		}
		catch(Exception e)
		{
			
		}		return 0;
	}

	@Override
	public int link()
	{
		File[] files = getFiles();
		
		String src = "";
		String cfg = "";

		List<String> commandline = new Vector<String>();
		commandline.add(project.getProperty("project.tool.bin") + File.separator + "cl65");
		commandline.add("--verbose");
		
		for(File s : files)
		{
			try
			{
				if(s.getCanonicalPath().endsWith(".o"))
				{
					if(src.length() >0)
						src+=" ";
					src+=(s);
					commandline.add(s.getName());
				}
				else if(s.getCanonicalPath().endsWith(".cfg"))
				{
					CCConfig ccfg = new CCConfig(s);
					//cfg = "--config ";
					//cfg += s.getCanonicalPath();
					commandline.add("--config");
					commandline.add(s.getName());
				}
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("CFG:" + cfg);
		System.out.println("SRC:" + src);
		
		//commandline.add(cfg);
		
		for(String s : commandline)
			System.out.println("commandline:" + s);
		
		try
		{
			ProcessBuilder pb = new ProcessBuilder(commandline.toArray(new String[commandline.size()]));
			try
			{
				pb.directory(new File(this.projectDir));
				pb.redirectErrorStream(true);
				
				Process p = pb.start();
				
				String line = null;
				
				InputStream pin = p.getInputStream();
				
				BufferedReader bin = new BufferedReader(new InputStreamReader(pin));
				while(( line = bin.readLine())!=null)
				{
					System.out.println("LINK:" + line);
				}
				p.waitFor();
				
				
			} 
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}

	public static void main(String[] args)
	{
		CC65ProjectImpl cc = new CC65ProjectImpl(args[0]);
		cc.compile();
		cc.link();
	}
	
}
