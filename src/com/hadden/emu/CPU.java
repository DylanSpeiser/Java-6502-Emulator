package com.hadden.emu;

public interface CPU
{
	public enum ClockRateUnit
	{
		HZ,
		KHZ,
		MHZ
	}
	
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
	};
	
	public String getName();
	public void interrupt();
	public void reset();
	public void clock();
	public Telemetry getTelemetry();
}
