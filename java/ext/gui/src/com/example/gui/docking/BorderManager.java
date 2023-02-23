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

import com.example.gui.docking.DockingPort;

/**
 * This interface provides a set of methods used by the
 * <code>DefaultDockingPort</code> class to manage border state after docking
 * and undocking operations.
 * 
 * @author Chris Butler
 */
public interface BorderManager
{
	/**
	 * Callback method allowing for customized behavior when the
	 * <code>DefaultDockingPort's</code> docked component state has changed and
	 * there is no longer a component docked within the port.
	 */
	public void managePortNullChild(DockingPort port);

	/**
	 * Callback method allowing for customized behavior when the
	 * <code>DefaultDockingPort's</code> docked component state has changed and
	 * there is a single generic component docked within the port. The
	 * <code>Component</code> may be retrieved by calling
	 * <code>port.getDockedComponent()</code>.
	 */
	public void managePortSimpleChild(DockingPort port);

	/**
	 * Callback method allowing for customized behavior when the
	 * <code>DefaultDockingPort's</code> docked component state has changed and
	 * the port has been split between two components. The
	 * <code>JSPlitPane</code> may be retrieved by calling
	 * <code>port.getDockedComponent()</code>.
	 */
	public void managePortSplitChild(DockingPort port);

	/**
	 * Callback method allowing for customized behavior when the
	 * <code>DefaultDockingPort's</code> docked component state has changed and
	 * docked components within the <code>CENTER</code> region are layed-out
	 * within a <code>JTabbedPane</code>. The <code>JTabbedPane</code> may be
	 * retrieved by calling <code>port.getDockedComponent()</code>.
	 */
	public void managePortTabbedChild(DockingPort port);
}
