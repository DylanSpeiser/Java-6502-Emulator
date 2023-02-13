package com.juse.emulator;

public interface Emulator
{
	String getSystemVersion();
	String getMainTitle();
	Clock  getClock();
	CPU    getCPU();
	void   reset();
	Bus    getBus();
}
