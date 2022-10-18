//Original Code by Dylan Speiser
//https://github.com/DylanSpeiser

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;

public class EaterEmulator extends JFrame implements ActionListener {
	public static EaterEmulator emu;
	public static String versionString = "2.0";
	public static boolean debug = false;
	
	//Swing Things
	JPanel p = new JPanel();
	JPanel header = new JPanel();
	public static JFileChooser fc = new JFileChooser();
	public static JButton ROMopenButton = new JButton("Open ROM File");
	public static JButton RAMopenButton = new JButton("Open RAM File");

	public static JButton ShowLCDButton = new JButton("Hide LCD");
	public static JButton ShowGPUButton = new JButton("Show GPU");

	public static JButton optionsButton = new JButton("Options");
	
	//Clock Stuff
	public static Thread clockThread;
	public static boolean clockState = false;
	public static int clocks = 0;
	public static boolean haltFlag = true;
	public static boolean slowerClock = false;
	
	//Emulator Things
	public static RAM ram = new RAM();
	public static ROM rom = new ROM();
	public static LCD lcd = new LCD();
	public static VIA via = new VIA();
	public static Bus bus = new Bus();
	public static CPU cpu = new CPU();
	public static GPU gpu = new GPU(ram);

	public static DisplayPanel GraphicsPanel = new DisplayPanel();

	//Options
	public static OptionsPane options = new OptionsPane();

	public static boolean running = false;
	
	public EaterEmulator() {
		//Swing Stuff:
		System.setProperty("sun.java2d.opengl", "true");
		this.setSize(1920,1080);
		
		//Open .bin file button
		ROMopenButton.setVisible(true);
		ROMopenButton.addActionListener(this);
		ROMopenButton.setBounds(getWidth()-150, 15, 125, 25);
		ROMopenButton.setBackground(Color.white);
		GraphicsPanel.add(ROMopenButton);
		
		RAMopenButton.setVisible(true);
		RAMopenButton.addActionListener(this);
		RAMopenButton.setBounds(getWidth()-150, 45, 125, 25);
		RAMopenButton.setBackground(Color.white);
		GraphicsPanel.add(RAMopenButton);

		//Show Extra Windows buttons
		ShowLCDButton.setVisible(true);
		ShowLCDButton.addActionListener(this);
		ShowLCDButton.setBounds(getWidth()-300, 15, 125, 25);
		ShowLCDButton.setBackground(Color.white);
		GraphicsPanel.add(ShowLCDButton);
		
		ShowGPUButton.setVisible(true);
		ShowGPUButton.addActionListener(this);
		ShowGPUButton.setBounds(getWidth()-300, 45, 125, 25);
		ShowGPUButton.setBackground(Color.white);
		GraphicsPanel.add(ShowGPUButton);

		//Options Button
		optionsButton.setVisible(true);
		optionsButton.addActionListener(this);
		optionsButton.setBounds(getWidth()-450, 15, 125, 55);
		optionsButton.setBackground(Color.white);
		GraphicsPanel.add(optionsButton);
		
		//file chooser
		fc.setVisible(true);
		fc.setCurrentDirectory(new File(options.data.defaultFileChooserDirectory));

		//Clock thread setup
		clockThread = new Thread(() -> {
	        while (true) {
	        	if (EaterEmulator.clockState)
	        		cpu.clock();
	        	System.out.print("");
	        	if (slowerClock) {
		        	try {
						Thread.sleep(1);
					} catch (InterruptedException e) {e.printStackTrace();}
	        	}
	        }
	    });
		clockThread.start();
		
		//Final Setup
		GraphicsPanel.setVisible(true);
		GraphicsPanel.t.addActionListener(this);
		options.setVisible(false);

		this.setTitle("6502 Emulator");
		this.setContentPane(GraphicsPanel);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		options.updateSwingComponents();
        options.applySwingValues();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(ROMopenButton)) {
			fc.setSelectedFile(new File(""));
	        int returnVal = fc.showOpenDialog(this);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            rom.setROMArray(ROMLoader.readROM(fc.getSelectedFile()));
	        }
	        GraphicsPanel.requestFocus();
	        GraphicsPanel.romPageString = EaterEmulator.rom.ROMString.substring(GraphicsPanel.romPage*960,(GraphicsPanel.romPage+1)*960);
	        cpu.reset();
		} else if (e.getSource().equals(RAMopenButton)) {
			fc.setSelectedFile(new File(""));
			int returnVal = fc.showOpenDialog(this);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            ram.setRAMArray(ROMLoader.readROM(fc.getSelectedFile()));
	        }
	        GraphicsPanel.requestFocus();
	        GraphicsPanel.ramPageString = EaterEmulator.ram.RAMString.substring(GraphicsPanel.ramPage*960,(GraphicsPanel.ramPage+1)*960);
			cpu.reset();
		} else if (e.getSource().equals(ShowLCDButton)) {
			lcd.setVisible(!lcd.isVisible());
		} else if (e.getSource().equals(ShowGPUButton)) {
			gpu.setVisible(!gpu.isVisible());
		} else if (e.getSource().equals(optionsButton)) {
			options.setVisible(!options.isVisible());
		} else if (e.getSource().equals(GraphicsPanel.t)) {
			if (!gpu.isVisible()) {
				ShowGPUButton.setText("Show GPU");
			} else {
				ShowGPUButton.setText("Hide GPU");
			}

			if (!lcd.isVisible()) {
				ShowLCDButton.setText("Show LCD");
			} else {
				ShowLCDButton.setText("Hide LCD");
			}
		}

		
	}
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				emu = new EaterEmulator();
            }
        });
	}
}