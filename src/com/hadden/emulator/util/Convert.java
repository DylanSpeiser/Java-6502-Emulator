package com.hadden.emulator.util;

public class Convert
{
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
}
