package com.hadden.emulator;

public interface DeviceDebugger
{
	String[] getFeatures();
	boolean isEnabled(String feature);
	void setEnabled(String feature,boolean state);
}
