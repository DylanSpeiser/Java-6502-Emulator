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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.EventListener;
import java.util.HashMap;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 * This class is used to manage drag operations for <code>Dockable</code>
 * components. Application code should interact with this class through
 * <code>static</code> utility methods.
 * <p>
 * Any component that wishes to have docking capabilities enabled should call
 * <code>registerDockable(Component evtSrc, String desc, boolean allowResize)</code>.
 * <code>registerDockable(Component evtSrc, String desc, boolean allowResize)</code>
 * will create a <code>Dockable</code> instance that this class can work with
 * during drag operations. Likewise, when dealing strictly with bare
 * <code>Components</code>, most methods have been overloaded with a
 * <code>Component</code> version and a <code>Dockable</code> version.
 * <code>Component</code> versions always create (or pull from a cache) a
 * corresponding <code>Dockable</code> instance and dispatch to the overloaded
 * <code>Dockable</code> version of the method.
 * <p>
 * <code>registerDockable(Component evtSrc, String desc, boolean allowResize)</code>
 * adds required <code>MouseMotionListeners</code> to the source
 * <code>Component</code>, which automatically handle method dispatching to
 * <code>startDrag()</code>, so explicitly initiating a drag in this manner,
 * while not prohibited, is typically not required.
 * <p>
 * During drag operations, an outline of the <code>Dockable.getDockable()</code>
 * is displayed on the GlassPane and moves with the mouse cursor. The
 * <code>DockingManager</code> monitors the docking region underneath the mouse
 * cursor for underlying <code>DockingPorts</code> and the mouse cursor icon
 * will reflect this appropriately. The image displayed by for the mouse cursor
 * may be altered by returning a custom <code>CursorProvider</code> for the
 * currentl <code>Dockable</code>. When the mouse has been released, a call to
 * <code>stopDrag()</code> is issued. If the current docking region allows
 * docking, then the <code>DockingManager</code> removes the drag source from
 * its original parent and docks it into its new <code>DockingPort</code>,
 * subsequently issuing callbacks to
 * <code>DockingPort.dockingComplete(String region)</code> and then
 * <code>Dockable.dockingCompleted()</code>. If docking is not allowed, then no
 * docking operation is performed and a callback is issued to
 * <code>Dockable.dockingCanceled()</code>.
 * <p>
 * Whenever a <code>Dockable</code> is removed from a <code>DockingPort</code>,
 * the <code>DockingManager</code> takes care of making the requisite call to
 * <code>DockingPort.undock()</code>.
 * 
 * @author Chris Butler
 */
