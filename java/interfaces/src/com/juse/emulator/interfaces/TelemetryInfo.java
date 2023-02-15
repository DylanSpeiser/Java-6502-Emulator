package com.juse.emulator.interfaces;

import java.util.Map;

public class TelemetryInfo extends Telemetry
{
	public Map<String,Integer> registerInfo;
	public Map<String,Integer> flagInfo;
	public Map<String,Integer> pointerInfo;
	public Map<String,Integer> addressInfo;
};