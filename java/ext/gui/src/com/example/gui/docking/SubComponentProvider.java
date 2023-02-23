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

import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import com.example.gui.docking.DockingPort;

/**
 * This interface provides a pluggable means of customizing docking behavior
 * within a <code>DefaultDockingPort</code>. Any class implementing this
 * interface may be plugged into a <code>DefaultDockingPort</code> and will
 * assume responsibility for managing the creation of child components for the
 * <code>DefaultDockingPort</code> during docking operations.
 *
 * @author Chris Butler
 */
public interface SubComponentProvider
{

	/**
	 * Supplies the <code>DefaultDockingPort</code> with
	 * child-<code>DockingPorts</code> when splitting the
	 * <code>DefaultDockingPort</code> between two components.
	 * 
	 * @return a <code>DockingPort</code> instance.
	 */
	public DockingPort createChildPort();

	/**
	 * Supplies the <code>DefaultDockingPort</code> with the
	 * <code>JTabbedPane</code> to be used when adding multiple components to
	 * the <code>CENTER</code> region.
	 * 
	 * @return a <code>JTabbedPane</code> instance.
	 */
	public JTabbedPane createTabbedPane();

	/**
	 * Supplies the <code>DefaultDockingPort</code> with the
	 * <code>JSplitPane</code> to be used when splitting the
	 * <code>DefaultDockingPort</code> between two components.
	 * 
	 * @return a <code>JSplitPane</code> instance.
	 */
	public JSplitPane createSplitPane();

	/**
	 * Supplies the <code>DefaultDockingPort</code> with the initial
	 * proportional <code>JSplitPane</code> divider location to be used when
	 * splitting the <code>DefaultDockingPort</code> between two components. The
	 * <code>JSplitPane</code> will initially appear on the screen with this
	 * divider location. Should be between 0.0 and 1.0.
	 * 
	 * @return the desired initial divider location.
	 */
	public double getInitialDividerLocation();

}
