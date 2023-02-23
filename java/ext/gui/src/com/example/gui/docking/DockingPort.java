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
import java.awt.Insets;

/**
 * This interface is designed to specify the API's required by
 * <code>DockingManager</code> for placing <code>Dockable</code> instances
 * within a container. A <code>DockingPort</code> is the parent container inside
 * of which <code>Dockable</code> instances may be placed.
 * 
 * @author Chris Butler
 */
public interface DockingPort
{
	public static final String CENTER_REGION = "CENTER";
	public static final String EAST_REGION = "EAST";
	public static final String EMPTY_REGION = "EMPTY";
	public static final String NORTH_REGION = "NORTH";
	public static final String SOUTH_REGION = "SOUTH";
	public static final String UNKNOWN_REGION = "UNKNOWN";
	public static final String WEST_REGION = "WEST";

	/**
	 * Returns a boolean indicating whether or not docking is allowed within the
	 * specified region. Used by <code>DockingManager</code> during drag
	 * operations.
	 */
	public boolean allowsDocking(String region);

	/**
	 * Docks the specified Dockable in the specified region. The
	 * <code>Dockable's</code> <code>getDockable()</code> component is used as
	 * the docking component.
	 */
	public boolean dock(Dockable dockable, String region);

	/**
	 * Docks the specified Component in the specified region. <code>desc</code>
	 * is used as a tab-title description in the event the specified component
	 * is docked into a tabbed pane. <code>resizable</code> is used to specify
	 * whether split-layout docking will be fixed or resizable. Returns
	 * <code>true</code> for success and <code>false</code> for failure.
	 */
	public boolean dock(Component comp, String desc, String region, boolean resizable);

	/**
	 * Callback method invoked from the <code>DockingManager</code> upon
	 * successful completion of a docking operation within the specified region.
	 */
	public void dockingComplete(String region);

	/**
	 * Returns a reference to the currently docked component.
	 */
	public Component getDockedComponent();

	/**
	 * Returns the docking insets for the current <code>DockingPort</code>.
	 * Docking insets determine the NORTH, SOUTH, EAST, and WEST boundaries for
	 * a given <code>DockingPort</code>. Docking insets are used by the
	 * <code>DockingManager</code> to determine the region over which the mouse
	 * cursor currently resides.
	 */
	public Insets getDockingInsets();

	/**
	 * Indicates whether or not the specified component is a child component
	 * docked within the <code>DockingPort</code>.
	 */
	public boolean hasDockedChild(Component comp);

	/**
	 * Indicates whether or not split-layout dockings are fixed or resizable.
	 */
	public boolean isLayoutResizable();

	/**
	 * Removes the currently docked component without losing a reference to the
	 * object, such that it may be re-docked via
	 * <code>retainDockedComponent()</code>. Swing doesn't provide a built-in
	 * facility for re-adding a component to its parent container with the same
	 * layout constraints after it has been removed. This method implies that
	 * such a facility will be built into implementing classes.
	 */
	public Component lendDockedComponent();

	/**
	 * Returns the docked component to its original child-state after having
	 * been removed via <code>lendDockedComponent()</code>. All original layout
	 * constraints should be honored when re-adding the component.
	 */
	public void retainDockedComponent();

	/**
	 * Determines whether docking will be allowed against this DockingPort
	 * instance. Useful in the event that, after docking a particular,
	 * 'non-friendly' component, subsequent dockings may be rejected.
	 */
	public void setDockingAllowed(boolean b);

	/**
	 * Sets the docking insets for the current <code>DockingPort</code>.
	 */
	public void setDockingInsets(Insets insets);

	/**
	 * Determines whether split-layout dockings are to be fixed or resizable.
	 */
	public void setLayoutResizable(boolean b);

	/**
	 * Removes the specified Component in from the <code>DockingPort</code>.
	 * Returns <code>true</code> for success and <code>false</code> for failure.
	 */
	public boolean undock(Component comp);

}
