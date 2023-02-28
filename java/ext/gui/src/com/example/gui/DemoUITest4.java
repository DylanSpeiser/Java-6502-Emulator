package com.example.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.juse.emulator.interfaces.AddressMap;
import com.juse.emulator.interfaces.BusListener;
import com.juse.emulator.interfaces.Clock;
import com.juse.emulator.interfaces.Emulator;
import com.juse.emulator.interfaces.Telemetry;
import com.juse.emulator.interfaces.TelemetryInfo;
import com.juse.emulator.interfaces.ui.EmulatorDisplay;
import com.juse.emulator.interfaces.ui.EmulatorFrame;
import com.juse.emulator.util.process.ProcessUtil;


public class DemoUITest4 extends JFrame implements EmulatorFrame, BusListener, EmulatorDisplay, KeyListener, MouseWheelListener, ActionListener
{
	public static void main(String[] args)
	{
		JFrame f = new DemoUITest4("Simple Docking Test",0,0,0,0);
		f.setSize(1920, 1080);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

	private Emulator emulator;
	private JMenuBar menuBar;
	

	public DemoUITest4()
	{
		this(null,0,0,0,0);
	}
	
	public DemoUITest4(String emulatorTitle, int initialX, int initialY, int initialWidth, int initialHeight)
	{
	    super();
	    if(emulatorTitle!=null)
	    	this.setTitle(emulatorTitle);
	    else
	    	this.setTitle("System Emulator");
	    this.setSize(new Dimension(initialWidth, initialHeight));
		if(initialX == -1 && initialY == -1)
		{
			this.setLocationRelativeTo(null);
		}
		else
			this.setLocation(initialX, initialY);
		
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);	 
		
		menuBar = configureMenu();								
		this.setJMenuBar(menuBar);
		
		//setLayout(new BorderLayout());
	    
	    //setContentPane(createContentPane());
	}

	protected JMenuBar configureMenu()
	{
		// Create Menu Font
		Font menuFont = new Font("Arial", Font.BOLD, 16);
		// Create the menu bar.
		JMenuBar menuBar = new JMenuBar();

		//Build the File menu.
		JMenu menuFile = createFileMenu(menuFont);
		JMenu menuView = createViewMenu(menuFont);
		JMenu menuDebug = createDebugMenu(menuFont);
		JMenu menuClock = createClockMenu(menuFont);
		
		menuBar.add(menuFile);
		menuBar.add(menuView);
		menuBar.add(menuClock);
		menuBar.add(menuDebug);
		
		
		return menuBar;
	}

