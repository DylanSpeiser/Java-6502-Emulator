package com.hadden.emulator.project;

import java.io.File;

public class ProjectImpl implements Project
{
	private String projectDir;

	public ProjectImpl(String projectDir)
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
	public File[] getFiles()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compile()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int link()
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
