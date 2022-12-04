package com.hadden.emulator.project;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class CC65ProjectImpl implements Project
{
	private String projectDir;

	public CC65ProjectImpl(String projectDir)
	{
		System.out.println("ProjectImpl::ProjectImpl:" + projectDir);
		this.projectDir = projectDir;
		
		File file = new File(this.projectDir);
		if(file.exists())
		{
			String[] files = file.list();
			for(String f : files)
				System.out.println("FILE:" + f);
		}
	}

	@Override
	public String[] getFiles()
	{
		String[] files = null;
		
		File file = new File(this.projectDir);
		if(file.exists())
		{
			files = file.list();
		}
		
		return files;
	}

	@Override
	public int compile()
	{
		String[] files = getFiles();
		
		String src = "";

		List<String> commandline = new Vector<String>();
		commandline.add("cc65");
		
		for(String s : files)
		{
			if(s.endsWith("s"))
			{
				if(src.length() >0)
					src+=" ";
				src+=(s);
				commandline.add(projectDir + File.separator + s);
				commandline.add("-o");
				commandline.add(projectDir + File.separator + s.replace(".s",".o"));
			}
		}
		System.out.println("SRC:" + src);
		
		//commandline.add(cfg);
		
		
		
		for(String s : commandline)
			System.out.println("commandline:" + s);
		try
		{
			Runtime.getRuntime().exec(commandline.toArray(new String[commandline.size()]));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int link()
	{
		String[] files = getFiles();
		
		String src = "";
		String cfg = "";

		List<String> commandline = new Vector<String>();
		commandline.add("cl65");
		
		for(String s : files)
		{
			if(s.endsWith("o"))
			{
				if(src.length() >0)
					src+=" ";
				src+=(s);
				commandline.add(s);
			}
			else if(s.endsWith("cfg"))
			{
				cfg = "--config ";
				cfg += s;
			}
		}
		System.out.println("CFG:" + cfg);
		System.out.println("SRC:" + src);
		
		//commandline.add(cfg);
		
		for(String s : commandline)
			System.out.println("commandline:" + s);
		
		try
		{
			Runtime.getRuntime().exec(commandline.toArray(new String[commandline.size()]));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}

}