	protected JMenu createFileMenu(Font menuFont)
	{
		JMenu menu = new JMenu("File");
		menu.setFont(menuFont);
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("File operations menu");
		
		//a group of JMenuItems
		JMenuItem menuItem = new JMenuItem("Open",KeyEvent.VK_O);
		menuItem.setFont(menuFont);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Open configuration file");
		menuItem.addActionListener(fileOpenHandler);
		menu.add(menuItem);		
		
		
		JMenuItem menuExit = new JMenuItem("Exit",KeyEvent.VK_T);
		menuExit.setFont(menuFont);
		menuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
		menuExit.getAccessibleContext().setAccessibleDescription("Exit Application");
		menuExit.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dispatchEvent(new WindowEvent(DemoUITest4.this, WindowEvent.WINDOW_CLOSING));
			}
		});
		menu.add(menuExit);
		return menu;
	}

	protected ActionListener fileOpenHandler = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			final JFileChooser fc = new JFileChooser();

			FileNameExtensionFilter filter = new FileNameExtensionFilter("JUSE Configurations", "system");
			fc.setFileFilter(filter);

			fc.addChoosableFileFilter(new FileFilter()
			{
				public String getDescription()
				{
					return "JUSE Configurations (*.system)";
				}

				public boolean accept(File f)
				{
					if (f.isDirectory())
					{
						return true;
					}
					else
					{
						return f.getName().toLowerCase().endsWith(".system");
					}
				}
			});
			int returnVal = fc.showOpenDialog(DemoUITest4.this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File f = fc.getSelectedFile();
				// This is where a real application would open the file.
				try
				{
					System.out.println(f.getCanonicalPath());
					
					List<String> arguments = ProcessUtil.getProcessArguments();
					ProcessUtil.replaceProcessArgumentValue(arguments, "--config", f.getCanonicalPath());
					
					ProcessUtil.relauchWithExt((Class)ProcessUtil.getProcessLaunchClass(),
							                   null, 
							                   arguments,
							                   false);
					
					
					
					dispatchEvent(new WindowEvent(DemoUITest4.this, WindowEvent.WINDOW_CLOSING));
				}
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else
			{
				// log.append("Open command cancelled by user." + newline);
			}
		}
	};

	protected JMenu createClockMenu(Font menuFont)
	{
		JMenu menuView = new JMenu("Clock");
		menuView.setFont(menuFont);
		menuView.setMnemonic(KeyEvent.VK_A);
		menuView.getAccessibleContext().setAccessibleDescription("Clock");
		
		
		JMenuItem menuRegisters = createMenuItem(menuFont, "Run", "", KeyEvent.VK_C, null);
		menuView.add(menuRegisters);
		JMenuItem menuInstructions = createMenuItem(menuFont, "Pulse", "", KeyEvent.VK_L, null);
		menuView.add(menuInstructions);
		JMenuItem menuMemory = createMenuItem(menuFont, "Slow Mode", "", KeyEvent.VK_Y, null);
		menuView.add(menuMemory);
		
		return menuView;
	}	
	
	protected JMenu createViewMenu(Font menuFont)
	{
		JMenu menuView = new JMenu("View");
		menuView.setFont(menuFont);
		menuView.setMnemonic(KeyEvent.VK_A);
		menuView.getAccessibleContext().setAccessibleDescription("View");
		
		
		JMenuItem menuRegisters = createMenuItem(menuFont, "Registers View", "", KeyEvent.VK_R, null);
		menuView.add(menuRegisters);
		JMenuItem menuInstructions = createMenuItem(menuFont, "Instructions View", "", KeyEvent.VK_I, null);
		menuView.add(menuInstructions);
		JMenuItem menuMemory = createMenuItem(menuFont, "Memory View", "", KeyEvent.VK_M, null);
		menuView.add(menuMemory);
		
		return menuView;
	}

	
	protected JMenu createDebugMenu(Font menuFont)
	{
		JMenu menuView = new JMenu("Debug");
		menuView.setFont(menuFont);
		menuView.setMnemonic(KeyEvent.VK_A);
		menuView.getAccessibleContext().setAccessibleDescription("Debug");
		
		
		JMenuItem menuABP = createMenuItem(menuFont, "Add Break Point", "", KeyEvent.VK_R, null);
		menuView.add(menuABP);
		JMenuItem menuRBP = createMenuItem(menuFont, "Remove Break Point", "", KeyEvent.VK_I, null);
		menuView.add(menuRBP);
		JMenuItem menuEditMemory = createMenuItem(menuFont, "Edit Memory", "", KeyEvent.VK_M, null);
		menuView.add(menuEditMemory);
		
		return menuView;
	}	
	
	protected JMenuItem createMenuItem(Font menuFont, String name, String desc, ActionListener actionListener)
	{
		return createMenuItem(menuFont, name, desc, -1, actionListener);
	}	
	
	protected JMenuItem createMenuItem(Font menuFont, String name, String desc, int ke, ActionListener actionListener)
	{
		JMenuItem menuItem;
		
		if(ke!=-1)
			menuItem = new JMenuItem(name,ke);
		else
			menuItem = new JMenuItem(name);
		
		menuItem.setFont(menuFont);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(ke, ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(desc);
		if(actionListener == null)
			actionListener = new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					System.out.println("Clicked:" + ((JMenuItem)e.getSource()).getText());
				}
			};
		
		menuItem.addActionListener(actionListener);
		return menuItem;
	}
	
	
	private JPanel createContentPane()
	{
		JPanel p = new JPanel(new BorderLayout(2, 2));
		p.setBackground(Color.white);
		
		
		
		
		p.add(buildDockingPort(Color.BLUE, "CPU"), BorderLayout.WEST);
		p.add(buildDockingPort(Color.GREEN, "RAM"), BorderLayout.EAST);
		p.add(buildDockingPort(Color.BLACK, "Controls"), BorderLayout.NORTH);
		p.add(buildDockingPort(Color.RED, "Instructions"), BorderLayout.CENTER);
		//p.add(createDockingPort(), BorderLayout.CENTER);
		pack();
		
		return p;
	}

	private JPanel buildDockingPort(Color color, final String desc)
	{
		// create the DockingPort
		//DefaultDockingPort port = createDockingPort();

		final Color myColor = color;
		// create and register the Dockable panel
		JPanel p = new JPanel();
		
		if("Controls".equals(desc))
		{			
			Border blackline = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder());
			p.setBorder(blackline);
			p.setBackground(new Color(246,246,246));
			p.setLayout(new BorderLayout());
		}
		else
		{
			Border blackline = BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),desc);		
			((javax.swing.border.TitledBorder)blackline).setTitleFont(new Font("Arial", Font.BOLD, 16));
			p.setBorder(blackline);
			p.setBackground(new Color(246,246,246));
			p.setLayout(new BorderLayout());
		}
		
		
		JPanel pp; 
		
		if("Devices".equals(desc))
		{
			pp = createDevicesSubPanel(desc, myColor);
			pp.setPreferredSize(new Dimension(300,150));
		}
		else if("Instructions".equals(desc)) 
		{
			pp = createInstructionsSubPanel(desc, myColor);			
			pp.setPreferredSize(new Dimension(500,768));
		}
		else if("RAM".equals(desc)) 
		{
			pp = createRAMSubPanel(desc, myColor);			
			pp.setPreferredSize(new Dimension(350,768));
		}
		else if("CPU".equals(desc)) 
		{
			pp = createRegistersSubPanel(desc, myColor);			
			pp.setPreferredSize(new Dimension(350,768));
		}
		else if("Controls".equals(desc)) 
		{
			pp = createControlsSubPanel("", myColor);			
			pp.setPreferredSize(new Dimension(400,30));
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
		JPanel pp = new ExRAMImpl(this.emulator);
		//JPanel pp = new ExRAMGridImpl(this.emulator);
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

	protected JPanel createControlsSubPanel(final String desc, final Color myColor)
	{
		JPanel pp = new JPanel();
		pp.setLayout(new GridLayout(1,0));
		//JToolBar toolBar = new JToolBar("Still draggable");
		JPanel toolBar = pp ;
		toolBar.setAlignmentX(LEFT_ALIGNMENT);
		//toolBar.setFloatable(false);
		toolBar.setPreferredSize(new Dimension(450, 25));
		final JButton runButton = new JButton("Run");
		//runButton.setPreferredSize(new Dimension(100,75));
		runButton.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Clock c = emulator.getClock();
				
				if(!c.isEnabled())
					runButton.setText("Stop");
				else
					runButton.setText("Run");
				
				c.setEnabled(!c.isEnabled());
			}	
			
		});
		toolBar.add(runButton);
		toolBar.add(new JButton("Tick"));		
		toolBar.add(new JButton("Clock+"));
		toolBar.add(new JButton("Clock-"));
		toolBar.add(new JButton("Slow Mode"));
		
		toolBar.add(new JButton("Reset"));
		toolBar.add(new JButton("Reset CPU"));
		toolBar.add(new JButton("Debug"));
		toolBar.add(new JButton("Add Break"));
		toolBar.add(new JButton("Build"));
		toolBar.add(new JButton("Interrupts"));
        pp.setAlignmentX(LEFT_ALIGNMENT);
        pp.setPreferredSize(new Dimension(450, 25));
        //pp.add(toolBar, BorderLayout.NORTH);
        //pp.add(toolBar);
		
		return pp;
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
		redraw();
	}

	@Override
	public void showFrame(boolean bVisible)
	{ 
		this.setVisible(bVisible);		
		redraw();
	}

	@Override
	public void initFrame(Emulator emu)
	{
		this.emulator = emu;
		((AddressMap)emu.getBus()).addBusListener((BusListener)this);		
		setContentPane(createContentPane());
		pack();
		SwingUtilities.invokeLater(new Runnable() 
		{

			@Override
			public void run()
			{
				redraw();
			}			
		});
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
