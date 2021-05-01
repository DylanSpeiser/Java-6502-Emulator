//Original Code by Dylan Speiser
//https://github.com/DylanSpeiser

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;

public class EaterEmulator extends JFrame implements ActionListener {
	public static EaterEmulator emu;
	public static String versionString = "1.4";
	
	//Swing Things
	JPanel p = new JPanel();
	JPanel header = new JPanel();
	JFileChooser fc = new JFileChooser();
	public static JButton ROMopenButton = new JButton("Open ROM File");
	public static JButton RAMopenButton = new JButton("Open RAM File");
	
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
	
	public DisplayPanel GraphicsPanel = new DisplayPanel();
	
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
		
		//file chooser
		fc.setVisible(true);
		fc.setCurrentDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator")+ "Downloads"));

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
		this.setTitle("6502 Emulator");
		this.setContentPane(GraphicsPanel);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
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
		}
	}
	
	public static void main(String[] args) {
		emu = new EaterEmulator();
	}
}
