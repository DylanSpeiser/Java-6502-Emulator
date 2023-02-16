package com.juse.emulator.debug;

import com.juse.emulator.interfaces.IOSize;

public interface DebugListener
{
	public static enum DebugReason
	{
		Step,
		Clock,
		Break
	}
	
	public static enum DebugCode
	{
		None
	}
	
	DebugCode debugEvent(DebugReason dr, byte data, IOSize size);
	DebugCode debugEvent(DebugReason dr, short data, IOSize size);
	DebugCode debugEvent(DebugReason dr, int data, IOSize size);
	DebugCode debugEvent(DebugReason dr, long data, IOSize size);
}
