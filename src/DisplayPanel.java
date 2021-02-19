import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;
import javax.swing.Timer;

public class DisplayPanel extends JPanel implements ActionListener, KeyListener, MouseMotionListener {
	Timer t;
	Point mousePos = new Point(0,0);
	int ramPage = 0;
	int romPage = 0;
	
	public DisplayPanel() {
		super(null);
		
		t = new javax.swing.Timer(100, this);
		t.start();
		setBackground(Color.blue);
		setPreferredSize(new Dimension(1936, 966));
		
		this.setFocusable(true);
	    this.requestFocus();
		this.addKeyListener(this);
		this.addMouseMotionListener(this);
	}
	
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.setColor(Color.white);
        
        //Title
        g.setFont(new Font("Calibri Bold", 50, 50));
        g.drawString("Ben Eater 6502 Emulator", 40, 50);
        
        //Mouse Position
//        g.setFont(new Font("Arial",10,10));
//        g.drawString(mousePos.toString(), 10, 10);
        
        //PAGE INDICATORS
        g.setFont(new Font("Courier New Bold",20,20));
        g.drawString("(K) <-- "+ROMLoader.byteToHexString((byte)(romPage+0x80))+" --> (L)", 1600, 950);
        g.drawString("(H) <-- "+ROMLoader.byteToHexString((byte)ramPage)+" --> (J)", 1200, 950);
        
        //ROM
        g.drawString("ROM", 1690, 130);
        drawString(g,EaterEmulator.rom.toStringWithOffset(8,0x8000,true).substring(romPage*960,(romPage+1)*960), 1525, 150);
        
        //RAM
        g.drawString("RAM", 1280, 130);
        drawString(g,EaterEmulator.ram.toString(8,true).substring(ramPage*960,(ramPage+1)*960), 1125, 150);
	
        //CPU
        g.drawString("CPU Registers:",50,120);
        g.drawString("A: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.cpu.a)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.cpu.a)+")", 35, 150);
        g.drawString("X: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.cpu.x)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.cpu.x)+")", 35, 180);
        g.drawString("Y: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.cpu.y)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.cpu.y)+")", 35, 210);
        g.drawString("Stack Pointer: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.cpu.stackPointer)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.cpu.stackPointer)+")", 35, 240);
        g.drawString("Program Counter: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt(EaterEmulator.cpu.programCounter)), 16)+" ("+ROMLoader.padStringWithZeroes(Integer.toHexString(Short.toUnsignedInt(EaterEmulator.cpu.programCounter)),4)+")", 35, 270);
        g.drawString("Flags: ", 35, 300);
        
        g.drawString("Absolute Address: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt(EaterEmulator.cpu.addressAbsolute)), 16)+" ("+ROMLoader.byteToHexString((byte)(EaterEmulator.cpu.addressAbsolute/0xFF))+ROMLoader.byteToHexString((byte)EaterEmulator.cpu.addressAbsolute)+")", 35, 350);
        g.drawString("Relative Address: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Short.toUnsignedInt(EaterEmulator.cpu.addressRelative)), 16)+" ("+ROMLoader.byteToHexString((byte)(EaterEmulator.cpu.addressRelative/0xFF))+ROMLoader.byteToHexString((byte)EaterEmulator.cpu.addressRelative)+")", 35, 380);
        g.drawString("Opcode: "+EaterEmulator.cpu.lookup[Byte.toUnsignedInt(EaterEmulator.cpu.opcode)]+" ("+ROMLoader.byteToHexString(EaterEmulator.cpu.opcode)+")", 35, 410);
        
        int counter = 0;
        String flagsString = "CZIDBUVN";
        for (char c : ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.cpu.flags)),8).toCharArray()) {
        	g.setColor((c == '1') ? Color.green : Color.red);
        	g.drawString(String.valueOf(flagsString.charAt(counter)), 120+15*counter, 300);
        	counter++;
        }
        
        g.setColor(Color.white);
        //VIA
        g.drawString("VIA Registers:",50,480);
        g.drawString("PORT A: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.via.PORTA)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.via.PORTA)+")", 35, 510);
        g.drawString("PORT B: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.via.PORTB)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.via.PORTB)+")", 35, 540);
        g.drawString("DDR  A: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.via.DDRA)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.via.DDRA)+")", 35, 570);
        g.drawString("DDR  B: "+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(EaterEmulator.via.DDRB)), 8)+" ("+ROMLoader.byteToHexString(EaterEmulator.via.DDRB)+")", 35, 600);
        
	}
	
	public static void drawString(Graphics g, String text, int x, int y) {
	    for (String line : text.split("\n"))
	        g.drawString(line, x, y += g.getFontMetrics().getHeight());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(t)) {
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
				}
				break;
			case 'k':
				if (romPage > 0) {
					romPage-=1;
				}
				break;
			case 'j':
				if (ramPage < 0x80) {
					ramPage+=1;
				}
				break;
			case 'h':
				if (ramPage > 0) {
					ramPage-=1;
				}
				break;
			case 'r':
				EaterEmulator.cpu.reset();
				EaterEmulator.lcd.reset();
				EaterEmulator.via = new VIA();
				EaterEmulator.ram = new RAM();
				break;
			case ' ':
				EaterEmulator.cpu.clock();
				break;
			case 'c':
				EaterEmulator.haltFlag = !EaterEmulator.haltFlag;
				break;
		}
		this.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		mousePos.setX(arg0.getX());
		mousePos.setY(arg0.getY());
		
		this.repaint();
	}
	
}
