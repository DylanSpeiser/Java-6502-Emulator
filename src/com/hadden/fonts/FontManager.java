package com.hadden.fonts;

import java.awt.Font;

import java.net.URL;

public class FontManager
{
	public static final int FONT_5x8_LCD = 0;
	public static final int FONT_ARCADE_CLS = 1;
	public static final int FONT_LCD_BLOCK = 2;
	public static final int FONT_KONG_TEXT = 3;
	public static final int FONT_NPIXELTEXT = 4;
	public static final int FONT_JOYSTICK_TEXT = 5;
	public static final int C64_TEXT = 6;
	
	private static final String[][] fontNames = {{"5x8_lcd_hd44780u_a02.ttf"},
			                                     {"ARCADECLASSIC.TTF"},
			                                     {"lcd-block.ttf"},
			                                     {"kongtext.ttf"},
			                                     {"NeuePixelSans.ttf"},
			                                     {"joystixmono.ttf"},
			                                     {"C64_Pro_Mono-STYLE.ttf"}};
	
	public FontManager()
	{
		
	}

	public static Font loadFont(int fontId)
	{
		Font font = null;
		
		if(fontId < fontNames.length)
		{
			font = loadFont(fontNames[fontId][0],47f);
		}
		
		return font;
	}
	
	public static Font loadFont(int fontId, float scale)
	{
		Font font = null;
		
		if(fontId < fontNames.length)
		{
			font = loadFont(fontNames[fontId][0],scale);
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
			System.out.println("FontManager:URL:" + url);
			font = Font.createFont(Font.TRUETYPE_FONT | Font., url.openStream()).deriveFont(scale);
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
