package com.juse.emulator.ui;


import com.juse.emulator.interfaces.Emulator;

/*
public abstract class EmulatorFrame extends JFrame
{
	abstract void initDisplay(EmulatorDisplay ed);
}
*/

public interface EmulatorFrame
{
	void initFrame(EmulatorDisplay ed);
	void showFrame(boolean bVisible);
	void initFrame(Emulator emu);
}