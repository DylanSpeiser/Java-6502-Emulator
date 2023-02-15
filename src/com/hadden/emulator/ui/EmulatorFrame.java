package com.juse.emulator.ui;

import javax.swing.JFrame;

import com.juse.emulator.interfaces.Emulator;
import com.juse.emulator.ui.EmulatorDisplay;

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