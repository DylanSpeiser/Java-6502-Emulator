package com.example.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.juse.emulator.interfaces.AddressMap;
import com.juse.emulator.interfaces.BusListener;
import com.juse.emulator.interfaces.Emulator;
import com.juse.emulator.interfaces.ui.EmulatorDisplay;
import com.juse.emulator.interfaces.ui.EmulatorFrame;



public class ExGUIFrameImpl extends JFrame implements EmulatorFrame, ActionListener
{
	public String versionString = "1.0";

	// Swing Things
	JPanel p = new JPanel();
	JPanel header = new JPanel();
	JFileChooser fc = new JFileChooser();

	private String platform;

	private EmulatorDisplay emulatorDisplay;

	public ExGUIFrameImpl(String title, int initialX, int initialY, int initialWidth, int initialHeight)
	{
		platform = System.getProperty("os.name").toLowerCase();
		// Global Stuff:
		System.setProperty("sun.java2d.opengl", "true");
		//
		// set local look
		//
		try
		{
			UIManager.setLookAndFeel(new MetalLookAndFeel());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// Final Setup
		this.setSize(new Dimension(initialWidth,initialHeight));
		if(initialX == -1 && initialY == -1)
		{
			this.setLocationRelativeTo(null);
			//Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			//this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);			
		}
		else
			this.setLocation(initialX, initialY);
		
		if(title!=null)
			this.setTitle(title + " (" + platform.toUpperCase() + ")");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public void initFrame(Emulator emu) 
	{
		//
		// Assume the contents of the frame are known to the Frame
		//
		try
		{
			EmulatorDisplay ed = new ExGUIDisplayImpl(emu);
			if(ed!=null)
			{
				//
				// Allow UI to see Bus events
				//
				((AddressMap)emu.getBus()).addBusListener((BusListener)ed);
				initDisplay(ed);
			}
		}
		catch (Exception e)
		{
			System.out.println("Unable to instantiate user interface.");
		}
	}
	
	public void initDisplay(EmulatorDisplay emulatorDisplay) 
	{
		this.emulatorDisplay = emulatorDisplay;
		
		// Final Setup
		JPanel jp = (JPanel)emulatorDisplay;
		jp.setVisible(true);		
		jp.setSize(new Dimension(1920,1080));
		this.setContentPane(jp);
		//
		//this.setUndecorated(false);
		this.setTitle("Green System Emulator");		
		//this.setSize(new Dimension(1920,1080));
		this.setVisible(true);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setResizable(true);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e)
	{

	}

	public static void main(String[] args)
	{
		int x = 0;
		int y = 0;
		
		//
		// test layouts
		//
		
		EmulatorFrame ef1 = new ExGUIFrameImpl("New Window 1", -1, -1, 800, 600);
		ef1.showFrame(true);

		EmulatorFrame ef2 = new ExGUIFrameImpl("New Window 2", x+=100, y+=100, 800, 600);
		ef2.showFrame(true);

		EmulatorFrame ef3 = new ExGUIFrameImpl("New Window 3", x+=100, y+=100, 800, 600);
		ef3.showFrame(true);
	}


	@Override
	public void showFrame(boolean bVisible)
	{
		this.setVisible(bVisible);		
	}


	
}
