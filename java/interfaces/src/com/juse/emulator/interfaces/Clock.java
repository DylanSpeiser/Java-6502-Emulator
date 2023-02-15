package com.juse.emulator.interfaces;

public interface Clock
{
	boolean isEnabled();
	boolean isHalted();
	boolean isSlow();

	void addClockLine(ClockLine line);
	void setEnabled(boolean b);
	void setSlow(boolean b);
	void pulse();
}
