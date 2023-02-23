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
import javax.swing.JSplitPane;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class SplitPaneDemo extends JPanel
{
	private JLabel titlebar;
	private Dockable dockableImpl;

	public SplitPaneDemo(String title)
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

	private Dockable getDockable()
	{
		return dockableImpl;
	}

	private SplitPaneDemo getThis()
	{
		return this;
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

	private static class ComponentProvider extends ComponentProviderAdapter
	{
		public DockingPort createChildPort()
		{
			DefaultDockingPort port = new DefaultDockingPort();
			port.setComponentProvider(this);
			return port;
		}

		public JSplitPane createSplitPane()
		{
			JSplitPane split = new JSplitPane();
			// remove the border from the split pane
			split.setBorder(null);

			// set the divider size for a more reasonable, less bulky look
			split.setDividerSize(3);

			// check the UI. If we can't work with the UI any further, then
			// exit here.
			if (!(split.getUI() instanceof BasicSplitPaneUI))
				return split;

			// grab the divider from the UI and remove the border from it
			BasicSplitPaneDivider divider = ((BasicSplitPaneUI) split.getUI()).getDivider();
			if (divider != null)
				divider.setBorder(null);

			return split;
		}

	}

	private static JPanel createContentPane()
	{
		JPanel p = new JPanel(new BorderLayout(5, 5));
		p.add(buildDockingPort("North"), BorderLayout.NORTH);
		p.add(buildDockingPort("South"), BorderLayout.SOUTH);
		p.add(buildDockingPort("East"), BorderLayout.EAST);
		p.add(buildDockingPort("West"), BorderLayout.WEST);
		p.add(createDockingPort(), BorderLayout.CENTER);
		return p;
	}

	private static DefaultDockingPort buildDockingPort(String desc)
	{
		// create the DockingPort
		DefaultDockingPort port = createDockingPort();

		// create the Dockable panel
		SplitPaneDemo spd = new SplitPaneDemo(desc);
		DockingManager.registerDockable(spd.getDockable());

		// dock the panel and return the DockingPort
		port.dock(spd.getDockable(), DockingPort.CENTER_REGION);
		return port;
	}

	private static DefaultDockingPort createDockingPort()
	{
		DefaultDockingPort port = new DefaultDockingPort();
		port.setBackground(Color.gray);
		port.setPreferredSize(new Dimension(100, 100));
		port.setComponentProvider(new ComponentProvider());
		return port;
	}

	public static void main(String[] args)
	{
		JFrame f = new JFrame("Split Docking Demo");
		f.setContentPane(createContentPane());
		f.setSize(600, 400);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
