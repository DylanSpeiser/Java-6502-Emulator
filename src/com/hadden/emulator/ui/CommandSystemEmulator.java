package com.hadden.emulator.ui;
//Original Code by Dylan Speiser

//https://github.com/DylanSpeiser

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;

import com.hadden.emu.AddressMap;
import com.hadden.emu.AddressMapImpl;
import com.hadden.emu.Bus;
import com.hadden.emu.BusDevice;
import com.hadden.emu.BusIRQ;
import com.hadden.emu.CPU;
import com.hadden.emu.RAM;
import com.hadden.emu.ROM;
import com.hadden.emu.VIA;
import com.hadden.emu.c64.BASICDevice;
import com.hadden.emu.c64.CIADevice;
import com.hadden.emu.c64.CharacterDevice;
import com.hadden.emu.c64.KernalDevice;
import com.hadden.emu.c64.KeyboardDevice;
import com.hadden.emu.c64.ScreenDevice;
import com.hadden.emu.c64.VICIIDevice;
import com.hadden.emu.cpu.MOS65C02;
import com.hadden.emu.cpu.MOS65C02A;
import com.hadden.emu.impl.DisplayDevice;
import com.hadden.emu.impl.LCDDevice;
import com.hadden.emu.impl.MuxDevice;
import com.hadden.emu.impl.RAMDevice;
import com.hadden.emu.impl.ROMDevice;
import com.hadden.emu.impl.SerialDevice;
import com.hadden.emu.impl.TimerDevice;
import com.hadden.emu.impl.MuxDevice.MuxMapper;
import com.hadden.roms.ROMManager;
import com.hadden.emulator.Clock;
import com.hadden.emulator.ClockLine;
import com.hadden.emulator.Emulator;

@SuppressWarnings("serial")
public class CommandSystemEmulator implements Emulator
{
	public String versionString = "1.0";
	private Clock clock = null;

	public CommandSystemEmulator()
	{
		clock = new SystemClock();
		clock.addClockLine(clockLine);
		clock.setEnabled(true);
	}

	@Override
	public String getSystemVersion()
	{
		return this.versionString;
	}

	@Override
	public Clock getClock()
	{
		return clock;
	}

	private ClockLine clockLine = new ClockLine()
	{
		@Override
		public void pulse()
		{
			System.out.println("CommandSystemEmulator::ClockLine");
		}
	};

	@Override
	public String getTitle()
	{
		return "Command Line Emulator";
	}

	private static boolean running = true;
	
	public static void main(String[] args)
	{
		CommandSystemEmulator emu = new CommandSystemEmulator();

		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				running = false;
			}
		});

		while(running)
		{
			try
			{
				int c = System.in.read();
								
				if(c == 'c')
				{
					System.out.println("CommandSystemEmulator::Toggle Clock");
					emu.getClock().setEnabled(!emu.getClock().isEnabled());
				}
				//Thread.sleep(500);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		System.out.println("CommandSystemEmulator::Exit");
	}

	@Override
	public CPU getCPU()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset()
	{
		// TODO Auto-generated method stub
		
	}

}
