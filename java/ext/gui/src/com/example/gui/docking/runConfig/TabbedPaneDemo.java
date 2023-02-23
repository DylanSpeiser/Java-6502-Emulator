package com.example.gui.docking.runConfig;

import com.example.gui.docking.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;

public class TabbedPaneDemo extends JPanel
{
	private JLabel titlebar;
	private Dockable dockableImpl;

	public TabbedPaneDemo(String title)
	{
		super();
		titlebar = createTitlebar(" " + title);
		add(titlebar);
		setBorder(new LineBorder(Color.black));
		dockableImpl = new DockableImpl();
	}

	private JLabel createTitlebar(String title)
	{
		JLabel lbl = new JLabel(title);
		lbl.setForeground(Color.white);
		lbl.setBackground(Color.blue);
		lbl.setOpaque(true);
		return lbl;
	}

	public void doLayout()
	{
		Insets in = getInsets();
		titlebar.setBounds(in.left, in.top, getWidth() - in.left - in.right, 25);
	}

	private TabbedPaneDemo getThis()
	{
		return this;
	}

	private Dockable getDockable()
	{
		return dockableImpl;
	}

	private class DockableImpl extends DockableAdapter
	{
		public Component getDockable()
		{
			return getThis();
		}

		public String getDockableDesc()
		{
			return titlebar.getText().trim();
		}

		public Component getInitiator()
		{
			// the titlebar will the the 'hot' component that initiates dragging
			return titlebar;
		}
	}

	private static JPanel createContentPane()
	{
		JPanel p = new JPanel(new BorderLayout(5, 5));
		p.add(buildDockingPort("North"), BorderLayout.NORTH);
		p.add(buildDockingPort("South"), BorderLayout.SOUTH);
		p.add(buildDockingPort("East"), BorderLayout.EAST);
		p.add(buildDockingPort("West"), BorderLayout.WEST);
		p.add(createDockingPort("Center"), BorderLayout.CENTER);
		return p;
	}

	private static DefaultDockingPort buildDockingPort(String desc)
	{
		// create the DockingPort
		DefaultDockingPort port = createDockingPort(desc);

		// create the Dockable panel
		TabbedPaneDemo cd = new TabbedPaneDemo(desc);
		DockingManager.registerDockable(cd.getDockable());

		// dock the panel and return the DockingPort
		port.dock(cd.getDockable(), DockingPort.CENTER_REGION);
		return port;
	}

	private static int getTabPosition(String desc)
	{
		if ("North".equals(desc))
			return JTabbedPane.TOP;
		if ("South".equals(desc))
			return JTabbedPane.BOTTOM;
		if ("East".equals(desc))
			return JTabbedPane.RIGHT;
		if ("West".equals(desc))
			return JTabbedPane.LEFT;
		return JTabbedPane.TOP;
	}

	private static DefaultDockingPort createDockingPort(String desc)
	{
		DefaultDockingPort port = new DefaultDockingPort();
		port.setBackground(Color.gray);
		port.setPreferredSize(new Dimension(200, 100));
		port.setComponentProvider(new ComponentProvider(getTabPosition(desc)));
		return port;
	}

	public static void main(String[] args)
	{
		JFrame f = new JFrame("Custom Conatainers Docking Demo");
		f.setContentPane(createContentPane());
		f.setSize(600, 400);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

	private static class ComponentProvider extends ComponentProviderAdapter
	{
		int tabPosition;

		private ComponentProvider(int tabs)
		{
			tabPosition = tabs;
		}

		public JTabbedPane createTabbedPane()
		{
			return new JTabbedPane(tabPosition);
		}
	}

}
