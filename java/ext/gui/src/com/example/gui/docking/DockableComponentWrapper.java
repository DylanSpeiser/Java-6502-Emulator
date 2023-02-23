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
import java.awt.Point;
import java.awt.event.MouseMotionListener;
import java.util.EventListener;

/**
 * This class models a <code>Dockable</code> implementation for wrapping a
 * <code>Component</code>. It is essentially the simplest means to turning a
 * generic <code>Component</code> into a <code>Dockable</code> instance.
 * Compound <code>Dockables</code> may have separate child components that are
 * responsible for drag initiation, whereas another component is the actual drag
 * source. This is shown in the manner that a <code>JInternalFrame</code> would
 * be a draggable component, while the frame's title pane is the actual drag
 * initiator.
 * <p>
 * The class, conversely, deals with the <i>simple</i> case, where a
 * <code>Component</code> itself must be docking-enabled.
 * <code>DockableComponentWrapper</code> wraps a <code>Component</code> and
 * implements the <code>Dockable</code> interface. Since the
 * <code>Component</code> itself is being docking-enabled, it serves as both the
 * drag source and drag initiator. Thus, <code>getDockable()</code> and
 * <code>getInitiator()</code> return a reference to the same wrapped
 * <code>Component</code> object.
 * <p>
 * This class may be used by application code to enable docking capabilities on
 * a given <code>Component</code>. However, it is recommended that
 * <code>DockingManager.registerDockable(Component evtSrc, String desc, boolean allowResize)</code>
 * be used as a more automated, less invasive means of enabling docking on a
 * component.
 * <code>DockingManager.registerDockable(Component evtSrc, String desc, boolean allowResize)</code>
 * will automatically create a <code>DockableComponentWrapper</code> instance
 * and register the required MouseMotionListeners.
 *
 * @author Chris Butler
 */
public class DockableComponentWrapper implements Dockable
{
	private Component dragSrc;
	private String description;
	private boolean dockingEnabled;
	private boolean dockedLayoutResizable;

	/**
	 * Creates a <code>DockableComponentWrapper</code> instance using the
	 * specified source component, docking description, and resizing policy. If
	 * <code>src</code> is null, this method returns <code>null</code>. This is
	 * used to create <code>Dockable</code> instances for simple Components
	 * where the drag source and drag initiator are the same Component.
	 *
	 * @param src
	 *            the source component
	 * @param desc
	 *            the docking description
	 * @param resizable
	 *            the resizing policy
	 */
	public static DockableComponentWrapper create(Component src, String desc, boolean resizable)
	{
		if (src == null)
			return null;

		return new DockableComponentWrapper(src, desc, resizable);
	}

	/**
	 * @param src
	 * @param desc
	 * @param resizable
	 */
	private DockableComponentWrapper(Component src, String desc, boolean resizable)
	{
		dragSrc = src;
		setDockableDesc(desc);
		dockingEnabled = true;
		dockedLayoutResizable = resizable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.example.gui.docking.Dockable#getDockable()
	 */
	public Component getDockable()
	{
		return dragSrc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.example.gui.docking.Dockable#getDockableDesc()
	 */
	public String getDockableDesc()
	{
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.example.gui.docking.Dockable#dockingCanceled()
	 */
	public void dockingCanceled()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.example.gui.docking.Dockable#dockingCompleted()
	 */
	public void dockingCompleted()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.example.gui.docking.Dockable#getCursorProvider()
	 */
	public CursorProvider getCursorProvider()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.example.gui.docking.Dockable#isDockingEnabled()
	 */
	public boolean isDockingEnabled()
	{
		return dockingEnabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.example.gui.docking.Dockable#setDockingEnabled(boolean)
	 */
	public void setDockingEnabled(boolean b)
	{
		dockingEnabled = b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.example.gui.docking.Dockable#mouseMotionListenersBlockedWhileDragging
	 * ()
	 */
	public boolean mouseMotionListenersBlockedWhileDragging()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.example.gui.docking.Dockable#isDockedLayoutResizable()
	 */
	public boolean isDockedLayoutResizable()
	{
		return dockedLayoutResizable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.example.gui.docking.Dockable#setDockedLayoutResizable(boolean)
	 */
	public void setDockedLayoutResizable(boolean b)
	{
		dockedLayoutResizable = b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.example.gui.docking.Dockable#setDockableDesc(java.lang.String)
	 */
	public void setDockableDesc(String desc)
	{
		desc = desc == null ? "null" : desc.trim();
		if (desc.length() == 0)
			desc = "null";
		description = desc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.example.gui.docking.Dockable#getInitiator()
	 */
	public Component getInitiator()
	{
		return getDockable();
	}

}
