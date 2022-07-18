package com.hadden.emu.impl;

import com.hadden.emu.BusIRQ;

public class BusIRQImpl implements BusIRQ
{
	@Override
	public void raise(int source)
	{
		System.out.println("BusIRQImpl:CPU IRQ");				
	}
}