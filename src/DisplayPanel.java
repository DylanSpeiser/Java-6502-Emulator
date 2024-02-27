import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.io.IOException;
import java.util.ArrayList;

public class DisplayPanel extends JPanel implements ActionListener, KeyListener {
	Timer frameTimer = new javax.swing.Timer(16, this);;
	Timer clocksPerSecondCheckTimer = new Timer(150,this);
	int ramPage = 0;
	int romPage = 0;
	
	int rightAlignHelper = Math.max(getWidth(), 1334);

	public Font courierNewBold;
	
	String ramPageString = "";
	String romPageString = "";

	public static Color bgColor = Color.blue;
	public static Color fgColor = Color.white;
	
	public static ArrayList<Byte> keys = new ArrayList<Byte>();
	
	boolean debug = false;
	
	public DisplayPanel() {
		super(null);
		
		clocksPerSecondCheckTimer.start();
		frameTimer.start();
		setBackground(bgColor);
		setPreferredSize(new Dimension(1936, 966));

		try {
			courierNewBold = Font.createFont(Font.TRUETYPE_FONT,this.getClass().getClassLoader().getResourceAsStream("courbd.ttf")).deriveFont(20f);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(courierNewBold);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
			if (EaterEmulator.verbose) System.out.println("Error loading Courier Font!");
		}
		
		romPageString = EaterEmulator.rom.ROMString.substring(romPage*960,(romPage+1)*960);
		ramPageString = EaterEmulator.ram.RAMString.substring(ramPage*960,(ramPage+1)*960);
		
		this.setFocusable(true);
	    this.requestFocus();
		this.addKeyListener(this);
	}
	
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
		g.setColor(fgColor);
		//g.drawString("Render Mode: paintComponent",5,15);
		
//		g.setColor(getBackground());
//		g.fillRect(0, 0, EaterEmulator.getWindows()[1].getWidth(), EaterEmulator.getWindows()[1].getHeight());
//      g.setColor(Color.white);
//      g.drawString("Render Mode: fillRect",5,15);
		
		rightAlignHelper = Math.max(getWidth(), 1334);
		
        //Title
        g.setFont(new Font("Calibri Bold", 50, 50));
        g.drawString("BE6502 Emulator", 40, 50);
        
        //Version
        g.setFont(courierNewBold);
        g.drawString("v"+EaterEmulator.versionString, 7, 1033);
        
        //Clocks
        g.drawString("Clocks: "+EaterEmulator.clocks, 40, 80);
        g.drawString("Speed: "+EaterEmulator.cpu.ClocksPerSecond+" Hz"+(EaterEmulator.slowerClock ? " (Slow)" : ""), 40, 110);
        
        //PAGE INDICATORS
        g.drawString("(K) <-- "+ROMLoader.byteToHexString((byte)(romPage+0x80))+" --> (L)", rightAlignHelper-304, Math.max(getHeight()-91, 920));
        g.drawString("(H) <-- "+ROMLoader.byteToHexString((byte)ramPage)+" --> (J)", rightAlignHelper-704, Math.max(getHeight()-91, 920));
        
        //ROM
        g.drawString("ROM", rightAlignHelper-214, 130);
        drawString(g,romPageString, rightAlignHelper-379, 150);
        
        //Stack Pointer Underline
        if (ramPage == 1) {
        	g.setColor(new Color(0.7f,0f,0f));
        	g.fillRect(rightAlignHelper-708+36*(Byte.toUnsignedInt(EaterEmulator.cpu.stackPointer)%8), 156+23*((int)Byte.toUnsignedInt(EaterEmulator.cpu.stackPointer)/8), 25, 22);
        	g.setColor(fgColor);
        }
        
