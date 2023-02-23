package com.example.gui.docking.runConfig;

import com.example.gui.docking.DefaultDockingPort;
import com.example.gui.docking.DockingManager;
import com.example.gui.docking.DockingPort;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SimpleDemo extends JFrame
{
	public static void main(String[] args)
	{
		JFrame f = new SimpleDemo();
		f.setSize(600, 400);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

	public SimpleDemo()
	{
		super("Simple Docking Demo");
		setContentPane(createContentPane());
	}

	private JPanel createContentPane()
	{
		JPanel p = new JPanel(new BorderLayout(5, 5));
		p.add(buildDockingPort(Color.blue, "Blue"), BorderLayout.NORTH);
		p.add(buildDockingPort(Color.red, "Red"), BorderLayout.SOUTH);
		p.add(buildDockingPort(Color.green, "Green"), BorderLayout.EAST);
		p.add(buildDockingPort(Color.yellow, "Yellow"), BorderLayout.WEST);
		p.add(createDockingPort(), BorderLayout.CENTER);
		return p;
	}

	private DefaultDockingPort buildDockingPort(Color color, String desc)
	{
		// create the DockingPort
		DefaultDockingPort port = createDockingPort();

		// create and register the Dockable panel
		JPanel p = new JPanel();
		p.setBackground(color);
		DockingManager.registerDockable(p, desc, true);

		// dock the panel and return the DockingPort
		port.dock(p, desc, DockingPort.CENTER_REGION, false);
		return port;
	}

	private DefaultDockingPort createDockingPort()
	{
		DefaultDockingPort port = new DefaultDockingPort();
		port.setBackground(Color.gray);
		port.setPreferredSize(new Dimension(100, 100));
		return port;
	}
}
