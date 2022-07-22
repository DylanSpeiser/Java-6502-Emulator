package com.hadden.emu;

public interface CPU
{
	public enum ClockRateUnit
	{
		HZ,
		KHZ,
		MHZ
	}
	
	public String getName();
	public void interrupt();
	public void reset();
	public void clock();
	public int rate(ClockRateUnit unit);
	public int register(String name);
	public int flags();
	public int cycles();
	public int opcode();
	public String opcodeMnemonic(int opcode);
	public int relativeAddress();
	public int absoluteAddress();
}
