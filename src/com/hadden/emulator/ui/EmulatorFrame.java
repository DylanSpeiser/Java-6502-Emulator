package com.hadden.emulator.ui;

import javax.swing.JFrame;

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
}