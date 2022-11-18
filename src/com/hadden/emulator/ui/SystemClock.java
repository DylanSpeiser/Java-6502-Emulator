package com.hadden.emulator.ui;

import java.util.ArrayList;
import java.util.List;

import com.hadden.emulator.Clock;
import com.hadden.emulator.ClockLine;

public class SystemClock implements Clock
{
	public Thread clockThread;
	public boolean clockState 	= false;
	public boolean haltFlag 	= true;
	public boolean slowerClock 	= false;

	private List<ClockLine> lines = new ArrayList<ClockLine>();
	
	public SystemClock()
	{
		clockThread = new Thread()
		{
			public void run()
			{
				while (true)
				{
					try
					{
						if (clockState)
						{
							//System.out.println("<SystemClock>");
							for(ClockLine cl : lines)
							{
								cl.pulse();
							}															
						}
						else
						{
							//System.out.println("<Idle>");
							Thread.sleep(1);
						}
						if (slowerClock)
						{
							try
							{
								Thread.sleep(500);
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						}
						// remove later
						//Thread.sleep(1);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					//System.out.println("<tick>");
				}
			}
		};
		clockThread.setDaemon(true);
		clockThread.start();
	}

	@Override
	public boolean isEnabled()
	{
		//System.out.println("<SystemClock::isEnabled>:" + clockState);
		return clockState;
	}

	@Override
	public boolean isHalted()
	{
		return haltFlag;
	}

	@Override
	public boolean isSlow()
	{
		return slowerClock;
	}

	@Override
	public void addClockLine(ClockLine line)
	{
		lines.add(line);
	}

	@Override
	public synchronized void setEnabled(boolean b)
	{
		//System.out.println("<SystemClock::setEnabled>:" + b);
		clockState = b;		
	}

	@Override
	public void setSlow(boolean b)
	{
		slowerClock = b;
	}

	@Override
	public void pulse()
	{
		for(ClockLine cl : lines)
		{
			cl.pulse();
		}	
	}

}
