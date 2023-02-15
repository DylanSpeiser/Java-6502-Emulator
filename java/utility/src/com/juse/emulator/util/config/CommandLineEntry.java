package com.juse.emulator.util.config;

import java.util.Map;

public abstract class CommandLineEntry
{
	public interface CommandLineHander
	{
		
	}
	
	protected abstract Map<String,CommandLineHander> getHandlers();	
}
