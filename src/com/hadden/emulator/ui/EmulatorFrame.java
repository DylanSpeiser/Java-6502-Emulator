package com.hadden.emulator.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.hadden.SystemConfig;
import com.hadden.SystemConfigLoader;
import com.hadden.emulator.project.CC65ProjectImpl;
import com.hadden.emulator.project.Project;

public class EmulatorFrame extends JFrame implements ActionListener
{
	public String versionString = "1.0";

	// Swing Things
	JPanel p = new JPanel();
	JPanel header = new JPanel();
	JFileChooser fc = new JFileChooser();

	private String platform;

	public EmulatorFrame(String title)
	{
		platform = System.getProperty("os.name").toLowerCase();

		if (!platform.contains("windows"))
		{
			//JDialog.setDefaultLookAndFeelDecorated(true);
			//JFrame.setDefaultLookAndFeelDecorated(true);
			//this.setUndecorated(false);
		}
		else
		{
			//JDialog.setDefaultLookAndFeelDecorated(true);
			//JFrame.setDefaultLookAndFeelDecorated(true);
			//this.setUndecorated(true);
		}

		try
		{
			UIManager.setLookAndFeel(new MetalLookAndFeel());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// Final Setup
		// separate out impl
		/*
		 * JPanel jp = (JPanel)new EmulatorDisplayImpl(null);
		 * jp.setVisible(true); jp.setSize(new Dimension(1920,1200));
		 * this.setContentPane(jp);
		 */
		//
		// this.setUndecorated(true);
		if(title!=null)
			this.setTitle(title + "(" + platform.toUpperCase() + ")");
		// this.setSize(new Dimension(1920,1080));
		// this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		// this.setResizable(true);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{

	}

	public static void main(String[] args)
	{
		EmulatorFrame ef = new EmulatorFrame("New Window 1");
		
		ef.setVisible(true);
		ef.setSize(new Dimension(800,600));

		EmulatorFrame ef2 = new EmulatorFrame("New Window 2");
		
		ef2.setVisible(true);
		ef2.setSize(new Dimension(800,600));

		EmulatorFrame ef3 = new EmulatorFrame("New Window 3");
		
		ef3.setVisible(true);
		ef3.setSize(new Dimension(800,600));
		
		
	}

	
}
