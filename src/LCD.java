import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.*;

public class LCD extends JFrame implements ActionListener {
	LCDPanel p = new LCDPanel();
	Timer t;
	Timer cursorTimer;
	Font lcdFont;
	Scanner s;
	
	boolean graphicalCursorBlinkFlag = false;
	boolean bigMode = false;
	
	boolean debug = false;

	int cols = 16;
	int rows = 2;
	
	//Internal flags
	int cursorPos = 0;
	boolean increment = true;
	boolean displayPower = false;
	boolean cursor = false;
	boolean cursorBlink = false;
	boolean fourBitMode = false;
	
	char[] text = new char[80];
	
	public LCD() {
		this.setSize(565,185);
		t = new Timer(100,this);
		t.start();
		cursorTimer = new Timer(500,this);
		cursorTimer.start();
		
		String s = "";
		
		for (int i = 0; i < 80; i++) {
			if (i<s.length()) {
				text[i] = s.charAt(i);
			} else {
				text[i] = ' ';
			}
		}
		
		this.setTitle("LCD");
		this.setContentPane(p);
		this.setAlwaysOnTop(true);
		this.setVisible(false);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);

		updateMode();
	}
	
	public static void main(String[] args) {
		LCD lcd = new LCD();
		lcd.setVisible(true);
		
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		System.out.println("[R/W] [RS] [data]");
		
		while (true) {
			String input = scan.nextLine();
			
			char 	rw = input.charAt(0);
			boolean rs = input.charAt(2) == '1' ? true : false;
			
			if (Character.toUpperCase(rw) == 'W') {
				//Write

				try {
				byte data = (byte)Integer.parseInt(input.substring(4,12), 2);
				System.out.println((rs ? "Data" : "Instruction") + ": 0x" + ROMLoader.byteToHexString(data));
				lcd.write(rs, data);
				} catch (Exception e) {
					System.out.println("Error!");
				}

			} else {
				//Read
				byte readData = lcd.read(rs);
				System.out.println("Read byte 0x" + ROMLoader.byteToHexString(readData));
			}
		}
	}
	
	public void reset() {
		String s = "";
		
		for (int i = 0; i < 80; i++) {
			if (i<s.length()) {
				text[i] = s.charAt(i);
			} else {
				text[i] = ' ';
			}
		}
		
		graphicalCursorBlinkFlag = false;
		
		cursorPos = 0;
		increment = true;
		displayPower = false;
		cursor = false;
		cursorBlink = false;
		fourBitMode = false;
	}
	
	//Regular write to LCD
	public void write(boolean regSel, byte data) {
		if (regSel == false) {
			//INSTRUCTION
			if ((data & 0b10000000) == 0b10000000) {
				//Set DDRAM Address
				int newPos = (data & 0b01111111);

				if (newPos >= 0 && newPos < text.length) {
					cursorPos = (data & 0b01111111);
				} else {
					cursorPos = 0;
				}

			} else if ((data & 0b01000000) == 0b01000000) {
				//Set CGRAM Address
				System.out.println("LCD: Tried to set CGRAM Address, that is unimplemented!");

			} else if ((data & 0b00100000) == 0b00100000) {
				//FUNCTION SET
				if ((data & 0b00010000) == 0b00010000) {
					fourBitMode = false;
				} else {
					fourBitMode = true;
				}
				if (debug)
					System.out.println("Function: Four Bit Mode: "+fourBitMode);
			} else if ((data & 0b00010000) == 0b00010000) {
				//SHIFT
				boolean rightleft = false;
				if ((data & 0b00000100) == 0b00000100) {
					//RIGHT
					rightleft = true;
				} else {
					//LEFT
					rightleft = false;
				}
				if ((data & 0b00001000) == 0b00001000) {
					//SCREEN
					char[] newCharArray = new char[40];
					Arrays.fill(newCharArray, ' ');
					if (debug)
						System.out.println("Shifted screen to the "+(rightleft ? "right." : "left."));
				} else {
					//CURSOR
					cursorPos += (rightleft ? 1 : -1);
					if (cursorPos < 0)
						cursorPos = 0;
					if (debug) {
						System.out.println("Shifted cursor to the "+(rightleft ? "right." : "left."));
						System.out.println("CursorPos: "+cursorPos);
					}
				}
			} else if ((data & 0b00001000) == 0b00001000) {
				//DISPLAY CONTROL
				if ((data & 0b00000100) == 0b00000100) {
					displayPower = true;
				} else {
					displayPower = false;
					if (debug)
						System.out.println("Turned the Display off! | "+Integer.toBinaryString(Byte.toUnsignedInt(data)));
				}
				if ((data & 0b00000010) == 0b00000010) {
					cursor = true;
				} else {
					cursor = false;
				}
				if ((data & 0b00000001) == 0b00000001) {
					cursorBlink = true;
				} else {
					cursorBlink = false;
				}
				if (debug)
					System.out.println("Display Control: Power: "+displayPower+" Cursor: "+cursor+" Blink: "+cursorBlink);
			} else if ((data & 0b00000100) == 0b00000100) {
				//ENTRY MODE SET
				if ((data & 0b00000010) == 0b00000010) {
					increment = true;
				} else {
					increment = false;
				}
				if (debug)
					System.out.println("Set Entry Mode: Increment: "+increment);
			} else if ((data & 0b00000010) == 0b00000010) {
				//RETURN HOME
				cursorPos = 0;
				if (debug)
					System.out.println("Return Home");
			} else if (data == 0b00000001) {
				//CLEAR
				cursorPos = 0;
				text = new char[80];
				for (int i = 0; i < 80; i++) {
					text[i] = ' ';
				}
				if (debug)
					System.out.println("Cleared!");
			} else {
				System.out.println("Tried to do invalid instruction "+ROMLoader.byteToHexString(data));
			}
		} else {
			//DATA
			text[cursorPos] = (char)data;
			int prevCursorPos = cursorPos;
			cursorPos += increment ? 1 : -1;
			if (cursorPos >= text.length) {
				cursorPos = 0;
			} else if (cursorPos < 0) {
				cursorPos = 0;
			}
			if (debug)
				System.out.println("Data: Wrote "+(char)data+" at "+prevCursorPos);
		}
	}
	
	//Read from LCD
	public byte read(boolean regSel) {
		if (debug)
			System.out.println("Reading from LCD with regSel"+(regSel ? '1':'0'));

		byte retVal = 0;

		if (regSel) {
			//Read from RAM
			retVal = (byte)(text[cursorPos]);
		} else {
			//Read busy flag and address
			retVal = (byte)(127 & cursorPos);
		}

		return retVal;
	}
	
	public class LCDPanel extends JPanel {
		public LCDPanel() {
			try {
				lcdFont = Font.createFont(Font.TRUETYPE_FONT,this.getClass().getClassLoader().getResourceAsStream("5x8_lcd_hd44780u_a02.ttf")).deriveFont(47f);
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				ge.registerFont(lcdFont);
			} catch (FontFormatException | IOException e) {
				e.printStackTrace();
				System.out.println("Error loading LCD Font!");
			}
		}
		
		public void paintComponent(Graphics g) {
			g.setColor(Color.getHSBColor(0.62f, 0.83f, 1f));
			g.fillRect(0, 0, p.getWidth(), p.getHeight());
			g.setColor(Color.getHSBColor(0.62f, 0.87f, 0.78f));
			for (int i = 0; i<rows; i++) {
				for (int j = 0; j<cols; j++) {
					g.fillRect(12+33*i, 25+50*j, 30, 47);
				}
			}

			int[] rowRemap = {0,2,1,3};

			if (displayPower) {
				g.setColor(Color.white);
				g.setFont(lcdFont);
				for (int i = 0; i<rows; i++) {
					for (int j = 0; j<cols; j++) {
						g.drawString(String.valueOf(text[rowRemap[j]*rows + i]), 12+33*i, 70+50*j);

						if (rowRemap[j]*rows + i == cursorPos) {
							if (graphicalCursorBlinkFlag)
								g.fillRect(12+33*i, 66+50*j, 30, 5);
						}
					}
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(t)) {
			p.repaint();
		}
		if (arg0.getSource().equals(cursorTimer)) {
			if (cursor) {
				if (cursorBlink) {
					graphicalCursorBlinkFlag = !graphicalCursorBlinkFlag;
				} else {
					graphicalCursorBlinkFlag = cursor;
				}
			} else {
				graphicalCursorBlinkFlag = false;
			}
		}
	}

	public void updateMode() {
		if (!bigMode) {
			this.rows = 16;
			this.cols = 2;
			this.setSize(565,185);
		} else {
			this.rows = 20;
			this.cols = 4;
			this.setSize(685,275);
		}
	}
}
