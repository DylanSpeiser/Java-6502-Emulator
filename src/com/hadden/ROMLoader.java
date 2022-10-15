package com.hadden;

import java.io.*;
import java.util.ArrayList;

public class ROMLoader
{
	static ArrayList<Byte> ROM = new ArrayList<Byte>();

	
	public static byte[] readROM(File selectedFile)
	{
		return readROM(selectedFile,null);
	}
	
	
	public static byte[] readROM(File file,Integer[] baseAddress)
	{
		ROM = new ArrayList<Byte>();
		
		int value = 0;
		//InputStream inputStream;
		DataInputStream inputStream;
		try
		{
			byte[] bytes = new byte[4];
			
			
			inputStream = new DataInputStream(new FileInputStream(file));
			
			//inputStream = new FileInputStream(file);
			int byteRead;

			if(file.getPath().endsWith(".exm"))
			{
				
				value = inputStream.readInt();
				//for (byte b : bytes) {
				//    value = (value << 8) + (b & 0xFF);
				//}
			}
			
			if(file.getName().startsWith("B-") )
			{
				String[] parts = file.getName().split("-");
				if(parts.length > 1)
				{
					value = Integer.parseInt(parts[1],16);
				}
				
			}
			
			while ((byteRead = inputStream.read()) != -1)
			{
				ROM.add((byte) byteRead);
			}

		} catch (IOException e)
		{
			e.printStackTrace();
		}

		byte[] ROMArray = new byte[ROM.size()];

		int counter = 0;
		for (Byte b : ROM)
		{
			ROMArray[counter] = b.byteValue();
			counter++;
		}

		if(baseAddress != null && value > 0)
		{
			baseAddress[0] = Integer.valueOf(value);
		}
		
		
		return ROMArray;
	}

	
	
	public static String ROMString(byte[] array, int bytesPerLine, boolean addresses)
	{
		StringBuilder sb = new StringBuilder();

		if (addresses)
			sb.append("0000: ");

		for (int i = 1; i <= array.length; i++)
		{
			if ((i % bytesPerLine != 0) || (i == 0))
			{
				sb.append(byteToHexString(array[i - 1]) + " ");
			}
			else
			{
				String zeroes = "0000";
				sb.append(byteToHexString(array[i - 1]) + "\n");
				if (addresses)
					sb.append(zeroes.substring(0, 4 - Integer.toHexString(i).length()) + Integer.toHexString(i) + ": ");
			}
		}

		return sb.toString();
	}

	public static String ROMString(byte[] array)
	{
		StringBuilder sb = new StringBuilder();
		int bytesPerLine = 16;

		for (int i = 1; i <= array.length; i++)
		{
			if ((i % bytesPerLine != 0) || (i == 0))
			{
				sb.append(byteToHexString(array[i - 1]) + " ");
			}
			else
			{
				sb.append(byteToHexString(array[i - 1]) + "\n");
			}
		}

		return sb.toString();
	}

	public static String byteToHexString(byte b)
	{
		if (Byte.toUnsignedInt(b) < 16)
			return "0" + Integer.toHexString(Byte.toUnsignedInt(b)).toUpperCase();
		return Integer.toHexString(Byte.toUnsignedInt(b)).toUpperCase();
	}

	public static String padStringWithZeroes(String s, int padLength)
	{
		char[] pads = new char[padLength - s.length()];
		for (int i = 0; i < pads.length; i++)
		{
			pads[i] = '0';
		}
		return String.valueOf(pads) + s;
	}



//	public static void main(String[] args) {
//		byte[] be = {(byte)255,(byte)0,(byte)16,(byte)128,(byte)11,(byte)32,(byte)255,(byte)0,(byte)16,(byte)128,(byte)11,(byte)32,(byte)255,(byte)0,(byte)16,(byte)128,(byte)11,(byte)32,(byte)255,(byte)0,(byte)16,(byte)128,(byte)11,(byte)32,(byte)255,(byte)0,(byte)16,(byte)128,(byte)11,(byte)32,(byte)255,(byte)0,(byte)16,(byte)128,(byte)11,(byte)32};
//		System.out.println(ROMString(readROM(ROMfile),16,true));
//	}
}
