import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SerialInterface extends JFrame implements ActionListener {

	private JTextArea textArea;
	private Byte typedKey = 0x00;
	private boolean hasKey = false;
	
    public SerialInterface(boolean isVisible) {
        // Set up the frame
        setTitle("Serial Interface");
        setSize(400, 300);
        getContentPane().setBackground(new Color(40, 40, 40)); // Dark gray background
        getRootPane().setBorder(null);

        // Create a text area for displaying text
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Set font to monospaced for console-like appearance

        // Set text area colors
        textArea.setBackground(new Color(40, 40, 40)); // Dark gray background
        textArea.setForeground(Color.WHITE); // White text color
        textArea.setCaretColor(Color.WHITE); // White cursor color

        // Disable the text area border
        textArea.setBorder(null);
        
        // Set up a key listener to capture key events
        textArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) { }

            @Override
            public void keyPressed(KeyEvent e) {
            	if(isAcceptableChar(e.getKeyChar())) { // have to restrict characters like shift from being sent
	            	hasKey = true;
	                typedKey = (byte) convertChar(e.getKeyChar());
            	}
            }

            @Override
            public void keyReleased(KeyEvent e) { }
        });

        // Set up the layout
        setLayout(new BorderLayout());
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Make the frame visible
		this.setAlwaysOnTop(true);
		this.setVisible(isVisible);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setResizable(false);
    }

	@Override
    public void setVisible(boolean isVisible) {
    	super.setVisible(isVisible);
    	if(isVisible) {
    		requestFocus();
    		textArea.requestFocus();
    	}
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
    
    // Function to get key
    public byte getKey() {
        if(!hasKey) return 0x00;
        hasKey = false;
    	return typedKey;
    }
    
    // Function to check if key is there
    public boolean hasKey() {
    	return hasKey;
    }

    // Function to receive key
    public void receiveKey(byte keyChar) {
		if (keyChar == 0x0A) {
			if (!EaterEmulator.carriageReturn) textArea.append("\n");
		} else if (keyChar == 0x0D) {
			if (EaterEmulator.carriageReturn) textArea.append("\n");
		} else if (keyChar == 0x08) {
			textArea.setText(textArea.getText().substring(0, textArea.getText().length() - 1));
		} else textArea.append(String.valueOf((char)keyChar));
    }

    // Function to reset the text area
    public void reset() {
        textArea.setText("");
    }
    
    private static boolean isAcceptableChar(char c) {
    	int i = (int)c;
    	if(i==0x09) return true; //horizontal tab
    	if(i==0x0a) return true; //enter
    	if(i==0x1b) return true; //escape
    	if(i==0x08) return true; //backspace
    	if(i==0x7f) return true; //delete
    	return c >= ' ' && c <= '~'; //normal ascii range
    }

	private char convertChar(char keyChar) {
		if (keyChar == '\n' && EaterEmulator.carriageReturn) return '\r';
		return keyChar;
	}
}
