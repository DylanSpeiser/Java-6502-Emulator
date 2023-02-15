package com.hadden.emulator.debug;

public interface DebugControl
{
	void enable();
	void disable();
	void addStepListener(DebugListener dsl);
	void addClockListener(DebugListener dsl);
	void addExecutionListener(DebugListener dsl);
}
