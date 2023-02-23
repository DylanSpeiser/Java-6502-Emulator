package com.example.gui.docking;

public class ElegantDockingPort extends DefaultDockingPort
{
	public ElegantDockingPort()
	{
		setComponentProvider(new ChildComponentDelegate());
		setBorderManager(new StandardBorderManager(new ShadowBorder()));
	}

	public void add(ElegantPanel view)
	{
		dock(view.getDockable(), CENTER_REGION);
	}
}
