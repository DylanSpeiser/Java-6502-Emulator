package com.hadden.fonts;

import java.awt.Font;

import java.net.URL;

public class FontManager
{
	public static final int FONT_5x8_LCD = 0;
	
	private static final String[][] fontNames = {{"5x8_lcd_hd44780u_a02.ttf"}};
	
	public FontManager()
	{
		
	}
	
	public static Font loadFont(int fontId)
	{
		Font font = null;
		
		if(fontId < fontNames.length)
		{
			font = loadFont(fontNames[fontId][0]);
		}
		
		return font;
	}
	
	public static Font loadFont(String fontName)
	{
		return loadFont(fontName,47f);
	}
	
	public static Font loadFont(String fontName, float scale)
	{
		Font font = null;
		
		
		try
		{
			URL url = FontManager.class.getResource(fontName);			
			font = Font.createFont(Font.TRUETYPE_FONT, url.openStream()).deriveFont(scale);
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return font;
	}
	
	public static void main(String[] args)
	{
		Font f = FontManager.loadFont(FontManager.FONT_5x8_LCD);
		System.out.println(f.getFamily());
	}
	
}
