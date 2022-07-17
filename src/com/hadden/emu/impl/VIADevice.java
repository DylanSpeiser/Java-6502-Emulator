package com.hadden.emu.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.*;

import com.hadden.emu.BusAddressRange;
import com.hadden.emu.BusDevice;
import com.hadden.emu.BusDevice.IOSize;

public class VIADevice implements BusDevice
{
	Timer t;
	Timer cursorTimer;
	Font lcdFont;
	Scanner s;

	boolean graphicalCursorBlinkFlag = false;

	boolean debug = true;

	// Internal flags
	int cursorPos = 0;
	boolean increment = true;
	boolean displayPower = false;
	boolean cursor = false;
	boolean cursorBlink = false;
	boolean fourBitMode = false;

	BusAddressRange bar = null;
	char[] bank = new char[0x50];
	boolean rs = false;

	byte PORTA = 0x00;
	byte PORTB = 0x00;
	byte DDRA = 0x00;
	byte DDRB = 0x00;
	byte PCR = 0x00;
	byte IFR = 0x00;
	byte IER = 0x00;

	public byte read(int address)
	{
		switch (address)
		{
		case 0x6000:
			IFR &= (byte) (0b01100111);
			return 0;
		case 0x6001:
			IFR &= (byte) (0b01111100);
			return 0;
		case 0x6002:
			return 0;
		case 0x6003:
			return 0;
		case 0x600C:
			return PCR;
		case 0x600D:
			return IFR;
		case 0x600E:
			return IER;
		}
		return 0;
	}

	public void write(int address, byte data) {
		switch (address){
			case 0x6000:
				PORTB = data;
				break;
			case 0x6001:
				PORTA = data;
				if ((data&0x80)==0x80) {
					//EaterEmulator.lcd.write((PORTA&0x20)==0x20, (byte)(PORTB&DDRB));
				}
				break;
			case 0x6002:
				DDRB = data;
				break;
			case 0x6003:
				DDRA = data;
				break;
			case 0x600C:
				PCR = data;
				break;
			case 0x600D:
				IFR = data;
				break;
			case 0x600E:
				IER = data;
				break;
		}
	}

	public void CA1()
	{
		if ((IER &= (byte) (0b00000010)) == 0b00000010)
		{
			IFR |= (byte) (0b10000010);
			//EaterEmulator.cpu.interruptRequested = true;
		}
	}

	public void CA2()
	{
		if ((IER &= (byte) (0b00000010)) == 0b00000001)
		{
			IFR |= (byte) (0b10000001);
			//EaterEmulator.cpu.interruptRequested = true;
		}
	}

	public void CB1()
	{
		if ((IER &= (byte) (0b00000010)) == 0b00010000)
		{
			IFR |= (byte) (0b10010000);
			//EaterEmulator.cpu.interruptRequested = true;
		}
	}

	public void CB2()
	{
		if ((IER &= (byte) (0b00000010)) == 0b00001000)
		{
			IFR |= (byte) (0b10001000);
			//EaterEmulator.cpu.interruptRequested = true;
		}
	}

	public VIADevice(int portAddress)
	{
		this.bar = new BusAddressRange(portAddress, 2, 1);

		String s = "";

		for (int i = 0; i < 0x50; i++)
		{
			if (i < s.length())
			{
				bank[i] = s.charAt(i);
			}
			else
			{
				bank[i] = ' ';
			}
		}
	}

	@Override
	public String getName()
	{
		return "VIA";
	}

	@Override
	public BusAddressRange getBusAddressRange()
	{
		return this.bar;
	}

	@Override
	public void writeAddress(int address, int value, IOSize size)
	{
		if (address == bar.getLowAddress())
			this.rs = (value == 1 ? true : false);

		if (address == bar.getHighAddress())
		{
			//write(this.rs, (byte) value);
			rs = false;
		}

	}

	@Override
	public int readAddressSigned(int address, IOSize size)
	{
		int effectiveAddress = address - this.bar.getLowAddress();
		return bank[effectiveAddress];
	}

	@Override
	public int readAddressUnsigned(int address, IOSize size)
	{
		int effectiveAddress = address - this.bar.getLowAddress();
		return bank[effectiveAddress];
	}

	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		VIADevice lcd = new VIADevice(0x0000B000);
		System.out.println(
				lcd.getBusAddressRange().getLowAddressHex() + ":" + lcd.getBusAddressRange().getHighAddressHex());

		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		System.out.println("[RS] [data]");

		while (true)
		{
			String input = scan.nextLine();

			String address = input.substring(0, 8);

			String asciiChar = input.substring(8);

			int location = Integer.parseInt(address, 16);
			int value = Integer.parseInt(asciiChar);

			lcd.writeAddress(location, value, IOSize.IO8Bit);
			// lcd.writeAddress(0x0000B001, data, IOSize.IO8Bit);

		}
	}

}
