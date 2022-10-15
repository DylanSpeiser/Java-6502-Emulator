package com.hadden.emulator;

import com.hadden.emu.CPU;

public interface Emulator
{
	String getSystemVersion();
	String getTitle();
	Clock  getClock();
	CPU    getCPU();
	void   reset();
}
