package com.juse.emulator.interfaces;

import java.util.List;
import java.util.Map;

import com.juse.emulator.interfaces.Bus;
import com.juse.emulator.interfaces.Telemetry;

public interface CPU
{
	public String getName();
	public void interrupt();
	public void reset();
	public void clock();
	public Telemetry getTelemetry();
	public void setBus(Bus bus);
	//public IOSize getAddressSize();
}
