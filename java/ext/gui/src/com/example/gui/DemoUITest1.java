package com.example.gui;


//java Program to create multiple internal frames
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.awt.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
class DemoUITest1 extends JFrame {

	// frame
	static JFrame f;

	// label to display text
	static JLabel l, l1;

	// main class
	public static void main(String[] args)
	{
		// Global Stuff:
		System.setProperty("sun.java2d.opengl", "true");
		try
		{
			UIManager.setLookAndFeel(new MetalLookAndFeel());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// create a new frame
		f = new JFrame("JUSE");
		try
		{
			InputStream is = DemoUITest1.class.getResourceAsStream("/resources/juseandjava.png");
			BufferedImage bufferedImage = ImageIO.read(is);
			//f.setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\devprojects\\git\\Java-System-Emulator\\java\\ext\\gui\\src\\com\\example\\gui\\juseandjava.png"));
			f.setIconImage(bufferedImage);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// set layout of frame
		f.setLayout(new FlowLayout());

		// create a internal frame
		JInternalFrame in = new JInternalFrame("Debug", true, true, true, true);

		// create a internal frame
		JInternalFrame in1 = new JInternalFrame("Registers", true, true, true, true);

		// create a Button
		JButton b = new JButton("button");
		JButton b1 = new JButton("button1");

		// create a label to display text
		l = new JLabel("This is a JInternal Frame no 1 ");
		l1 = new JLabel("This is a JInternal Frame no 2 ");

		// create a panel
		JPanel p = new JPanel();
		JPanel p1 = new JPanel();

		// add label and button to panel
		p.add(l);
		p.add(b);
		p1.add(l1);
		p1.add(b1);

		// set visibility internal frame
		in.setVisible(true);
		in1.setVisible(true);

		// add panel to internal frame
		in.add(p);
		in1.add(p1);

		// add internal frame to frame
		f.add(in);
		f.add(in1);

		// set the size of frame
		f.setSize(300, 300);

		//f.show();
		f.setVisible(true);
	}
}
