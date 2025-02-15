import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class DisassemblyOutput extends JFrame {

	static DisassemblyPanel DisPanel = new DisassemblyPanel();

	public static int StartAddress = 0, EndAddress = 0;

	public DisassemblyOutput() {
		setSize(EaterEmulator.windowWidth, EaterEmulator.windowHeight);

		setTitle("6502 Disassembly");
		setContentPane(DisPanel);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
	}

	public void updateDisplay() {
		DisPanel.setBackground(DisplayPanel.bgColor);
		DisPanel.setForeground(DisplayPanel.fgColor);
	}
}

class DisassemblyPanel extends JPanel {

	private Rectangle[] textBounds = new Rectangle[0];
	private short[] nextInstructionAddresses = new short[0];

	private Font smallerFont;

	public DisassemblyPanel() {
		super(null);

		setBackground(DisplayPanel.bgColor);
		setPreferredSize(new Dimension(EaterEmulator.windowWidth, EaterEmulator.windowHeight));

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
		String[] text = DisplayPanel.disassembleUntil((short)DisassemblyOutput.StartAddress, (short)DisassemblyOutput.EndAddress).split("\n");
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