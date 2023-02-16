package com.juse.emulator.interfaces;

import java.util.List;

//
//
// Specific to the 6502 family, so use CPUInfo instead
//
//

public class Telemetry
{
	public byte 	a;
	public byte 	x;
	public byte 	y;
	public byte 	stackPointer;
	public short 	programCounter;
	public short 	addressAbsolute;
	public short 	addressRelative;
	public byte 	opcode;
	public String   opcodeName;
	public int 		cycles;
	public double 	clocksPerSecond;
	public int      clocks;
	public byte     flags;
	public long     irqs;
	public List<String> history;
};