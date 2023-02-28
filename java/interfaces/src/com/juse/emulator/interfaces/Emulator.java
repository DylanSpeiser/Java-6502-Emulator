package com.juse.emulator.interfaces;

public interface Emulator
{
	String getSystemVersion();
	String getMainTitle();
	Clock  getClock();
	CPU    getCPU();
	void   reset();
	Bus    getBus();
	String getExtensionsPath();
	void   setExtensionsPath(String extPath);
}
