package com.juse.emulator;

import com.hadden.emu.Bus;
import com.hadden.emu.CPU;

public interface Emulator
{
	String getSystemVersion();
	String getMainTitle();
	Clock  getClock();
	CPU    getCPU();
	void   reset();
	Bus    getBus();
}
