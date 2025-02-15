import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class DisassemblyOutput extends JFrame {

	static DisassemblyPanel DisPanel = new DisassemblyPanel();

	public static int StartAddress = 0, EndAddress = 0;

	public DisassemblyOutput() {
		setSize((int) (EaterEmulator.windowWidth * 0.9), (int) (EaterEmulator.windowHeight * 0.9));

		setTitle("6502 Disassembly");
		setContentPane(DisPanel);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
	}

	public void updateDisplay() {
		DisPanel.setBackground(DisplayPanel.bgColor);
		DisPanel.setForeground(DisplayPanel.fgColor);
	}

	public static String disassembleUntil(short programCounter, short finalCount) {
		StringBuilder str = new StringBuilder();
		for (short j = 0; programCounter < finalCount; j++) {
			Instruction currentInstruction = EaterEmulator.cpu.lookup[Byte.toUnsignedInt(Bus.read(programCounter))];
			if (!str.isEmpty()) str.append("\n");
			str.append(disassemble(programCounter));
			switch (currentInstruction.addressMode) {
				case AddressMode.IMP, AddressMode.ACC -> programCounter += 1;
				case AddressMode.IMM, AddressMode.ZPP, AddressMode.ZPX, AddressMode.ZPY, AddressMode.REL,
					 AddressMode.IZX, AddressMode.IZY, AddressMode.ZPI -> programCounter += 2;
				case AddressMode.ABS, AddressMode.ABX, AddressMode.ABY, AddressMode.IND, AddressMode.IAX ->
						programCounter += 3;
			}
		}
		return str.toString();
	}

	public static String disassemble(short programCounter, short numInstructions) {
		StringBuilder str = new StringBuilder();
		for (short j = 0; j <= numInstructions; j++) {
			Instruction currentInstruction = EaterEmulator.cpu.lookup[Byte.toUnsignedInt(Bus.read(programCounter))];
			if (!str.isEmpty()) str.append("\n");
			str.append(disassemble(programCounter));
			switch (currentInstruction.addressMode) {
				case AddressMode.IMP, AddressMode.ACC -> programCounter += 1;
				case AddressMode.IMM, AddressMode.ZPP, AddressMode.ZPX, AddressMode.ZPY, AddressMode.REL,
					 AddressMode.IZX, AddressMode.IZY, AddressMode.ZPI -> programCounter += 2;
				case AddressMode.ABS, AddressMode.ABX, AddressMode.ABY, AddressMode.IND, AddressMode.IAX ->
						programCounter += 3;
			}
		}
		return str.toString();
	}

	public static String disassemble(short programCounter) {
		Instruction currentInstruction = EaterEmulator.cpu.lookup[Byte.toUnsignedInt(Bus.read(programCounter))];

		String str = toHexShortString(programCounter, 4) + ": " + currentInstruction.opcode.toString();
		switch (currentInstruction.addressMode) {
			case ACC -> str = str + " A";
			case ABS -> str = str + " $" + busHexString((short) (programCounter+2)) + busHexString((short) (programCounter+1));
			case ABX -> str = str + " $" + busHexString((short) (programCounter+2)) + busHexString((short) (programCounter+1)) + ",X";
			case ABY -> str = str + " $" + busHexString((short) (programCounter+2)) + busHexString((short) (programCounter+1)) + ",Y";
			case IMM -> str = str + " #$" + busHexString((short) (programCounter+1));
			case IMP -> {}
			case IND -> str = str + " ($" + busHexString((short) (programCounter+2)) + busHexString((short) (programCounter+1)) + ")";
			case IZX -> str = str + " ($" + busHexString((short) (programCounter+1)) + ",X)";
			case IZY -> str = str + " ($" + busHexString((short) (programCounter+1)) + "),Y";
			case REL, ZPP -> str = str + " $" + busHexString((short) (programCounter+1));
			case ZPX -> str = str + " $" + busHexString((short) (programCounter+1)) + ",X";
			case ZPY -> str = str + " $" + busHexString((short) (programCounter+1)) + ",Y";
			case ZPI -> str = str + " ($" + busHexString((short) (programCounter+1)) + ")";
		}
		return str;
	}

	public static String busHexString(short address) {
		return toHexShortString(Bus.read(address), 2);
	}

	public static String toHexShortString(int i, int packNumChars) {
		StringBuilder str = new StringBuilder(Integer.toHexString(i));
		while (str.length() < packNumChars) str.insert(0, "0");
		if (str.length() > packNumChars) str = new StringBuilder(str.substring(str.length() - packNumChars));
		return str.toString();
	}
}

class DisassemblyPanel extends JPanel {

	private Rectangle[] textBounds = new Rectangle[0];
	private short[] nextInstructionAddresses = new short[0];

	private Font smallerFont;

	public DisassemblyPanel() {
		super(null);

		setBackground(DisplayPanel.bgColor);
		setPreferredSize(new Dimension((int) (EaterEmulator.windowWidth * 0.9), (int) (EaterEmulator.windowHeight * 0.9)));

		try {
			smallerFont = Font.createFont(Font.TRUETYPE_FONT,this.getClass().getClassLoader().getResourceAsStream("courbd.ttf")).deriveFont(14f);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(smallerFont);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
			if (EaterEmulator.verbose) System.out.println("Error loading Courier Font!");
		}

		this.setFocusable(true);
		this.requestFocus();

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
//				if (debug) System.out.println("mouseClicked");
				for (int i = 0; i < textBounds.length; i++) {
					if (textBounds[i] != null && textBounds[i].contains(e.getPoint())) {
						if (DisplayPanel.breakpoints.contains(nextInstructionAddresses[i])) DisplayPanel.breakpoints.remove((Short)nextInstructionAddresses[i]);
						else DisplayPanel.breakpoints.add(nextInstructionAddresses[i]);
//						if (debug) System.out.println("matched with textBound " + textBounds[i] + " and it now contains it: " + breakpoints.contains(nextInstructionAddresses[i]));
					}
				}
				repaint();
				requestFocus();
			}
		});
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(DisplayPanel.fgColor);

		//Disassembly
		g.setFont(smallerFont);
		g.drawString("Instructions",50,30);
		String[] text = DisassemblyOutput.disassembleUntil((short)DisassemblyOutput.StartAddress, (short)DisassemblyOutput.EndAddress).split("\n");
		int x = 75, y = 50;
		textBounds = new Rectangle[text.length];
		nextInstructionAddresses = new short[text.length];
		int lineLongestWidth = 0;
		for (int i = 0; i < text.length; i++) {
			String line = text[i];
			short address = (short) Integer.parseInt(line.substring(0,4), 16);
			if (DisplayPanel.breakpoints.contains(address)) g.setColor(Color.RED);

			FontMetrics fm = g.getFontMetrics();
			int textWidth = fm.stringWidth(line);
			if (textWidth > lineLongestWidth) lineLongestWidth = textWidth;
			int textHeight = fm.getAscent();
			textBounds[i] = new Rectangle(x, y, textWidth, textHeight);
			nextInstructionAddresses[i] = address;

			g.drawString(line, x, y += fm.getHeight());
			if (y >= getHeight() - 50) {
				x += lineLongestWidth + 15;
				lineLongestWidth = 0;
				y = 50;
			}

			if (DisplayPanel.breakpoints.contains(address)) g.setColor(DisplayPanel.fgColor);
		}
	}
}