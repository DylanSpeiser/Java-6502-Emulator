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
import java.awt.Container;
import java.awt.LayoutManager;
import java.awt.Point;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.JWindow;

/**
 * This class provides an abstraction of root containers used in Swing. It
 * allows transparent use of methods common to <code>JFrame</code>,
 * <code>JApplet</code>, <code>JWindow</code>, and <code>JDialog</code> without
 * making an outward distinction between the different container types. This is
 * accomplished by wrapping the root component.
 * 
 * @author Chris Butler
 */
public class RootSwingContainer
{
	public static final Integer DEFAULT_MAXED_LAYER = Integer.valueOf(JLayeredPane.PALETTE_LAYER.intValue() - 10);
	private static final HashMap<Component,RootSwingContainer> MAP_BY_ROOT_CONTAINER = new HashMap<Component,RootSwingContainer>();
	private LayoutManager maxedLayout;
	private Integer maximizationLayer;
	private Component root;

	private static Component getRoot(Component c)
	{
		if (c == null)
			return null;

		if (isValidRootContainer(c))
			return c;

		Container parent = c.getParent();
		while (parent != null && !isValidRootContainer(parent))
			parent = parent.getParent();

		return parent;
	}

	/**
	 * Traverses the container hierarchy to locate the root container and
	 * returns corresponding <code>RootSwingContainer</code>. If <code>c</code>
	 * is <code>null</code>, a <code>null</code> reference is returned.
	 * 
	 * @param c
	 *            the container whose root we wish to find
	 * @return the enclosing <code>RootSwingcontainer</code>
	 */
	public static RootSwingContainer getRootContainer(Component c)
	{
		Component root = getRoot(c);
		if (!isValidRootContainer(root))
			return null;

		RootSwingContainer container = (RootSwingContainer) MAP_BY_ROOT_CONTAINER.get(root);
		if (container == null)
		{
			container = new RootSwingContainer(root);
			MAP_BY_ROOT_CONTAINER.put(root, container);
		}

		if (container.getRootContainer() != root)
			container.setRootContainer(root);

		return container;
	}

	/**
	 * Indicates whether the supplied <code>Component</code> is, in fact, a root
	 * Swing container.
	 * 
	 * @param c
	 *            the <code>Component</code> we wish to check
	 */
	protected static boolean isValidRootContainer(Component c)
	{
		return c instanceof JFrame || c instanceof JWindow || c instanceof JDialog;
	}

	/**
	 * Creates a new <code>RootSwingContainer</code> wrapping the specified
	 * component.
	 */
	protected RootSwingContainer(Component root)
	{
		setMaximizationLayer(DEFAULT_MAXED_LAYER);
		setRootContainer(root);
	}

	/**
	 * Returns the <code>contentPane</code> object for the wrapped component.
	 * 
	 * @return the <code>contentPane</code> property
	 */
	public Container getContentPane()
	{
		if (root instanceof JFrame)
			return ((JFrame) root).getContentPane();
		if (root instanceof JWindow)
			return ((JWindow) root).getContentPane();
		if (root instanceof JDialog)
			return ((JDialog) root).getContentPane();
		return null;
	}

	/**
	 * Returns the <code>glassPane</code> object for the wrapped component.
	 * 
	 * @return the <code>glassPane</code> property
	 */
	public Component getGlassPane()
	{
		if (root instanceof JFrame)
			return ((JFrame) root).getGlassPane();
		if (root instanceof JWindow)
			return ((JWindow) root).getGlassPane();
		if (root instanceof JDialog)
			return ((JDialog) root).getGlassPane();
		return null;
	}

	/**
	 * Returns the <code>layeredPane</code> object for the wrapped component.
	 * 
	 * @return the <code>layeredPane</code> property
	 */
	public JLayeredPane getLayeredPane()
	{
		if (root instanceof JFrame)
			return ((JFrame) root).getLayeredPane();
		if (root instanceof JWindow)
			return ((JWindow) root).getLayeredPane();
		if (root instanceof JDialog)
			return ((JDialog) root).getLayeredPane();
		return null;
	}

	/**
	 * Gets the location of the wrapped component in the form of a point
	 * specifying the component's top-left corner in the screen's coordinate
	 * space.
	 * 
	 * @return An instance of <code>Point</code> representing the top-left
	 *         corner of the component's bounds in the coordinate space of the
	 *         screen.
	 */
	public Point getLocationOnScreen()
	{
		if (root instanceof JFrame)
			return ((JFrame) root).getLocationOnScreen();
		else if (root instanceof JWindow)
			return ((JWindow) root).getLocationOnScreen();
		else if (root instanceof JDialog)
			return ((JDialog) root).getLocationOnScreen();
		return null;
	}

