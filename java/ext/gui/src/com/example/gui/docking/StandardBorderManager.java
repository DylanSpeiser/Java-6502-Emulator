/* Copyright (c) 2004 Christopher M Butler

Permission is hereby granted, free of charge, to any person obtaining a copy of 
this software and associated documentation files (the "Software"), to deal in the 
Software without restriction, including without limitation the rights to use, 
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the 
Software, and to permit persons to whom the Software is furnished to do so, subject 
to the following conditions:

The above copyright notice and this permission notice shall be included in all 
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package com.example.gui.docking;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * This class provides a standard implementation of the
 * <code>BorderManager</code> interface. It is designed to avoid the negative
 * visual effects caused by nesting docked components that have individual
 * borders. It accomplishes this by establishing and maintaining a single border
 * for all docked components
 * 
 * This class encapsulates a <code>javax.swing.border.Border</code> instance, or
 * a <code>null</code> border reference, for application to a
 * <code>DefaultDockingPort</code> and its child components. If the
 * <code>DefaultDockingPort</code> has no child component, then
 * <code>managePortNullChild()</code> will apply the encapsulated border to the
 * <code>DefaultDockingPort</code> itself, rendering the visible outline of an
 * empty <code>DockingPort</code>. If the <code>DefaultDockingPort</code> has a
 * generic <code>Component</code> as its child, excluding a
 * <code>JTabbedPane</code> or <code>JSplitPane</code>, then the border for that
 * component is set to <code>null</code> and the encapsulated border is applied
 * to the <code>DockingPort</code> via <code>managePortSimpleChild()</code>. If
 * the <code>DefaultDockingPort</code> has a <code>JTabbedPane</code> as its
 * child, then a <code>null</code> border is set for the
 * <code>JTabbedPane</code> and all of its child components, and the
 * encapsulated border is applied to the <code>DockingPort</code>. This is
 * accomplished by calling <code>managePortTabbedChild</code>. Finally,
 * <code>managePortSplitChild()</code> will manage the border for a
 * <code>DefaultDockingPort</code> whose docked component is a
 * <code>JSplitPane</code>. This method removes all borders from the
 * <code>DefaultDockingPort</code> and the split pane divider and applies the
 * encapsulated border to both left and right child components of the
 * <code>JSplitPane</code>.
 * 
 * @author Chris Butler
 */
public class StandardBorderManager implements BorderManager
{
	private Border assignedBorder;

	/**
	 * Creates a new <code>StandardBorderManager</code> with a <code>null</code>
	 * assigned border.
	 */
	public StandardBorderManager()
	{
	}

	/**
	 * Creates a new <code>StandardBorderManager</code> with the specified
	 * assigned border.
	 * 
	 * @param border
	 *            the currently assigend border.
	 */
	public StandardBorderManager(Border border)
	{
		setBorder(border);
	}

	/**
	 * Returns the currently assigned border.
	 * 
	 * @return the currently assigned border.
	 */
	public Border getBorder()
	{
		return assignedBorder;
	}

	/**
	 * Sets the assigned border. Null values are acceptable.
	 * 
	 * @param border
	 *            the assigned border.
	 */
	public void setBorder(Border border)
	{
		assignedBorder = border;
	}

	/**
	 * Set the border on the supplied <code>DockingPort</code> to the currently
	 * assigned border.
	 * 
	 * @see BorderManager#managePortNullChild(com.example.gui.docking.DockingPort)
	 */
	public void managePortNullChild(DockingPort port)
	{
		setBorder(port, assignedBorder);
	}

	/**
	 * Removes any border from the <code>DockingPort's</code> docked component
	 * and set the border on the <code>DockingPort</code> itself to the
	 * currently assigned border.
	 * 
	 * @see BorderManager#managePortSimpleChild(com.example.gui.docking.DockingPort)
	 */
	public void managePortSimpleChild(DockingPort port)
	{
		if (port != null)
		{
			setBorder(port.getDockedComponent(), null);
			setBorder(port, assignedBorder);
		}
	}

	/**
	 * Removes any border from the <code>DockingPort</code> itself and places
	 * the currently assigned border on the two child components of the
	 * <code>DockingPort's</code JSplitPane</code> child.
	 * 
	 * @see BorderManager#managePortSplitChild(com.example.gui.docking.DockingPort)
	 */
	public void managePortSplitChild(DockingPort port)
	{
		if (port == null || !(port.getDockedComponent() instanceof JSplitPane))
			return;

		setBorder(port, null);

		// clear the border from the split pane
		JSplitPane split = (JSplitPane) port.getDockedComponent();
		if (split.getUI() instanceof BasicSplitPaneUI)
		{
			// grab the divider from the UI and remove the border from it
			BasicSplitPaneDivider divider = ((BasicSplitPaneUI) split.getUI()).getDivider();
			if (divider != null && divider.getBorder() != null)
				divider.setBorder(null);
		}
		setBorder(split, null);

		// set the borders on each of the child components
		setBorder(split.getLeftComponent(), assignedBorder);
		setBorder(split.getRightComponent(), assignedBorder);
	}

	/**
	 * Removes any border from the <code>DockingPort's</code> docked
	 * <code>JTabbedPane</code> component and sets the border on the
	 * <code>DockingPort</code> itself to the currently assigned border.
	 * 
	 * @see BorderManager#managePortTabbedChild(com.example.gui.docking.DockingPort)
	 */
	public void managePortTabbedChild(DockingPort port)
	{
		managePortSimpleChild(port);
		if (port == null || !(port.getDockedComponent() instanceof JTabbedPane))
			return;

		// we need to use a special UI to remove the outline around a
		// JTabbedPane.
		// this UI will only allow the outline on the side of the JTabbedPane
		// that
		// contains the tabs.
		JTabbedPane tabs = (JTabbedPane) port.getDockedComponent();
		if (!(tabs.getUI() instanceof SimpleTabbedPaneUI))
			tabs.setUI(new SimpleTabbedPaneUI());

		// remove any borders from the tabPane childrem
		int tc = tabs.getTabCount();
		Component cmp = null;
		for (int i = 0; i < tc; i++)
		{
			cmp = tabs.getComponentAt(i);
			if (cmp instanceof JComponent)
				((JComponent) cmp).setBorder(null);
		}

	}

	private void setBorder(Component cmp, Border border)
	{
		if (cmp instanceof JComponent)
			((JComponent) cmp).setBorder(border);
	}

	private void setBorder(DockingPort port, Border border)
	{
		if (port instanceof JComponent)
			((JComponent) port).setBorder(border);
	}

	private static class SimpleTabbedPaneUI extends BasicTabbedPaneUI
	{
		protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h)
		{
			if (tabPlacement == BOTTOM)
				super.paintContentBorderBottomEdge(g, tabPlacement, selectedIndex, x, y, w, h);
		}

		protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h)
		{
			if (tabPlacement == LEFT)
				super.paintContentBorderLeftEdge(g, tabPlacement, selectedIndex, x, y, w, h);
		}

		protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h)
		{
			if (tabPlacement == RIGHT)
				super.paintContentBorderRightEdge(g, tabPlacement, selectedIndex, x, y, w, h);
		}

		protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h)
		{
			if (tabPlacement == TOP)
				super.paintContentBorderTopEdge(g, tabPlacement, selectedIndex, x, y, w, h);
		}
	}

}