        //RAM
        g.drawString("RAM", rightAlignHelper-624, 130);
        drawString(g,ramPageString, rightAlignHelper-779, 150);
        
	
        //CPU
        g.drawString("CPU Registers:",50,140);
        g.drawString("A: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.cpu.a)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.cpu.a)+")", 35, 170);
        g.drawString("X: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.cpu.x)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.cpu.x)+")", 35, 200);
        g.drawString("Y: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.cpu.y)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.cpu.y)+")", 35, 230);
        g.drawString("Stack Pointer: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.cpu.stackPointer)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.cpu.stackPointer)+")", 35, 260);
        g.drawString("Program Counter: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt(EaterEmulator.cpu.programCounter)), 16)+" ("+ROMLoader.padStringWithZeroes(Integer.toHexString(Short.toUnsignedInt(EaterEmulator.cpu.programCounter)).toUpperCase(),4)+")", 35, 290);
        g.drawString("Flags:             ("+ROMLoader.byteToHexString(EaterEmulator.cpu.flags)+")", 35, 320);
        
        g.drawString("Absolute Address: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt(EaterEmulator.cpu.addressAbsolute)), 16)+" ("+ROMLoader.byteToHexString((byte)(EaterEmulator.cpu.addressAbsolute/0xFF))+ROMLoader.byteToHexString((byte)EaterEmulator.cpu.addressAbsolute)+")", 35, 350);
        g.drawString("Relative Address: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt(EaterEmulator.cpu.addressRelative)), 16)+" ("+ROMLoader.byteToHexString((byte)(EaterEmulator.cpu.addressRelative/0xFF))+ROMLoader.byteToHexString((byte)EaterEmulator.cpu.addressRelative)+")", 35, 380);
        g.drawString("Opcode: "+EaterEmulator.cpu.lookup[Byte.toUnsignedInt(EaterEmulator.cpu.opcode)]+" ("+ROMLoader.byteToHexString(EaterEmulator.cpu.opcode)+")", 35, 410);
        g.drawString("Cycles: "+EaterEmulator.cpu.cycles, 35, 440);
        
        int counter = 0;
        String flagsString = "NVUBDIZC";
        for (char c : ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.cpu.flags)),8).toCharArray()) {
        	g.setColor((c == '1') ? Color.green : Color.red);
        	g.drawString(String.valueOf(flagsString.charAt(counter)), 120+16*counter, 320);
        	counter++;
        }
        
        g.setColor(fgColor);
        //VIA
        g.drawString("VIA Registers:",50,490);
        g.drawString("PORT A: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.via.PORTA)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.via.PORTA)+")", 35, 520);
        g.drawString("PORT B: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.via.PORTB)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.via.PORTB)+")", 35, 550);
        g.drawString("DDR  A: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.via.DDRA)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.via.DDRA)+")", 35, 580);
        g.drawString("DDR  B: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.via.DDRB)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.via.DDRB)+")", 35, 610);
        g.drawString("   PCR: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.via.PCR)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.via.PCR)+")", 35, 640);
        g.drawString("   IFR: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.via.IFR)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.via.IFR)+")", 35, 670);
        g.drawString("   IER: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.via.IER)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.via.IER)+")", 35, 700);
        