	/**
	 * Returns the layer associated with <code>Component</code> maximization
	 * within the <code>RootSwingContainer</code>.
	 * 
	 * @return an <code>Integer</code> indicating the maximization layer
	 *         property
	 */
	public Integer getMaximizationLayer()
	{
		return maximizationLayer;
	}

	/**
	 * Returns the <code>LayoutManager</code> associated with
	 * <code>Component</code> maximization within the
	 * <code>RootSwingContainer</code>.
	 * 
	 * @return a <code>LayoutManager</code> indicating the maximization layout
	 *         property
	 */
	public LayoutManager getMaximizedLayout()
	{
		return maxedLayout;
	}

	/**
	 * Returns the the wrapped component. (<code>JFrame</code>,
	 * <code>JApplet</code>, etc...)
	 * 
	 * @return the wrapped root container
	 */
	public Component getRootContainer()
	{
		return root;
	}

	/**
	 * Returns the <code>rootPane</code> object for the wrapped component.
	 * 
	 * @return the <code>rootPane</code> property
	 */
	public JRootPane getRootPane()
	{
		if (root instanceof JFrame)
			return ((JFrame) root).getRootPane();
		else if (root instanceof JWindow)
			return ((JWindow) root).getRootPane();
		else if (root instanceof JDialog)
			return ((JDialog) root).getRootPane();
		return null;
	}

	/**
	 * Convenience method that calls <code>revalidate()</code> on the current
	 * content pane if it is a <code>JComponent</code>. If not, no action is
	 * taken.
	 */
	public void revalidateContentPane()
	{
		Container c = getContentPane();
		if (c instanceof JComponent)
			((JComponent) c).revalidate();
	}

	/**
	 * Sets the <code>contentPane</code> property for the wrapped component.
	 *
	 * @param contentPane
	 *            the <code>contentPane</code> object for the wrapped component
	 */
	public void setContentPane(Container contentPane)
	{
		if (root instanceof JFrame)
			((JFrame) root).setContentPane(contentPane);
		else if (root instanceof JWindow)
			((JWindow) root).setContentPane(contentPane);
		else if (root instanceof JDialog)
			((JDialog) root).setContentPane(contentPane);
	}

	/**
	 * Sets the <code>glassPane</code> property for the wrapped component.
	 *
	 * @param glassPane
	 *            the <code>glassPane</code> object for the wrapped component
	 */
	public void setGlassPane(Component glassPane)
	{
		if (root instanceof JFrame)
			((JFrame) root).setGlassPane(glassPane);
		else if (root instanceof JWindow)
			((JWindow) root).setGlassPane(glassPane);
		else if (root instanceof JDialog)
			((JDialog) root).setGlassPane(glassPane);
	}

	/**
	 * Sets the <code>layeredPane</code> property for the wrapped component.
	 *
	 * @param layeredPane
	 *            the <code>layeredPane</code> object for the wrapped component
	 */
	public void setLayeredPane(JLayeredPane layeredPane)
	{
		if (root instanceof JFrame)
			((JFrame) root).setLayeredPane(layeredPane);
		else if (root instanceof JWindow)
			((JWindow) root).setLayeredPane(layeredPane);
		else if (root instanceof JDialog)
			((JDialog) root).setLayeredPane(layeredPane);
	}

	/**
	 * Sets the layer associated with <code>Component</code> maximization within
	 * the <code>RootSwingContainer</code>. If <code>layer</code> is
	 * <code>null</code>, DEFAULT_MAXED_LAYER is used instead.
	 * 
	 * @param layer
	 *            an <code>Integer</code> indicating the maximization layer
	 *            property
	 */
	public void setMaximizationLayer(Integer layer)
	{
		if (layer == null)
			layer = DEFAULT_MAXED_LAYER;
		maximizationLayer = layer;
	}

	/**
	 * Sets the <code>LayoutManager</code> associated with
	 * <code>Component</code> maximization within the
	 * <code>RootSwingContainer</code>.
	 * 
	 * @param mgr
	 *            the <code>LayoutManager</code> associated with
	 *            <code>Component</code> maximization within the
	 *            <code>RootSwingContainer</code>.
	 */
	public void setMaximizedLayout(LayoutManager mgr)
	{
		maxedLayout = mgr;
	}

	/**
	 * Sets the wrapped root container.
	 *
	 * @param root
	 *            the new wrapped root container
	 */
	protected void setRootContainer(Component root)
	{
		this.root = root;
	}
}
