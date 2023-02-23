package com.juse.emulator.interfaces.ui;


import com.juse.emulator.interfaces.Emulator;

/*
public abstract class EmulatorFrame extends JFrame
{
	abstract void initDisplay(EmulatorDisplay ed);
}
*/

public interface EmulatorFrame
{
	void initDisplay(EmulatorDisplay ed);
	void showFrame(boolean bVisible);
	void initFrame(Emulator emu);
}