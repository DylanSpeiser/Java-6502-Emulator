package com.juse.emulator.devices;

import com.juse.emulator.interfaces.BusIRQ;

public class BusIRQImpl implements BusIRQ
{
	@Override
	public void raise(int source)
	{
		System.out.println("BusIRQImpl:CPU IRQ");				
	}
}