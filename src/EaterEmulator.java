//Original Code by Dylan Speiser
//https://github.com/DylanSpeiser

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;

public class EaterEmulator extends JFrame implements ActionListener {
	public static String versionString = "2.11";
	public static boolean debug = false;

	public static boolean verbose = false;
	public static boolean realisticKeyboard = false;
	public static int windowWidth = 1920, windowHeight = 1080;

	//Swing Things
	JPanel p = new JPanel();
	JPanel header = new JPanel();
	public static FileDialog fc = new java.awt.FileDialog((java.awt.Frame) null);
	public static JButton ROMopenButton = new JButton("Open ROM File");
	public static JButton RAMopenButton = new JButton("Open RAM File");

	public static JButton ShowLCDButton = new JButton("Show LCD");
	public static JButton ShowGPUButton = new JButton("Show GPU");
	public static JButton ShowSerialButton = new JButton("Show Serial");
	public static JButton ResetButton = new JButton("Reset");

	public static JButton optionsButton = new JButton("Options");
	public static JButton keyboardButton= new JButton("Keyboard Mode");

	public static JButton disassemblyButton = new JButton("Disassemble");

	//Clock Stuff
	public static Thread clockThread;
	public static boolean clockState = false;
	public static int clocks = 0;
	public static boolean haltFlag = true;
	public static boolean slowerClock = false;
	public static boolean running = false;
	public static boolean keyboardMode = false;			// False = controls (default), True = keyboard
	public static boolean carriageReturn = false;		// False = \r or 0x0D, True = \n or 0x0A
	
	//Emulator Things
	public static EaterEmulator emu;
	public static RAM ram = new RAM();
	public static ROM rom = new ROM();
	public static LCD lcd = new LCD();
	public static VIA via = new VIA();
	public static ACIA acia = new ACIA();
	public static CPU cpu = new CPU();
	public static GPU gpu = new GPU(ram,false);
	public static SerialInterface serial = new SerialInterface(false);
	public static DisplayPanel GraphicsPanel = new DisplayPanel();
	public static OptionsPane options = new OptionsPane();
	public static DisassemblyOutput disOutput = new DisassemblyOutput();
	
