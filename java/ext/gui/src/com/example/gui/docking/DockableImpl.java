package com.example.gui.docking;

import java.awt.Component;

import javax.swing.JComponent;

public class DockableImpl extends DockableAdapter
{
	private ElegantPanel panel;
	private JComponent dragInitiator;

	public DockableImpl(ElegantPanel dockable, JComponent dragInit)
	{
		if (dockable == null)
			new IllegalArgumentException("Cannot create DockableImpl with a null DockablePanel.");
		if (dragInit == null)
			new IllegalArgumentException("Cannot create DockableImpl with a null drag initiator.");

		panel = dockable;
		dragInitiator = dragInit;
		DockingManager.registerDockable(this);
	}

	public Component getDockable()
	{
		return panel;
	}

	public String getDockableDesc()
	{
		String desc = panel.getTitle();
		if (desc == null || desc.length() == 0)
			return "null";
		return desc;
	}

	public Component getInitiator()
	{
		return dragInitiator;
	}

	public void setDockableDesc(String desc)
	{
		desc = desc == null ? "" : desc.trim();
		panel.setTitle(desc);
	}
}