		//ACIA
		g.drawString("ACIA Registers:",350,490);
		g.drawString("Data Register:    "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.acia.getDATA())), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.acia.getDATA())+")", 325, 520);
        g.drawString("Command Register: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.acia.getCOMMAND())), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.acia.getCOMMAND())+")", 325, 550);
        g.drawString("Status Register:  "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.acia.getSTATUS())), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.acia.getSTATUS())+")", 325, 580);
        g.drawString("Control Register: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.acia.getCONTROL())), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.acia.getCONTROL())+")", 325, 610);

        //Controls

		if (!EaterEmulator.keyboardMode) {
	        g.drawString("Controls:", 50, 750);
	        g.drawString("C - Toggle Clock", 35, 780);
	        g.drawString("Space - Pulse Clock", 35, 810);
	        g.drawString("R - Reset", 35, 840);
	        g.drawString("S - Toggle Slower Clock", 35, 870);
	        g.drawString("I - Trigger VIA CA1", 35, 900);
		} else {
			g.drawString("Keyboard Mode Controls:", 50, 750);
			g.drawString("Typing a key will write that key code to the memory location "+EaterEmulator.options.KeyboardLocationHexLabel.getText().substring(3), 35, 780);
			g.drawString(" and trigger an interrupt.", 35, 810);
		}
	}
	
	public static void drawString(Graphics g, String text, int x, int y) {
	    for (String line : text.split("\n"))
	        g.drawString(line, x, y += g.getFontMetrics().getHeight());
	}

	public void resetGraphics() {
		bgColor = EaterEmulator.options.data.bgColor;
		fgColor = EaterEmulator.options.data.fgColor;
		setBackground(bgColor);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(frameTimer)) {

			EaterEmulator.running = true;
			
			ramPageString = EaterEmulator.ram.RAMString.substring(ramPage*960,(ramPage+1)*960);
			EaterEmulator.ROMopenButton.setBounds(rightAlignHelper-150, 15, 125, 25);
			EaterEmulator.RAMopenButton.setBounds(rightAlignHelper-150, 45, 125, 25);
			EaterEmulator.ShowLCDButton.setBounds(rightAlignHelper-300, 15, 125, 25);
			EaterEmulator.ShowGPUButton.setBounds(rightAlignHelper-300, 45, 125, 25);
			EaterEmulator.ResetButton.setBounds(rightAlignHelper-450, 15, 125, 25);
			EaterEmulator.ShowSerialButton.setBounds(rightAlignHelper-450, 45, 125, 25);
			EaterEmulator.optionsButton.setBounds(rightAlignHelper-600, 15, 125, 25);
			EaterEmulator.keyboardButton.setBounds(rightAlignHelper-600, 45, 125, 25);
			this.repaint();

			if (!EaterEmulator.options.isVisible() && !EaterEmulator.serial.isVisible())
				this.requestFocus();
		} else if (e.getSource().equals(clocksPerSecondCheckTimer)) {
			EaterEmulator.cpu.timeDelta = System.nanoTime()-EaterEmulator.cpu.lastTime;
            EaterEmulator.cpu.lastTime = System.nanoTime();

            EaterEmulator.cpu.clockDelta = EaterEmulator.clocks - EaterEmulator.cpu.lastClocks;
            EaterEmulator.cpu.lastClocks = EaterEmulator.clocks;

            EaterEmulator.cpu.ClocksPerSecond = Math.round(EaterEmulator.cpu.clockDelta/((double)EaterEmulator.cpu.timeDelta/1000000000.0));
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (EaterEmulator.keyboardMode && EaterEmulator.realisticKeyboard) {
			keys.add(convertToPs2(arg0));
			if(EaterEmulator.verbose&&debug) System.out.println("Pressed key " + arg0.getKeyChar() + " " + convertToPs2(arg0) + " : " + (arg0.getKeyCode()));
			EaterEmulator.via.CA1();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if (EaterEmulator.keyboardMode && EaterEmulator.realisticKeyboard) {
			keys.add((byte) 0xf0);
			keys.add(convertToPs2(arg0));
			if(EaterEmulator.verbose&&debug) System.out.println("Pressed key " + arg0.getKeyChar() + " " + convertToPs2(arg0) + " : " + (arg0.getKeyCode()));
			EaterEmulator.via.CA1();
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		if (!EaterEmulator.keyboardMode) {
			//Control Keyboard Mode
			switch (arg0.getKeyChar()) {
				case 'l':
					if (romPage < 0x7f) {
						romPage+=1;
						romPageString = EaterEmulator.rom.ROMString.substring(romPage*960,(romPage+1)*960);
					} else {
						if (romPage > 0x7f) {
							romPage = 0x7f;
							romPageString = EaterEmulator.rom.ROMString.substring(romPage*960,(romPage+1)*960);
						}
					}
					break;
				case 'k':
					if (romPage > 0) {
						romPage-=1;
						romPageString = EaterEmulator.rom.ROMString.substring(romPage*960,(romPage+1)*960);
					}
					break;
				case 'j':
					if (ramPage < 0x7f) {
						ramPage+=1;
						ramPageString = EaterEmulator.ram.RAMString.substring(ramPage*960,(ramPage+1)*960);
						if (ramPage > 0x7f) {
							ramPage = 0x7f;
							ramPageString = EaterEmulator.ram.RAMString.substring(ramPage*960,(ramPage+1)*960);
						}
					}
					break;
				case 'h':
					if (ramPage > 0) {
						ramPage-=1;
						ramPageString = EaterEmulator.ram.RAMString.substring(ramPage*960,(ramPage+1)*960);
					}
					break;
				case 'r':
					EaterEmulator.cpu.reset();
					EaterEmulator.lcd.reset();
					EaterEmulator.via = new VIA();
					EaterEmulator.acia = new ACIA();
					EaterEmulator.ram = new RAM();
					EaterEmulator.gpu.setRAM(EaterEmulator.ram);
					ramPageString = EaterEmulator.ram.RAMString.substring(ramPage*960,(ramPage+1)*960);

					if (EaterEmulator.debug)
						if (EaterEmulator.verbose) System.out.println("Size: "+this.getWidth()+" x "+this.getHeight());
					break;
				case ' ':
					EaterEmulator.cpu.clock();
					break;
				case 'c':
					EaterEmulator.clockState = !EaterEmulator.clockState;
					break;
				case 's':
					EaterEmulator.slowerClock = !EaterEmulator.slowerClock;
					break;
				case 'i':
					EaterEmulator.via.CA1();
					break;
			}
		} else if (!EaterEmulator.realisticKeyboard) { //realistic keyboard uses key pressed and released so it can get the key code
			//Typing Keyboard Mode
			Bus.write((short)EaterEmulator.options.data.keyboardLocation, (byte)arg0.getKeyChar());
			EaterEmulator.via.CA1();
		}
	}

	
	//converts java keyCode to ps2 keyCode to be compatible
	//found https://techdocs.altium.com/display/FPGA/PS2+Keyboard+Scan+Codes
	private byte convertToPs2(KeyEvent keyEvent) {
		switch(keyEvent.getKeyCode()) {
		case KeyEvent.VK_ESCAPE:
			return 0x76;
		case KeyEvent.VK_F1:
			return 0x05;
		case KeyEvent.VK_F2:
			return 0x06;
		case KeyEvent.VK_F3:
			return 0x04;
		case KeyEvent.VK_F4:
			return 0x0C;
		case KeyEvent.VK_F5:
			return 0x03;
		case KeyEvent.VK_F6:
			return 0x0B;
		case KeyEvent.VK_F7:
			return (byte) 0x83;
		case KeyEvent.VK_F8:
			return 0x0A;
		case KeyEvent.VK_F9:
			return 0x01;
		case KeyEvent.VK_F10:
			return 0x09;
		case KeyEvent.VK_F11:
			return 0x78;
		case KeyEvent.VK_F12:
			return 0x07;
		case KeyEvent.VK_SCROLL_LOCK:
			return 0x7E;
		case KeyEvent.VK_DEAD_GRAVE:
			return 0x0E;
		case KeyEvent.VK_BACK_QUOTE:
			return 0x0E;
		case KeyEvent.VK_1:
			return 0x16;
		case KeyEvent.VK_2:
			return 0x1E;
		case KeyEvent.VK_3:
			return 0x26;
		case KeyEvent.VK_4:
			return 0x25;
		case KeyEvent.VK_5:
			return 0x2E;
		case KeyEvent.VK_6:
			return 0x36;
		case KeyEvent.VK_7:
			return 0x3D;
		case KeyEvent.VK_8:
			return 0x3E;
		case KeyEvent.VK_9:
			return 0x46;
		case KeyEvent.VK_0:
			return 0x45;
		case KeyEvent.VK_MINUS:
			if(keyEvent.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD) return 0x7B;
			else return 0x4E;
		case KeyEvent.VK_UNDERSCORE:
			return 0x4E;
		case KeyEvent.VK_EQUALS:
			return 0x55;
		case KeyEvent.VK_PLUS:
			if(keyEvent.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD) return 0x79;
			else return 0x55;
		case KeyEvent.VK_BACK_SPACE:
			return 0x66;
		case KeyEvent.VK_TAB:
			return 0x0D;
		case KeyEvent.VK_Q:
			return 0x15;
		case KeyEvent.VK_W:
			return 0x1D;
		case KeyEvent.VK_E:
			return 0x24;
		case KeyEvent.VK_R:
			return 0x2D;
		case KeyEvent.VK_T:
			return 0x2C;
		case KeyEvent.VK_Y:
			return 0x35;
		case KeyEvent.VK_U:
			return 0x3C;
		case KeyEvent.VK_I:
			return 0x43;
		case KeyEvent.VK_O:
			return 0x44;
		case KeyEvent.VK_P:
			return 0x4D;
		case KeyEvent.VK_OPEN_BRACKET:
			return 0x54;
		case KeyEvent.VK_BRACELEFT:
			return 0x54;
		case KeyEvent.VK_CLOSE_BRACKET:
			return 0x5B;
		case KeyEvent.VK_BRACERIGHT:
			return 0x5B;
		case KeyEvent.VK_BACK_SLASH:
			return 0x5D;
		case KeyEvent.VK_CAPS_LOCK:
			return 0x58;
		case KeyEvent.VK_A:
			return 0x1C;
		case KeyEvent.VK_S:
			return 0x1B;
		case KeyEvent.VK_D:
			return 0x23;
		case KeyEvent.VK_F:
			return 0x2B;
		case KeyEvent.VK_G:
			return 0x34;
		case KeyEvent.VK_H:
			return 0x33;
		case KeyEvent.VK_J:
			return 0x3B;
		case KeyEvent.VK_K:
			return 0x32;
		case KeyEvent.VK_L:
			return 0x4B;
		case KeyEvent.VK_SEMICOLON:
			return 0x4C;
		case KeyEvent.VK_COLON:
			return 0x4C;
		case KeyEvent.VK_QUOTE:
			return 0x52;
		case KeyEvent.VK_QUOTEDBL:
			return 0x52;
		case KeyEvent.VK_ENTER:
			if(keyEvent.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD) return 0x5A;
			else return 0x5A;
		case KeyEvent.VK_SHIFT:
			if(keyEvent.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) return 0x59;
			else return 0x12;
		case KeyEvent.VK_Z:
			return 0x1A;
		case KeyEvent.VK_X:
			return 0x22;
		case KeyEvent.VK_C:
			return 0x21;
		case KeyEvent.VK_V:
			return 0x2A;
		case KeyEvent.VK_B:
			return 0x32;
		case KeyEvent.VK_N:
			return 0x31;
		case KeyEvent.VK_M:
			return 0x3A;
		case KeyEvent.VK_COMMA:
			return 0x41;
		case KeyEvent.VK_LESS:
			return 0x41;
		case KeyEvent.VK_PERIOD:
			if(keyEvent.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD) return 0x71;
			else return 0x49;
		case KeyEvent.VK_GREATER:
			return 0x49;
		case KeyEvent.VK_SLASH:
			return 0x4A;
		case KeyEvent.VK_CONTROL:
			if(keyEvent.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) return 0x14;
			else return 0x14;
		case KeyEvent.VK_WINDOWS:
			if(keyEvent.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) return 0x27;
			else return 0x1F;
		case KeyEvent.VK_ALT:
			if(keyEvent.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) return 0x11;
			else return 0x11;
		case KeyEvent.VK_SPACE:
			return 0x29;
		case KeyEvent.VK_CONTEXT_MENU:
			return 0x2F;
		case KeyEvent.VK_INSERT:
			return 0x70;
		case KeyEvent.VK_HOME:
			return 0x6C;
		case KeyEvent.VK_PAGE_UP:
			return 0x7D;
		case KeyEvent.VK_DELETE:
			return 0x71;
		case KeyEvent.VK_END:
			return 0x69;
		case KeyEvent.VK_PAGE_DOWN:
			return 0x7A;
		case KeyEvent.VK_UP:
			return 0x75;
		case KeyEvent.VK_KP_UP:
			return 0x75;
		case KeyEvent.VK_LEFT:
			return 0x6B;
		case KeyEvent.VK_KP_LEFT:
			return 0x6B;
		case KeyEvent.VK_DOWN:
			return 0x72;
		case KeyEvent.VK_KP_DOWN:
			return 0x72;
		case KeyEvent.VK_RIGHT:
			return 0x74;
		case KeyEvent.VK_KP_RIGHT:
			return 0x74;
		case KeyEvent.VK_NUM_LOCK:
			return 0x77;
		case KeyEvent.VK_ASTERISK:
			if(keyEvent.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD) return 0x7C;
			else return 0x3E;
		case KeyEvent.VK_NUMPAD1:
			return 0x69;
		case KeyEvent.VK_NUMPAD2:
			return 0x72;
		case KeyEvent.VK_NUMPAD3:
			return 0x7A;
		case KeyEvent.VK_NUMPAD4:
			return 0x6B;
		case KeyEvent.VK_NUMPAD5:
			return 0x73;
		case KeyEvent.VK_NUMPAD6:
			return 0x74;
		case KeyEvent.VK_NUMPAD7:
			return 0x6C;
		case KeyEvent.VK_NUMPAD8:
			return 0x75;
		case KeyEvent.VK_NUMPAD9:
			return 0x7D;
		case KeyEvent.VK_NUMPAD0:
			return 0x70;
		}
		return 0;
	}
}
