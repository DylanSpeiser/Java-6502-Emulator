package com.example.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import com.example.gui.docking.DefaultDockingPort;
import com.example.gui.docking.DockingManager;
import com.example.gui.docking.DockingPort;
import com.juse.emulator.interfaces.AddressMap;
import com.juse.emulator.interfaces.BusListener;
import com.juse.emulator.interfaces.DeviceDebugger;
import com.juse.emulator.interfaces.Emulator;
import com.juse.emulator.interfaces.Telemetry;
import com.juse.emulator.interfaces.TelemetryInfo;
import com.juse.emulator.interfaces.ui.EmulatorDisplay;
import com.juse.emulator.interfaces.ui.EmulatorFrame;
import com.juse.emulator.util.translate.Convert;


public class DemoUITest4 extends JFrame implements EmulatorFrame, BusListener, EmulatorDisplay, KeyListener, MouseWheelListener, ActionListener
{
	public static void main(String[] args)
	{
		JFrame f = new DemoUITest4("Simple Docking Test");
		f.setSize(1920, 1080);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

	private Emulator emulator;

	public DemoUITest4(String emulatorTitle) 
	{
	    super(emulatorTitle);
	    this.setSize(new Dimension(1920, 1080));

	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);	 
		//setLayout(new BorderLayout());
	    
	    //setContentPane(createContentPane());
	}

	private JPanel createContentPane()
	{
		JPanel p = new JPanel(new BorderLayout(5, 5));
		p.setBackground(Color.white);
		
		
		
		
		p.add(buildDockingPort(Color.BLUE, "CPU"), BorderLayout.WEST);
		p.add(buildDockingPort(Color.GREEN, "RAM"), BorderLayout.EAST);
		p.add(buildDockingPort(Color.BLACK, "Devices"), BorderLayout.NORTH);
		p.add(buildDockingPort(Color.RED, "Instructions"), BorderLayout.CENTER);
		//p.add(createDockingPort(), BorderLayout.CENTER);
		return p;
	}

	private JPanel buildDockingPort(Color color, final String desc)
	{
		// create the DockingPort
		//DefaultDockingPort port = createDockingPort();

		final Color myColor = color;
		// create and register the Dockable panel
		JPanel p = new JPanel();
		Border blackline = BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),desc);		
		((javax.swing.border.TitledBorder)blackline).setTitleFont(new Font("Arial", Font.BOLD, 18));
		p.setBorder(blackline);
		p.setBackground(new Color(246,246,246));
		p.setLayout(new BorderLayout());
		
		
		
		JPanel pp; 
		
		if("Devices".equals(desc))
		{
			pp = createDevicesSubPanel(desc, myColor);
			pp.setPreferredSize(new Dimension(300,150));
		}
		else if("Instructions".equals(desc)) 
		{
			pp = createInstructionsSubPanel(desc, myColor);			
			pp.setPreferredSize(new Dimension(500,200));
		}
		else if("RAM".equals(desc)) 
		{
			pp = createRAMSubPanel(desc, myColor);			
			pp.setPreferredSize(new Dimension(350,200));
		}
		else if("CPU".equals(desc)) 
		{
			pp = createRegistersSubPanel(desc, myColor);			
			pp.setPreferredSize(new Dimension(400,200));
		}
		else
		{
			pp = createSubPanel(desc,myColor);
			pp.setPreferredSize(new Dimension(100,100));
		}
			
		p.add(pp,BorderLayout.CENTER);
		
		//DockingManager.registerDockable(p, desc, true);

		// dock the panel and return the DockingPort
		//port.dock(p, desc, DockingPort.CENTER_REGION, false);
		return p;
	}

	private JPanel createInstructionsSubPanel(String desc, Color myColor)
	{
		JPanel pp = new ExInstructionsImpl(this.emulator);
		return pp;
	}

	private JPanel createRAMSubPanel(String desc, Color myColor)
	{
		//JPanel pp = new ExRAMImpl(this.emulator);
		JPanel pp = new ExRAMGridImpl(this.emulator);
		return pp;
	}

	
	protected JPanel createSubPanel(final String desc, final Color myColor)
	{
		JPanel pp = new JPanel() 
		{
			@Override
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				
				Insets insets = this.getInsets();
				
				g.setColor(myColor);
				
				g.setFont(new Font("Calibri Bold", 50, 16));
				// g.drawString("Ben Eater 6502 Emulator", 40, 50);
				g.drawString(desc, insets.left + g.getFontMetrics().getWidths()[32], 
						           insets.top + g.getFontMetrics().getHeight() + g.getFontMetrics().getDescent());
				System.out.println("Insets " + desc + ":" + insets.left + "," + insets.top);
			     
				// Version
				if(System.getProperty("os.name").toLowerCase().contains("windows"))
					g.setFont(new Font("Courier New Bold", Font.BOLD, 20));
				else
					g.setFont(new Font("Monospaced", Font.BOLD, 20));
				

				TelemetryInfo ti = null;
				Telemetry t = emulator.getCPU().getTelemetry();
				if(t instanceof TelemetryInfo)
				{
					ti = (TelemetryInfo)t;
				}			
			}
		};
		pp.setBackground(Color.WHITE);
		return pp;
	}
	
	protected JPanel createDevicesSubPanel(final String desc, final Color myColor)
	{
		JPanel pp = new ExGUIDisplayImpl(this.emulator);
		return pp;
	}

	protected JPanel createRegistersSubPanel(final String desc, final Color myColor)
	{
		JPanel pp = new ExRegistersImpl(this.emulator);
		return pp;
	}
	
	private DefaultDockingPort createDockingPort()
	{
		DefaultDockingPort port = new DefaultDockingPort();
		port.setBackground(Color.gray);
		port.setPreferredSize(new Dimension(100, 100));
		return port;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initDisplay(EmulatorDisplay ed)
	{
		// TODO Auto-generated method stub		
	}

	@Override
	public void showFrame(boolean bVisible)
	{ 
		this.setVisible(bVisible);		
	}

	@Override
	public void initFrame(Emulator emu)
	{
		this.emulator = emu;
		((AddressMap)emu.getBus()).addBusListener((BusListener)this);		
		setContentPane(createContentPane());
		redraw();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void redraw()
	{
		this.repaint();		
	}

	@Override
	public void readListener(short address)
	{
		//System.out.println("DemoUITest3::readListener");		
	}

	@Override
	public void writeListener(short address, byte data)
	{
		//System.out.println("DemoUITest3::writeListener");		
	}

	@Override
	public void busReset()
	{
		// TODO Auto-generated method stub
		
	}
}