public class DockingManager extends JPanel
{
	private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);
	private static final HashMap GLASSPANES_BY_ROOT_CONTAINER = new HashMap();
	private static final HashMap DEFAULT_CURSOR_IMAGES = createCursorImages();
	private static final Image EMPTY_IMAGE = ResourceManager.createImage("resources/images/emptyIcon.gif");
	private static final WeakHashMap CACHED_DRAG_INITIATORS_BY_COMPONENT = new WeakHashMap();

	private Dockable dockableImpl;
	private Component currentMouseoverComponent;
	private Point mouseLocation;
	private Point mouseOffsetFromDragSource;
	private boolean mouseOutOfBounds;
	private RootSwingContainer rootContainer;
	private DragEventManager dragEventManger;
	private Image currentMouseImage;
	private Dimension dragSourceSize;
	private Component cachedGlassPane;
	private String currentDockingRegion;
	private EventListener[] cachedListeners;
	private Point lastMouse;

	private DockingManager(RootSwingContainer root)
	{
		super(null);
		rootContainer = root;
		setOpaque(false);
		setBorder(null);
		mouseOffsetFromDragSource = new Point();
		setCursor(ResourceManager.createCursor("resources/images/emptyIcon.gif", new Point(8, 8), "Empty"));
		dragEventManger = new DragEventManager();
		addMouseListener(dragEventManger);
	}

	/**
	 * Overridden to ensure empty insets.
	 * 
	 * @return <code>new Insets(0, 0, 0, 0)</code>
	 */
	public Insets getInsets()
	{
		return new Insets(0, 0, 0, 0);
	}

	/**
	 * Overridden to ensure empty insets. Calls <code>getInsets()</code>.
	 * 
	 * @return <code>new Insets(0, 0, 0, 0)</code>
	 */
	public Insets getInsets(Insets insets)
	{
		return getInsets();
	}

	/**
	 * @see JComponent#paintComponent(Graphics)
	 */
	protected void paintComponent(Graphics g)
	{
		paintComponentImpl(g);
	}

	/**
	 * Overridden to ensure a null border. Will call
	 * <code>super.setBorder(null)</code>.
	 */
	public void setBorder(Border border)
	{
		super.setBorder(null);
	}

	/**
	 * Overridden to ensure transparency. Will call
	 * <code>super.setOpaque(false)</code>.
	 */
	public void setOpaque(boolean isOpaque)
	{
		super.setOpaque(false);
	}

	/**
	 * Overridden to do nothing.
	 * 
	 * @return returns a null reference
	 */
	public Component add(Component comp, int index)
	{
		return null;
	}

	/**
	 * Overridden to do nothing.
	 */
	public void add(Component comp, Object constraints, int index)
	{
	}

	/**
	 * Overridden to do nothing.
	 */
	public void add(Component comp, Object constraints)
	{
	}

	/**
	 * Overridden to do nothing.
	 * 
	 * @return returns a null reference
	 */
	public Component add(Component comp)
	{
		return null;
	}

	/**
	 * Overridden to do nothing.
	 * 
	 * @return returns a null reference
	 */
	public Component add(String name, Component comp)
	{
		return null;
	}

	/**
	 * Overridden to ensure a null layout. Will call
	 * <code>super.setLayout(null)</code>.
	 */
	public void setLayout(LayoutManager mgr)
	{
		super.setLayout(null);
	}

	private static HashMap createCursorImages()
	{
		HashMap map = new HashMap();
		map.put(DockingPort.NORTH_REGION, ResourceManager.createImage("resources/images/upArrow.gif"));
		map.put(DockingPort.SOUTH_REGION, ResourceManager.createImage("resources/images/downArrow.gif"));
		map.put(DockingPort.EAST_REGION, ResourceManager.createImage("resources/images/rightArrow.gif"));
		map.put(DockingPort.WEST_REGION, ResourceManager.createImage("resources/images/leftArrow.gif"));
		map.put(DockingPort.CENTER_REGION, ResourceManager.createImage("resources/images/stacked.gif"));
		map.put(DockingPort.UNKNOWN_REGION, ResourceManager.createImage("resources/images/notAllowed.gif"));
		map.put(DockingPort.EMPTY_REGION, EMPTY_IMAGE);
		return map;
	}

	private static DockingManager getDockingManager(Component c)
	{
		if (c == null)
			return null;

		RootSwingContainer root = RootSwingContainer.getRootContainer(c);
		if (root == null)
			return null;

		DockingManager mgr = (DockingManager) GLASSPANES_BY_ROOT_CONTAINER.get(root.getRootContainer());
		if (mgr == null)
		{
			mgr = new DockingManager(root);
			GLASSPANES_BY_ROOT_CONTAINER.put(root.getRootContainer(), mgr);
		}
		return mgr;
	}

	/**
	 * Dispatches to
	 * <code>startDrag(Component c, Point mousePosition, String tabTitle)</code>,
	 * passing in <code>null</code> for the <code>tabTitle</code>.
	 *
	 * @param c
	 *            the drag source
	 * @param mousePosition
	 *            the location of the mouse over the drag source
	 */
	public static void startDrag(Component c, Point mousePosition)
	{
		startDrag(c, mousePosition, null);
	}

	/**
	 * Dispatches to
	 * <code>startDrag(Component c, Point mousePosition, String tabTitle, boolean allowResize)</code>,
	 * passing in <code>false</code> for <code>allowResize</code>.
	 *
	 * @param c
	 *            the drag source
	 * @param mousePosition
	 *            the location of the mouse over the drag source
	 * @param tabTitle
	 *            the docking description to be used as a tab title if docked in
	 *            a tabbed pane
	 */
	public static void startDrag(Component c, Point mousePosition, String tabTitle)
	{
		startDrag(c, mousePosition, tabTitle, false);
	}

	/**
	 * Creates a <code>Dockable</code> instance and dispatches to
	 * <code>startDrag(Dockable initiator, Point mousePoint)</code>. If
	 * <code>mousePosition</code> is null, or a <code>Dockable</code> instance
	 * cannot be created, no exception is thrown and no action is taken.
	 * <code>tabTitle</code> and <code>allowResize</code> will supercede any
	 * cached values set by <code>setDockingDescription()</code> and
	 * <code>setDockingResizablePolicy()</code>.
	 *
	 * @param c
	 *            the drag source
	 * @param mousePosition
	 *            the location of the mouse over the drag source
	 * @param tabTitle
	 *            the docking description to be used as a tab title if docked in
	 *            a tabbed pane
	 * @param allowResize
	 *            the resize policy that determine whether a split-layout
	 *            docking will be fixed or not
	 */
	public static void startDrag(Component c, Point mousePosition, String tabTitle, boolean allowResize)
	{
		if (mousePosition == null)
			return;

		Dockable initiator = DockableComponentWrapper.create(c, tabTitle, allowResize);
		if (initiator != null)
			startDrag(initiator, mousePosition);
	}

	/**
	 * Begins processing of drag operations against the specified
	 * <code>Dockable</code> instance. Normally, this method is called
	 * automatically by MouseMotionListeners attached to Components via
	 * <code>registerDockable()</code> and not explicitly by application code.
	 * However, application code has the option to call this method if so
	 * desired. This method checks for null cases against <code>dockable</code>,
	 * <code>dockable.getInitiator()</code>, and
	 * <code>dockable.getDockable()</code>. If any of these checks fail, no
	 * exception is thrown and no action is taken. <code>mousePoint</code> is
	 * relative to <code>dockable.getInitiator()</code>. In the case of
	 * dispatching from one of the other overloaded <code>startDrag()</code>
	 * methods, <code>dockable.getInitiator()</code> and
	 * <code>dockable.getDockable()</code> refer to the same object, and so
	 * <code>mousePoint</code> is relative to both. The current GlassPane will
	 * be swapped out, a component outline will be drawn relative to the current
	 * mouse position, and the mosue cursor will change according to the
	 * available docking region beneath the mouse. This state will persist until
	 * <code>stopDrag()</code> is called, in which case the original GlassPane
	 * will be reinstated.
	 *
	 * @param dockable
	 *            the dockable instance we intend to drag
	 * @param mousePoint
	 *            the location of the mouse relative to
	 *            <code>dockable.getInitiator()</code>
	 */
	public static void startDrag(Dockable dockable, Point mousePoint)
	{
		if (dockable == null)
			return;

		Component c = dockable.getInitiator();
		if (c == null || dockable.getDockable() == null)
			return;

		DockingManager mgr = getDockingManager(c);
		if (mgr != null)
			mgr.startDragImpl(dockable, mousePoint);
	}

	/**
	 * Creates a <code>Dockable</code> instance and dispatches to
	 * <code>stopDrag(Dockable dockable)</code>.
	 *
	 * @param c
	 *            the currently dragged component
	 */
	public static void stopDrag(Component c)
	{
		Dockable init = DockableComponentWrapper.create(c, null, false);
		if (init != null)
			stopDrag(init);
	}

	/**
	 * Ends the drag operation against the specified <code>Dockable</code>
	 * instance. If no drag operation is in progress or the specified
	 * <code>Dockable</code> instance isn't the one in a drag-state, then no
	 * action is taken. When called, the <code>DockingManager</code> will
	 * attempt to drop the currently dragged <code>Dockable</code> instance into
	 * the region underneath the mouse cursor. This method is normally called
	 * internally by the <code>DockingManager</code> itself when the mouse
	 * button has been released. However, it has been made public to allow for
	 * the option os explicit invocation from application code.
	 *
	 * @param dockable
	 *            the currently dragged <code>Dockable</code>
	 */
	public static void stopDrag(Dockable dockable)
	{
		if (dockable == null)
			return;

		DockingManager mgr = getDockingManager(dockable.getDockable());
		if (mgr != null)
			mgr.stopDragImpl(dockable);
	}

	private static DockingPortCursorPane createDockingPortCursorProxy(DockingPort port)
	{
		DockingPortCursorPane mainPane = new DockingPortCursorPane(port, DockingPort.CENTER_REGION);
		Insets insets = port.getDockingInsets();
		if (insets == null)
			insets = EMPTY_INSETS;

		mainPane.createNorthRegion(insets.top);
		mainPane.createSouthRegion(insets.bottom);
		mainPane.createEastRegion(insets.right);
		mainPane.createWestRegion(insets.left);

		return mainPane;
	}

	private Image getCursorImageForRegion(String region)
	{
		if (region == null)
			region = DockingPort.UNKNOWN_REGION;

		CursorProvider provider = dockableImpl.getCursorProvider();
		Image image = provider == null ? null : getCursorFromProvider(provider, region);

		if (image == null)
			image = (Image) DEFAULT_CURSOR_IMAGES.get(region);
		if (image == null)
			return getCursorImageForRegion(DockingPort.UNKNOWN_REGION);
		return image;
	}

	private Image getCursorFromProvider(CursorProvider provider, String region)
	{
		if (DockingPort.NORTH_REGION.equals(region))
			return provider.getNorthImage();
		if (DockingPort.SOUTH_REGION.equals(region))
			return provider.getSouthImage();
		if (DockingPort.EAST_REGION.equals(region))
			return provider.getEastImage();
		if (DockingPort.WEST_REGION.equals(region))
			return provider.getWestImage();
		if (DockingPort.CENTER_REGION.equals(region))
			return provider.getCenterImage();
		return provider.getDisallowedImage();
	}

	/**
	 * Creates a Dockable for the specified component and dispatches to
	 * <code>registerDockable(Dockable init)</code>. If evtSrc is null, no
	 * exception is thrown and no action is performed.
	 *
	 * @param evtSrc
	 *            the target component for the Dockable, both drag-starter and
	 *            docking source
	 * @param desc
	 *            the description of the docking source. Used as the tab-title
	 *            of docked in a tabbed pane
	 * @param allowResize
	 *            specifies whether or not a resultant split-view docking would
	 *            be fixed or resizable
	 */
	public static void registerDockable(Component evtSrc, String desc, boolean allowResize)
	{
		if (evtSrc == null)
			return;

		Dockable init = DockableComponentWrapper.create(evtSrc, desc, allowResize);
		registerDockable(init);
	}

	/**
	 * Initializes the specified Dockable. Adds a MouseMotionListener to
	 * <code>init.getInitiator()</code> to detect drag events and call
	 * <code>startDrag()</code> when detected. Caches <code>init</code> in a
	 * <code>WeakHashMap</code> by <code>init.getDockable()</code> for
	 * subsequent internal lookups. If the MouseMotionListener is already
	 * registerd with the initiator component, it will not be added again.
	 * <code>init</code> may not be null and both
	 * <code>init.getDockable()</code> and , <code>init.getInitiator()</code>
	 * may not return null. If any of these checks fail, no exception is thrown
	 * and no action is performed.
	 *
	 * @param init
	 *            the Dockable that is being initialized.
	 */
	public static void registerDockable(Dockable init)
	{
		if (init == null || init.getDockable() == null || init.getInitiator() == null)
			return;

		DragInitiationListener listener = getInitiationListener(init);
		if (listener == null)
		{
			listener = new DragInitiationListener();
			listener.setInitiator(init);
			init.getInitiator().addMouseMotionListener(listener);
		}
		CACHED_DRAG_INITIATORS_BY_COMPONENT.put(init.getDockable(), init);
	}

	private static Dockable getDragInitiator(Component c)
	{
		if (c == null)
			return null;

		Dockable initiator = (Dockable) CACHED_DRAG_INITIATORS_BY_COMPONENT.get(c);
		if (initiator == null)
		{
			initiator = DockableComponentWrapper.create(c, null, false);
			CACHED_DRAG_INITIATORS_BY_COMPONENT.put(c, initiator);
		}
		return initiator;
	}

	private static DragInitiationListener getInitiationListener(Dockable init)
	{
		EventListener[] listeners = init.getInitiator().getListeners(MouseMotionListener.class);
		if (listeners == null || listeners.length == 0)
			return null;

		for (int i = 0; i < listeners.length; i++)
		{
			if (listeners[i] instanceof DragInitiationListener)
				return (DragInitiationListener) listeners[i];
		}
		return null;
	}

	/**
	 * Sets the resizing policy for the specified component. Te resizing policy
	 * is used determine whether, after docking in a split layout, the layout
	 * itself will be fixed or resizable. A Dockable is looked up from an
	 * internal cache using the specified component as a key. If not found, a
	 * dockabe is created and cached by the same key. This method dispatches to
	 * <code>setDockingResizablePolicy(Dockable dockable, boolean resizable)</code>.
	 *
	 * @param c
	 *            the Component that is the drag-source for a given Dockable
	 *            instance
	 * @param resizable
	 *            the requested resizing policy for splip-layout docking
	 */
	public static void setDockingResizablePolicy(Component c, boolean resizable)
	{
		Dockable init = getDragInitiator(c);
		setDockingResizablePolicy(init, resizable);
	}

	/**
	 * Sets the resizing policy for the specified Dockable. Te resizing policy
	 * is used determine whether, after docking in a split layout, the layout
	 * itself will be fixed or resizable. If the Dockable instance is not null,
	 * then <code>setDockedLayoutResizable(resizable)</code> is called against
	 * it.
	 *
	 * @param dockable
	 *            the Dockable instance whose resizing policy is being set
	 * @param resizable
	 *            the requested resizing policy for splip-layout docking
	 */
	public static void setDockingResizablePolicy(Dockable dockable, boolean resizable)
	{
		if (dockable != null)
			dockable.setDockedLayoutResizable(resizable);
	}

	/**
	 * Sets the docking description for the specified component. Te docking
	 * description is used as the tab-title when docking within a tabbed pane. A
	 * Dockable is looked up from an internal cache using the specified
	 * component as a key. If not found, a dockabe is created an cached by the
	 * same key. The Dockable's description is set to <code>desc</code>.
	 *
	 * @param c
	 *            the Component that is the drag-source for a given Dockable
	 *            instance
	 * @param desc
	 *            the dockable description for the specified component
	 */
	public static void setDockingDescription(Component c, String desc)
	{
		Dockable init = getDragInitiator(c);
		setDockingDescription(init, desc);
	}

	/**
	 * Sets the docking description for the Dockable instance. THe docking
	 * description is used as the tab-title when docking within a tabbed pane.
	 *
	 * @param dockable
	 *            the Dockable instance we're describing
	 * @param desc
	 *            the description of the Dockable instance. used as a tab-title.
	 */
	public static void setDockingDescription(Dockable dockable, String desc)
	{
		if (dockable != null)
			dockable.setDockableDesc(desc);
	}

	private void startDragImpl(Dockable initiator, Point mousePoint)
	{
		// remove any cruft left over from the last drag operation. This is
		// probably sloppy as we
		// should keep better track of what components we do and don't have. But
		// this
		// can be dealt with later.
		removeAll();

		// set the current drag initiator
		dockableImpl = initiator;

		// insert ourselves as the glassPane on the root container during the
		// drag
		// operation.
		cachedGlassPane = rootContainer.getGlassPane();
		rootContainer.setGlassPane(this);

		// initialize coordinate info for the drag operation
		Component dragSrc = dockableImpl.getDockable();
		dragSourceSize = dragSrc.getSize();
		initMouseCursorOffset(mousePoint);
		determineMouseLocationBasedOnDragSource(mousePoint);
		currentDockingRegion = null;
		lastMouse = null;

		// initialize listeners on the drag-source
		initializeListenerCaching();
		dockableImpl.getInitiator().addMouseMotionListener(dragEventManger);
		dockableImpl.getInitiator().addMouseListener(dragEventManger);

		// show the new glassPane.
		setVisible(true);

		// start dragging
		manageCursor();

	}

	private void stopDragImpl(Dockable init)
	{
		// this method isn't reentrant, but it may be invoked twice (once by the
		// drag-source
		// and once by the glassPane). we only want it to execute once. there is
		// no race-condition
		// here as we don't care which event gets here first. it'll merely lock
		// out other subsequent calls.
		// (note: this method isn't synchronized. swing event-processing is
		// single-threaded, so we shouldn't
		// have concurrency issues to deal with).
		if (dockableImpl == null || init == null || dockableImpl.getDockable() != init.getDockable())
			return;

		// perform the drop operation.
		boolean docked = dropComponent();

		// cache a temporary reference to the dockableImpl for later (after we
		// null it out)
		Dockable tmpReference = dockableImpl;

		// remove the listeners from the drag-source and all the old ones back
		// in
		Component dragSrc = dockableImpl.getDockable();
		dockableImpl.getInitiator().removeMouseMotionListener(dragEventManger);
		dockableImpl.getInitiator().removeMouseListener(dragEventManger);
		restoreCachedListeners();
		DragInitiationListener initiator = getInitiationListener(dockableImpl);
		if (initiator != null)
			initiator.setEnabled(true);

		// among other things, this will lock out other calls to this method
		// after the drag has
		// been stopped
		dockableImpl = null;

		// hide the glass pane before swapping out. If we swap out the glasspane
		// before
		// hiding, we'll run into problems w/the mouse cursor later.
		setVisible(false);

		// return the root container to its original state
		rootContainer.setGlassPane(cachedGlassPane);
		cachedGlassPane = null;

		// perform post-drag operations
		if (docked)
		{
			DockingPort port = ((DockingPortCursorPane) currentMouseoverComponent).dockingPort;
			port.dockingComplete(currentDockingRegion);
			tmpReference.dockingCompleted();
		}
		else
			tmpReference.dockingCanceled();

		// remove the reference to the current docking region. we don't need it
		// anymore.
		currentDockingRegion = null;
	}

	private void paintComponentImpl(Graphics g)
	{
		if (currentDockingRegion == null)
			resolveMouseCursorRegion();
		if (!mouseOutOfBounds)
			g.drawImage(currentMouseImage, mouseLocation.x - 8, mouseLocation.y - 8, null, null);

		Color cached = g.getColor();
		g.setColor(Color.black);
		g.drawRect(mouseLocation.x - mouseOffsetFromDragSource.x, mouseLocation.y - mouseOffsetFromDragSource.y,
				dragSourceSize.width, dragSourceSize.height);
		g.setColor(cached);
	}

	private void initializeListenerCaching()
	{
		// it's easier for us if we remove the MouseMostionListener associated
		// with the dragSource
		// before dragging, so normally we'll try to do that. However, if
		// developers really want to
		// keep them in there, then they can implement the Dockable interface
		// for their dragSource and
		// let mouseMotionListenersBlockedWhileDragging() return false
		if (!dockableImpl.mouseMotionListenersBlockedWhileDragging())
			return;

		Component initiator = dockableImpl.getInitiator();
		cachedListeners = initiator.getListeners(MouseMotionListener.class);
		if (cachedListeners != null)
		{
			for (int i = 0; i < cachedListeners.length; i++)
				initiator.removeMouseMotionListener((MouseMotionListener) cachedListeners[i]);
		}
	}

	private void restoreCachedListeners()
	{
		if (cachedListeners == null)
			return;

		Component initiator = dockableImpl.getInitiator();
		for (int i = 0; i < cachedListeners.length; i++)
			initiator.addMouseMotionListener((MouseMotionListener) cachedListeners[i]);
		cachedListeners = null;
	}

	private void determineMouseLocationBasedOnDragSource(Point dragSourcePoint)
	{
		if (mouseLocation != null)
		{
			if (lastMouse == null)
				lastMouse = (Point) mouseLocation.clone();
			else
				lastMouse.setLocation(mouseLocation);
		}
		mouseLocation = SwingUtilities.convertPoint(dockableImpl.getInitiator(), dragSourcePoint, this);
	}

	private Component resolveCurrentMouseoverComponent()
	{
		return SwingUtilities.getDeepestComponentAt(this, mouseLocation.x, mouseLocation.y);
	}

	private void manageCursor()
	{
		try
		{
			if (mouseOutOfBounds)
			{
				currentMouseoverComponent = null;
				return;
			}

			// as the mouse moves around, we'll be covering the glasspane with
			// dockingPort proxies.
			// if the current mouse location is already covered by a proxy, then
			// we don't need to go
			// any further
			currentMouseoverComponent = resolveCurrentMouseoverComponent();
			if (currentMouseoverComponent != this)
				return;

			// there isn't a dockingPort proxy at the current mouse location.
			// resolve the underlying
			// dockingPort to see if a proxy is needed.
			DockingPort port = resolveDockingPortComponent();
			if (port == null)
				return;

			// the mouse is located over a dockingPort that has no associated
			// cursor proxy. create one
			// and add it to the glasspane. we can do an explicit cast here to
			// Component from DockingPort
			// because resolveDockingPortComponent() won't return a DockingPort
			// that isn't also a Component.
			Component dockingCmp = (Component) port;
			Point proxyLoc = SwingUtilities.convertPoint(dockingCmp.getParent(), dockingCmp.getLocation(), this);
			Dimension proxySize = dockingCmp.getSize();

			DockingPortCursorPane proxy = createDockingPortCursorProxy(port);
			super.add(proxy);
			proxy.setBounds(proxyLoc.x, proxyLoc.y, proxySize.width, proxySize.height);
			revalidate();
			resolveCurrentMouseoverComponent();
		}
		finally
		{
			resolveMouseCursorRegion();
			if (lastMouse == null)
				repaint();
			else
				repaintRect();
		}
	}

	private void repaintRect()
	{
		// this is a critical piece of code. because we're doing our own
		// graphics w/out component layering,
		// the current component has no concept of a 'damaged' area. thus, when
		// we call repaint(), it
		// repaints the entire screen. when the canvas size reaches around
		// 800x600 and there is a complex
		// layout underneath the glasspane, there become very noticeable delays
		// while the screen updates
		// (I timed them around 170-220ms between invocations of
		// paintComponent()). In this method, we keep
		// track of the area we need to repaint and try to keep things to a
		// minimum. This gives us a very
		// noticeable improvement in repaints during a drag operation.
		int minX = Math.min(lastMouse.x, mouseLocation.x);
		int minY = Math.min(lastMouse.y, mouseLocation.y);
		int maxX = Math.max(lastMouse.x, mouseLocation.x);
		int maxY = Math.max(lastMouse.y, mouseLocation.y);
		int w = dragSourceSize.width + maxX - minX;
		int h = dragSourceSize.height + maxY - minY;
		repaint(minX - mouseOffsetFromDragSource.x - 10, minY - mouseOffsetFromDragSource.y - 10, w + 20, h + 20);
	}

	private void resolveMouseCursorRegion()
	{
		// if the mouse is out of bounds, then we don't need any special image
		// to represent the cursor
		if (mouseOutOfBounds)
		{
			currentDockingRegion = DockingPort.UNKNOWN_REGION;
			currentMouseImage = EMPTY_IMAGE;
			return;
		}

		// if there is no dockingport cursor proxy beneath the mouse, then we're
		// dealing with
		// an unknown region.
		if (currentMouseoverComponent == this || currentMouseoverComponent == null)
		{
			currentDockingRegion = DockingPort.UNKNOWN_REGION;
			currentMouseImage = getCursorImageForRegion(currentDockingRegion);
			return;
		}

		// there is some dockingport cursor proxy beneath the mouse. determine
		// the appropriate
		// mouse cursor image based upon the proxy region.
		DockingPortCursorPane proxy = (DockingPortCursorPane) currentMouseoverComponent;
		currentDockingRegion = proxy.region;
		// if the underlying dockingport doesn't allow docking in the specified
		// region, then override
		// the region with UNKNOWN
		if (!proxy.dockingPort.allowsDocking(currentDockingRegion))
			currentDockingRegion = DockingPort.UNKNOWN_REGION;
		else if (proxy.dockingPort.getDockedComponent() == dockableImpl.getDockable())
			currentDockingRegion = DockingPort.UNKNOWN_REGION;

		currentMouseImage = getCursorImageForRegion(currentDockingRegion);
	}

	private DockingPort resolveDockingPortComponent()
	{
		return resolveDockingPortComponent(mouseLocation, rootContainer);
	}

	private DockingPort resolveDockingPortComponent(Point mouseLoc, RootSwingContainer root)
	{
		Point loc = SwingUtilities.convertPoint(this, mouseLoc, root.getContentPane());
		Component deepestComponent = SwingUtilities.getDeepestComponentAt(root.getContentPane(), loc.x, loc.y);
		if (deepestComponent == null)
			return null;

		// we're assured here the the deepest component is both a Component and
		// DockingPort in
		// this case, so we're okay to return here.
		if (deepestComponent instanceof DockingPort)
			return (DockingPort) deepestComponent;

		// getAncestorOfClass() will either return a null or a Container that is
		// also an instance of
		// DockingPort. Since Container is a subclass of Component, we're fine
		// in returning both
		// cases.
		return (DockingPort) SwingUtilities.getAncestorOfClass(DockingPort.class, deepestComponent);
	}

	private boolean dropComponent()
	{
		if (DockingPort.UNKNOWN_REGION.equals(currentDockingRegion))
			return false;

		if (!(currentMouseoverComponent instanceof DockingPortCursorPane))
			return false;

		DockingPort target = ((DockingPortCursorPane) currentMouseoverComponent).dockingPort;
		Component docked = target.getDockedComponent();
		Component dragSrc = dockableImpl.getDockable();
		if (dragSrc == docked)
			return false;

		Container parent = dragSrc.getParent();
		if (parent != null)
		{
			DockingPort oldPort = getParentDockingPort(dragSrc);
			if (oldPort != null)
				// if 'dragSrc' was previously docked, then undock it instead of
				// using a
				// simple remove(). this will allow the DockingPort to do any of
				// its own
				// cleanup operations associated with component removal.
				oldPort.undock(dragSrc);
			else
				// otherwise, just remove the component
				parent.remove(dragSrc);
			revalidateComponent(rootContainer.getContentPane());
		}

		// when the original parent reevaluates its container tree after
		// undocking, it checks to see how
		// many immediate child components it has. split layouts and tabbed
		// interfaces may be managed by
		// intermediate wrapper components. When undock() is called, the docking
		// port
		// may decide that some of its intermedite wrapper components are no
		// longer needed, and it may get
		// rid of them. this isn't a hard rule, but it's possible for any given
		// DockingPort implementation.
		// In this case, the target we had resolved earlier may have been
		// removed from the component tree
		// and may no longer be valid. to be safe, we'll resolve the target
		// docking port again and see if
		// it has changed. if so, we'll adopt the resolved port as our new
		// target.
		DockingPort resolvedTarget = resolveDockingPortComponent();
		if (resolvedTarget != target)
		{
			target = resolvedTarget;
			// reset this field for reuse outside this method
			((DockingPortCursorPane) currentMouseoverComponent).dockingPort = target;
		}

		boolean ret = target.dock(dragSrc, dockableImpl.getDockableDesc(), currentDockingRegion,
				dockableImpl.isDockedLayoutResizable());
		revalidateComponent((Component) target);
		return ret;
	}

	private DockingPort getParentDockingPort(Component comp)
	{
		DockingPort port = (DockingPort) SwingUtilities.getAncestorOfClass(DockingPort.class, comp);
		if (port == null)
			return null;

		return port.hasDockedChild(comp) ? port : null;
	}

	private void revalidateComponent(Component comp)
	{
		if (comp instanceof JComponent)
			((JComponent) comp).revalidate();
	}

	private void initMouseCursorOffset(Point mouseRelativeToDragSource)
	{
		// the drag-initiator component may be different than the ultimate
		// drag-source (for
		// example, a dockable component may use a child subcomponent to
		// initiate its drag, much
		// like a JInternalFrame's titlePane initiates drag opeartions for its
		// parent frame).
		// in this case, we'll need to convert the mouse point to determine the
		// real offset needed
		// for drawing the rectangular outline during the drag operation.
		boolean conversionNeeded = dockableImpl.getInitiator() != dockableImpl.getDockable();
		if (conversionNeeded)
			mouseRelativeToDragSource = SwingUtilities.convertPoint(dockableImpl.getInitiator(), mouseRelativeToDragSource,
					dockableImpl.getDockable());

		mouseOffsetFromDragSource.x = mouseRelativeToDragSource.x;
		mouseOffsetFromDragSource.y = mouseRelativeToDragSource.y;
	}

	private class DragEventManager extends MouseAdapter implements MouseMotionListener
	{
		public void mouseDragged(MouseEvent e)
		{
			// e.getSource() is the drag-initiator, so we'll need to convert the
			// mouse
			// coordinates to the glassPane coordinate space
			determineMouseLocationBasedOnDragSource(e.getPoint());
			manageCursor();
		}

		public void mouseMoved(MouseEvent e)
		{
			// doesn't do anything
		}

		public void mouseEntered(MouseEvent e)
		{
			// e.getSource() is the glassPane, not the drag-source
			mouseOutOfBounds = false;
		}

		public void mouseExited(MouseEvent e)
		{
			// e.getSource() is the glassPane, not the drag-source
			mouseOutOfBounds = true;
		}

		public void mouseReleased(MouseEvent e)
		{
			stopDragImpl(dockableImpl);
		}
	}

	private static class DockingPortCursorPane extends JPanel
	{
		private String region;
		private DockingPort dockingPort;

		private DockingPortCursorPane(DockingPort port, String cursorRegion)
		{
			super(null);
			this.setOpaque(false);
			region = cursorRegion;
			dockingPort = port;
			if (DockingPort.CENTER_REGION.equals(region))
				this.setLayout(new BorderLayout(0, 0));
		}

		private void createNorthRegion(int height)
		{
			createRegion(height, DockingPort.NORTH_REGION, BorderLayout.NORTH);
		}

		private void createSouthRegion(int height)
		{
			createRegion(height, DockingPort.SOUTH_REGION, BorderLayout.SOUTH);
		}

		private void createEastRegion(int width)
		{
			createRegion(width, DockingPort.EAST_REGION, BorderLayout.EAST);
		}

		private void createWestRegion(int width)
		{
			createRegion(width, DockingPort.WEST_REGION, BorderLayout.WEST);
		}

		private void createRegion(int dim, String cursorRegion, String layoutRegion)
		{
			DockingPortCursorPane pane = new DockingPortCursorPane(dockingPort, cursorRegion);
			Dimension d = pane.getPreferredSize();
			pane.setPreferredSize(new Dimension(dim, dim));
			super.add(pane, layoutRegion);
		}
	}

	private static class DragInitiationListener extends MouseMotionAdapter
	{
		private boolean enabled;
		private Dockable initiator;

		private DragInitiationListener()
		{
			setEnabled(true);
		}

		public void mouseDragged(MouseEvent e)
		{
			if (enabled && initiator != null && initiator.isDockingEnabled())
			{
				// by disabling the listener here, we don't process any events
				// after the drag-start.
				// The DockingManager is responsible for re-enabling this
				// listener after the drag
				// operation has finished.
				setEnabled(false);
				startDrag(initiator, e.getPoint());
			}
		}

		private void setInitiator(Dockable init)
		{
			initiator = init;
		}

		private void setEnabled(boolean b)
		{
			enabled = b;
		}
	}
}
