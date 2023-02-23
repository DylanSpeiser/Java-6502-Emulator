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

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * This class provides <code>static</code> convenience methods for resource
 * management, including resource lookups and image, icon, and cursor creation.
 * 
 * @author Chris Butler
 */
public class ResourceManager
{

	/**
	 * Performs resource lookups using the <code>ClassLoader</code> and
	 * classpath. This method attemps to consolidate several techniques used for
	 * resource lookup in different situations, providing a common API that
	 * works the same from standalone applications to applets to
	 * multiple-classloader container-managed applications. Returns
	 * <code>null</code> if specified resource cannot be found.
	 * 
	 * @param uri
	 *            the String describing the resource to be looked up
	 * @return a <code>URL</code> representing the resource that has been looked
	 *         up.
	 */
	public static URL getResource(String uri)
	{
		URL url = ResourceManager.class.getResource(uri);
		if (url == null)
			url = ClassLoader.getSystemResource(uri);

		// if we still couldn't find the resource, then slash it and try again
		if (url == null && !uri.startsWith("/"))
			url = getResource("/" + uri);

		return url;
	}

	/**
	 * Returns an <code>Image</code> object based on the specified resource URL.
	 * Does not perform any caching on the <code>Image</code> object, so a new
	 * object will be created with each call to this method.
	 * 
	 * @param url
	 *            the <code>String</code> describing the resource to be looked
	 *            up
	 * @exception NullPointerException
	 *                if specified resource cannot be found.
	 * @return an <code>Image</code> created from the specified resource URL
	 */
	public static Image createImage(String url)
	{
		try
		{
			URL location = getResource(url);
			return Toolkit.getDefaultToolkit().createImage(location);
		}
		catch (NullPointerException e)
		{
			throw new NullPointerException("Unable to locate image: " + url);
		}
	}

	/**
	 * Returns an <code>ImageIcon</code> object based on the specified resource
	 * URL. Uses the <code>ImageIcon</code> constructor internally instead of
	 * dispatching to <code>createImage(String url)</code>, so
	 * <code>Image</code> objects are cached via the <code>MediaTracker</code>.
	 * 
	 * @param url
	 *            the <code>String</code> describing the resource to be looked
	 *            up
	 * @exception NullPointerException
	 *                if specified resource cannot be found.
	 * @return an <code>ImageIcon</code> created from the specified resource URL
	 */
	public static ImageIcon createIcon(String url)
	{
		try
		{
			URL location = getResource(url);
			return new ImageIcon(location);
		}
		catch (NullPointerException e)
		{
			throw new NullPointerException("Unable to locate image: " + url);
		}
	}

	/**
	 * Returns a <code>Cursor</code> object based on the specified resource URL.
	 * Throws a <code>NullPointerException</code> if specified resource cannot
	 * be found. Dispatches to <code>createImage(String url)</code>, so
	 * <code>Image</code> objects are <b>not</b> cached via
	 * the<code>MediaTracker</code>.
	 * 
	 * @param url
	 *            the <code>String</code> describing the resource to be looked
	 *            up
	 * @param hotPoint
	 *            the X and Y of the large cursor's hot spot. The hotSpot values
	 *            must be less than the Dimension returned by
	 *            getBestCursorSize().
	 * @param name
	 *            a localized description of the cursor, for Java Accessibility
	 *            use.
	 * @exception NullPointerException
	 *                if specified resource cannot be found.
	 * @exception IndexOutOfBoundsException
	 *                if the hotSpot values are outside
	 * @return a <code>Cursor</code> created from the specified resource URL
	 */
	public static Cursor createCursor(String url, Point hotPoint, String name)
	{
		Image image = createImage(url);
		Cursor c = Toolkit.getDefaultToolkit().createCustomCursor(image, hotPoint, name);
		return c;
	}
}
