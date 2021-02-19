//Original Code by Dylan Speiser
//https://github.com/DylanSpeiser

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;

public class EaterEmulator extends JFrame implements ActionListener {
	//Swing Things
	JPanel p = new JPanel();
	JPanel header = new JPanel();
	JFileChooser fc = new JFileChooser();
	JButton ROMopenButton = new JButton("Open ROM File");
	JButton RAMopenButton = new JButton("Open RAM File");
	
	public DisplayPanel GraphicsPanel = new DisplayPanel();
	
	public Timer clock;
	public static boolean haltFlag = true;
	
	//Emulator Things
	public static RAM ram = new RAM();
	public static ROM rom = new ROM();
	public static LCD lcd = new LCD();
	public static VIA via = new VIA();
	public static Bus bus = new Bus();
	public static CPU cpu = new CPU();
	
	public EaterEmulator() {
		//Swing Stuff:
		this.setSize(1920,1080);
		clock = new Timer(1,this);
		clock.start();
		
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
	        cpu.reset();
		} else if (e.getSource().equals(RAMopenButton)) {
			fc.setSelectedFile(new File(""));
			int returnVal = fc.showOpenDialog(this);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            ram.setRAMArray(ROMLoader.readROM(fc.getSelectedFile()));
	        }
	        GraphicsPanel.requestFocus();
	        cpu.reset();
		} else if (e.getSource().equals(clock)) {
			if (!haltFlag)
				cpu.clock();
		}
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		EaterEmulator emu = new EaterEmulator();
	}
}
