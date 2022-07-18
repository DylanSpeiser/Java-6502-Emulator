package com.hadden.roms;

import java.awt.Font;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;

public class ROMManager
{
	public ROMManager()
	{
		
	}

	public static byte[] loadROM(String romName)
	{
		byte[] romBytes = null;
		
		
		try
		{
			BufferedInputStream bin = new BufferedInputStream(ROMManager.class.getResourceAsStream(romName));
			if(bin!=null)
			{
				try
				{
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					if (bout != null)
					{
						try
						{
							int b = 0;
							while ((b = bin.read()) > -1)
							{
								bout.write(b);
							}
							romBytes = bout.toByteArray();
						}
						finally
						{
							bout.close();
						}						
					} 
				}
				finally
				{
					bin.close();
				}				
			}			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}		
		
		return romBytes;
	}
	
	public static void main(String[] args)
	{
		byte[] rom = ROMManager.loadROM("main.rom");
		System.out.println(rom.length);
	}
	
}