	public EaterEmulator() {
		//Swing Stuff:
		System.setProperty("sun.java2d.opengl", "true");
		this.setSize(windowWidth, windowHeight);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
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
		
		ShowSerialButton.setVisible(true);
		ShowSerialButton.addActionListener(this);
		ShowSerialButton.setBounds(getWidth()-450, 15, 125, 25);
		ShowSerialButton.setBackground(Color.white);
		GraphicsPanel.add(ShowSerialButton);
		
		ResetButton.setVisible(true);
		ResetButton.addActionListener(this);
		ResetButton.setBounds(getWidth()-450, 45, 125, 25);
		ResetButton.setBackground(Color.white);
		GraphicsPanel.add(ResetButton);

		//Options Button
		optionsButton.setVisible(true);
		optionsButton.addActionListener(this);
		optionsButton.setBounds(getWidth()-600, 15, 125, 25);
		optionsButton.setBackground(Color.white);
		GraphicsPanel.add(optionsButton);

		//Keyboard Mode Button
		keyboardButton.setVisible(true);
		keyboardButton.addActionListener(this);
		keyboardButton.setBounds(getWidth()-600, 45, 125, 25);
		keyboardButton.setBackground(Color.white);
		GraphicsPanel.add(keyboardButton);

		//Disassembly Button
		disassemblyButton.setVisible(true);
		disassemblyButton.addActionListener(this);
		disassemblyButton.setBounds(getWidth()-750, 15, 125, 25);
		disassemblyButton.setBackground(Color.white);
		GraphicsPanel.add(disassemblyButton);
		
		//file chooser
		fc.setDirectory(options.data.defaultFileChooserDirectory);
		fc.setVisible(false);

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
		GraphicsPanel.frameTimer.addActionListener(this);
		options.setVisible(false);

		this.setTitle("65c02 Emulator");
		this.setContentPane(GraphicsPanel);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		options.updateSwingComponents();
        options.applySwingValues();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(ROMopenButton)) {
			fc.setFile("");
			fc.setMode(FileDialog.LOAD);
	        fc.setVisible(true);

			if (fc.getFile() != null)
	        	rom.setROMArray(ROMLoader.readROM(new File(fc.getDirectory()+fc.getFile())));
	        
	        GraphicsPanel.requestFocus();
	        GraphicsPanel.romPageString = EaterEmulator.rom.ROMString.substring(GraphicsPanel.romPage*960,(GraphicsPanel.romPage+1)*960);
	        cpu.reset();
		} else if (e.getSource().equals(RAMopenButton)) {
			fc.setFile("");
			fc.setMode(FileDialog.LOAD);
	        fc.setVisible(true);

			if (fc.getFile() != null)
	        	ram.setRAMArray(ROMLoader.readROM(new File(fc.getDirectory()+fc.getFile())));

	        GraphicsPanel.requestFocus();
	        GraphicsPanel.ramPageString = EaterEmulator.ram.RAMString.substring(GraphicsPanel.ramPage*960,(GraphicsPanel.ramPage+1)*960);
			cpu.reset();
		} else if (e.getSource().equals(ShowLCDButton)) {
			lcd.setVisible(!lcd.isVisible());
		} else if (e.getSource().equals(ShowGPUButton)) {
			gpu.setVisible(!gpu.isVisible());
		} else if (e.getSource().equals(ShowSerialButton)) {
			serial.setVisible(!serial.isVisible());
		} else if (e.getSource().equals(ResetButton)) {
			reset();
		} else if (e.getSource().equals(optionsButton)) {
			options.setVisible(!options.isVisible());
		} else if (e.getSource().equals(disassemblyButton)) {
			int startAddress = -1, endAddress = -1;
			while (true) {
				try {
					String startHex = JOptionPane.showInputDialog(null, "Enter starting hex address (max 2 bytes):", "Hex Input", JOptionPane.QUESTION_MESSAGE);
					if (startHex == null) break; // canceled

					startAddress = Integer.parseInt(startHex.trim(), 16);
					if (startAddress > 0xffff) throw new NumberFormatException("Out of range");

					String endHex = JOptionPane.showInputDialog(null, "Enter final hex address (optional, defaults to FFFF):", "Hex Input", JOptionPane.QUESTION_MESSAGE);

					endAddress = (endHex == null || endHex.isEmpty()) ? 0xffff : Integer.parseInt(endHex.trim(), 16);
					if (endAddress > 0xffff) throw new NumberFormatException("Out of range");


					break;
				} catch (NumberFormatException e_) {
					JOptionPane.showMessageDialog(null, "Invalid input! Please enter a valid hex address within two bytes.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			if (startAddress != -1 && endAddress != -1) {
				DisassemblyOutput.StartAddress = startAddress;
				DisassemblyOutput.EndAddress = endAddress;
				disOutput.setVisible(true);
			}
		} else if (e.getSource().equals(GraphicsPanel.frameTimer)) {
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

			if (!serial.isVisible()) {
				ShowSerialButton.setText("Show Serial");
			} else {
				ShowSerialButton.setText("Hide Serial");
			}
		} else if (e.getSource().equals(keyboardButton)) {
			keyboardMode = !keyboardMode;
		}

		
	}

	public static void reset() {
		cpu.reset();
		lcd.reset();
		via = new VIA();
		acia = new ACIA();
		ram = new RAM();
		gpu.setRAM(ram);
		GraphicsPanel.ramPageString = ram.RAMString.substring(GraphicsPanel.ramPage*960,(GraphicsPanel.ramPage+1)*960);

		if (debug)
			if (verbose) System.out.println("Size: "+GraphicsPanel.getWidth()+" x "+GraphicsPanel.getHeight());
	}
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				for (int i = 0; i < args.length; i++) {
					String s = args[i];
					if (s.equals("-verbose")) verbose = true;
					else if (s.equals("-windowWidth")) {
						try {
							windowWidth = Integer.parseInt(args[++i]);
						} catch (Exception e) {
							System.out.println("Window width not understood, defaulting to " + windowWidth);
						}
					}
					else if (s.equals("-windowHeight")) {
						try {
							windowHeight = Integer.parseInt(args[++i]);
						} catch (Exception e) {
							System.out.println("Window height not understood, defaulting to " + windowHeight);
						}
					}
					else if (s.equals("-f")) {
						try {
							rom.setROMArray(ROMLoader.readROM(new File(args[++i])));
				        
					        GraphicsPanel.requestFocus();
					        GraphicsPanel.romPageString = EaterEmulator.rom.ROMString.substring(GraphicsPanel.romPage*960,(GraphicsPanel.romPage+1)*960);
							cpu.reset();
						} catch (Exception e) {
							System.out.println("There was an error loading the ROM file specified with -f");
						}
					}
				}
				
				//printing done after all arguments incase verbose argument comes after the other arguments
				if (verbose) System.out.println("Running in verbose mode!");
				if (verbose) System.out.println("Resolution of " + windowWidth + "x" + windowHeight + ".");
				
				emu = new EaterEmulator();
            }
        });
	}
}
