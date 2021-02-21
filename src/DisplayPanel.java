import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;
import javax.swing.Timer;

public class DisplayPanel extends JPanel implements ActionListener, KeyListener {
	Timer t;
	int ramPage = 0;
	int romPage = 0;
	
	String ramPageString = "";
	String romPageString = "";
	
	public DisplayPanel() {
		super(null);
		
		t = new javax.swing.Timer(16, this);
		t.start();
		setBackground(Color.blue);
		setPreferredSize(new Dimension(1936, 966));
		
		romPageString = EaterEmulator.rom.ROMString.substring(romPage*960,(romPage+1)*960);
		ramPageString = EaterEmulator.ram.RAMString.substring(ramPage*960,(ramPage+1)*960);
		
		this.setFocusable(true);
	    this.requestFocus();
		this.addKeyListener(this);
	}
	
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
		g.setColor(Color.white);
		//g.drawString("Render Mode: paintComponent",5,15);
		
//		g.setColor(getBackground());
//		g.fillRect(0, 0, EaterEmulator.getWindows()[1].getWidth(), EaterEmulator.getWindows()[1].getHeight());
//      g.setColor(Color.white);
//      g.drawString("Render Mode: fillRect",5,15);
        
        //Title
        g.setFont(new Font("Calibri Bold", 50, 50));
        g.drawString("Ben Eater 6502 Emulator", 40, 50);
        
        //Version
        g.setFont(new Font("Courier New Bold",20,20));
        g.drawString("v"+EaterEmulator.versionString, 7, 1033);
        
        //Clocks
        g.drawString("Clocks: "+EaterEmulator.clocks, 40, 80);
        g.drawString("Speed: "+EaterEmulator.cpu.ClocksPerSecond+" Hz"+(EaterEmulator.slowerClock ? " (Slow)" : ""), 40, 110);
        
        //PAGE INDICATORS
        g.drawString("(K) <-- "+ROMLoader.byteToHexString((byte)(romPage+0x80))+" --> (L)", 1600, 950);
        g.drawString("(H) <-- "+ROMLoader.byteToHexString((byte)ramPage)+" --> (J)", 1200, 950);
        
        //ROM
        g.drawString("ROM", 1690, 130);
        drawString(g,romPageString, 1525, 150);
        
        //RAM
        g.drawString("RAM", 1280, 130);
        drawString(g,ramPageString, 1125, 150);
	
        //CPU
        g.drawString("CPU Registers:",50,140);
        g.drawString("A: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.cpu.a)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.cpu.a)+")", 35, 170);
        g.drawString("X: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.cpu.x)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.cpu.x)+")", 35, 200);
        g.drawString("Y: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.cpu.y)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.cpu.y)+")", 35, 230);
        g.drawString("Stack Pointer: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.cpu.stackPointer)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.cpu.stackPointer)+")", 35, 260);
        g.drawString("Program Counter: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt(EaterEmulator.cpu.programCounter)), 16)+" ("+ROMLoader.padStringWithZeroes(Integer.toHexString(Short.toUnsignedInt(EaterEmulator.cpu.programCounter)),4)+")", 35, 290);
        g.drawString("Flags: ", 35, 320);
        
        g.drawString("Absolute Address: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt(EaterEmulator.cpu.addressAbsolute)), 16)+" ("+ROMLoader.byteToHexString((byte)(EaterEmulator.cpu.addressAbsolute/0xFF))+ROMLoader.byteToHexString((byte)EaterEmulator.cpu.addressAbsolute)+")", 35, 350);
        g.drawString("Relative Address: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt(EaterEmulator.cpu.addressRelative)), 16)+" ("+ROMLoader.byteToHexString((byte)(EaterEmulator.cpu.addressRelative/0xFF))+ROMLoader.byteToHexString((byte)EaterEmulator.cpu.addressRelative)+")", 35, 380);
        g.drawString("Opcode: "+EaterEmulator.cpu.lookup[Byte.toUnsignedInt(EaterEmulator.cpu.opcode)]+" ("+ROMLoader.byteToHexString(EaterEmulator.cpu.opcode)+")", 35, 410);
        g.drawString("Cycles: "+EaterEmulator.cpu.cycles, 35, 440);
        
        int counter = 0;
        String flagsString = "CZIDBUVN";
        for (char c : ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.cpu.flags)),8).toCharArray()) {
        	g.setColor((c == '1') ? Color.green : Color.red);
        	g.drawString(String.valueOf(flagsString.charAt(counter)), 120+15*counter, 320);
        	counter++;
        }
        
        g.setColor(Color.white);
        //VIA
        g.drawString("VIA Registers:",50,490);
        g.drawString("PORT A: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.via.PORTA)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.via.PORTA)+")", 35, 520);
        g.drawString("PORT B: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.via.PORTB)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.via.PORTB)+")", 35, 550);
        g.drawString("DDR  A: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.via.DDRA)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.via.DDRA)+")", 35, 580);
        g.drawString("DDR  B: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.via.DDRB)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.via.DDRB)+")", 35, 610);
        
        //Controls
        g.drawString("Controls:", 50, 750);
        g.drawString("C - Toggle Clock", 35, 780);
        g.drawString("Space - Pulse Clock", 35, 810);
        g.drawString("R - Reset", 35, 840);
        g.drawString("S - Toggle Slower Clock", 35, 870);
	}
	
	public static void drawString(Graphics g, String text, int x, int y) {
	    for (String line : text.split("\n"))
	        g.drawString(line, x, y += g.getFontMetrics().getHeight());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(t)) {
			ramPageString = EaterEmulator.ram.RAMString.substring(ramPage*960,(ramPage+1)*960);
			this.repaint();
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		switch (arg0.getKeyChar()) {
			case 'l':
				if (romPage < 0x80) {
					romPage+=1;
					romPageString = EaterEmulator.rom.ROMString.substring(romPage*960,(romPage+1)*960);
				}
				break;
			case 'k':
				if (romPage > 0) {
					romPage-=1;
					romPageString = EaterEmulator.rom.ROMString.substring(romPage*960,(romPage+1)*960);
				}
				break;
			case 'j':
				if (ramPage < 0x80) {
					ramPage+=1;
					ramPageString = EaterEmulator.ram.RAMString.substring(ramPage*960,(ramPage+1)*960);
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
				EaterEmulator.ram = new RAM();
				ramPageString = EaterEmulator.ram.RAMString.substring(ramPage*960,(ramPage+1)*960);
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
		}
	}
}
