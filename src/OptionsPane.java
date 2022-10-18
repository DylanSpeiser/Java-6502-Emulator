import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import javax.swing.*;
import java.util.ArrayList;

public class OptionsPane extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;

    OptionsData data = new OptionsData();

	OptionsPanel p = new OptionsPanel();
	Timer t;

    File optionsFile;

    //Swing Stuff
    ArrayList<JComponent> SwingComponentsList = new ArrayList<JComponent>();
    JFileChooser fc = new JFileChooser();
    JButton openOptionsFileButton = new JButton("Open File");
    JTextField openOptionsFileText = new JTextField(data.defaultSaveDirectory+"options.pref");
    JLabel openOptionsFileLabel = new JLabel("Load options from file");

    JLabel DefaultFileChooserPathOptionLabel = new JLabel("Default File Chooser Path");
    JTextField DefaultFileChooserPathOptionTextField = new JTextField(data.defaultFileChooserDirectory);

    JLabel VIAAddressOptionLabel = new JLabel("VIA Address");
    JTextField VIAAddressOptionTextField = new JTextField(""+data.VIA_Address);
    JLabel VIAAddressOptionHexLabel = new JLabel(Integer.toHexString(data.VIA_Address));

    JLabel GPUResolutionOptionLabel = new JLabel("GPU Resolution (WxH)");
    JTextField GPUHeightTextField = new JTextField(""+data.GPUHeight);
    JTextField GPUWidthTextField = new JTextField(""+data.GPUWidth);

    JLabel GPUCharGridOptionLabel = new JLabel("GPU Character Grid (ColsxRows)");
    JTextField GPUColsTextField = new JTextField(""+data.GPUCols);
    JTextField GPURowsTextField = new JTextField(""+data.GPURows);
    
    JLabel GPUBufferAddressOptionLabel = new JLabel("GPU Char/Pixel Buffer Start Address");
    JTextField GPUBufferAddressOptionTextField = new JTextField(""+data.GPUBufferBegin);
    JLabel GPUBufferAddressOptionHexLabel = new JLabel(Integer.toHexString(data.GPUBufferBegin));
    
    JLabel GPUModeOptionLabel = new JLabel("GPU Mode");
    JTextField GPUModeOptionTextField = new JTextField(""+data.GPUMode);

    JLabel ForegroundColorLabel = new JLabel("Foreground Color");
    JTextField ForegroundColorChooser = new JTextField("#"+Integer.toHexString(data.fgColor.getRGB()).substring(2));

    JLabel BackgroundColorLabel = new JLabel("Background Color");
    JTextField BackgroundColorChooser = new JTextField("#"+Integer.toHexString(data.bgColor.getRGB()).substring(2));

    int VRAMSize = data.GPUCols*data.GPURows;
    JLabel VRAMSizeLabel = new JLabel("("+VRAMSize+"bytes)");
    JLabel VRAMRangeLabel = new JLabel("VRAM Range: $"+Integer.toHexString(data.GPUBufferBegin)+"-$"+Integer.toHexString(data.GPUBufferBegin+VRAMSize-1));

    JButton applyOptionsButton = new JButton("Apply Options");
    JButton saveOptionsButton = new JButton("Save Options to File");
	
	public OptionsPane() {
		this.setSize(700,700);
		t = new Timer(16,this);
		t.start();

        //File chooser
        try {
            Files.createDirectories(Paths.get(data.defaultSaveDirectory));
        } catch (IOException e) {
            e.printStackTrace();
        }

        optionsFile = new File(data.defaultSaveDirectory+"options.pref");

        writeDataToFile(new File(data.defaultSaveDirectory+"defaults.pref"));
        readDataFromFile(optionsFile);
        writeDataToFile(optionsFile);

		fc.setVisible(true);
		fc.setCurrentDirectory(new File(data.defaultFileChooserDirectory));

        //Swing Components
        SwingComponentsList.add(openOptionsFileButton);
        SwingComponentsList.add(openOptionsFileText);
        SwingComponentsList.add(openOptionsFileLabel);
        SwingComponentsList.add(VIAAddressOptionLabel);
        SwingComponentsList.add(VIAAddressOptionTextField);
        SwingComponentsList.add(VIAAddressOptionHexLabel);
        SwingComponentsList.add(GPUResolutionOptionLabel);
        SwingComponentsList.add(GPUHeightTextField);
        SwingComponentsList.add(GPUWidthTextField);
        SwingComponentsList.add(GPUBufferAddressOptionLabel);
        SwingComponentsList.add(GPUBufferAddressOptionTextField);
        SwingComponentsList.add(GPUModeOptionLabel);
        SwingComponentsList.add(GPUModeOptionTextField);
        SwingComponentsList.add(applyOptionsButton);
        SwingComponentsList.add(saveOptionsButton);
        SwingComponentsList.add(DefaultFileChooserPathOptionLabel);
        SwingComponentsList.add(DefaultFileChooserPathOptionTextField);
        SwingComponentsList.add(GPUBufferAddressOptionHexLabel);
        SwingComponentsList.add(GPUCharGridOptionLabel);
        SwingComponentsList.add(GPUColsTextField);
        SwingComponentsList.add(GPURowsTextField);
        SwingComponentsList.add(ForegroundColorLabel);
        SwingComponentsList.add(ForegroundColorChooser);
        SwingComponentsList.add(BackgroundColorLabel);
        SwingComponentsList.add(BackgroundColorChooser);
        SwingComponentsList.add(VRAMRangeLabel);
        SwingComponentsList.add(VRAMSizeLabel);

        this.setTitle("Options");
		this.setContentPane(p);
		this.setAlwaysOnTop(true);
		this.setVisible(true);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setResizable(false);

        p.setBackground(new Color(200,200,200));

        for (JComponent component : SwingComponentsList) {
            component.setVisible(true);
            component.setBackground(Color.white);
            if (component instanceof JButton) {
                ((JButton)(component)).addActionListener(this);
            }
            p.add(component);
        }

        //Swing Positioning
        resetSwingPositions();
	}
	
	// @SuppressWarnings("unused")
	// public static void main(String[] args) {
	// 	OptionsPane options = new OptionsPane();
	// }
	
	public void reset() {
		p.repaint();
        resetSwingPositions();
	}
	
	public class OptionsPanel extends JPanel {
		public OptionsPanel() {
			
		}
		
		public void paintComponent(Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, p.getWidth(), p.getHeight());

			g.setColor(getForeground());

			
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(t)) {
            p.repaint();

            resetSwingPositions();

            //VIA Address Hex Decoding
            int VIAaddr = 0;

            try {
                VIAaddr = Integer.parseInt(VIAAddressOptionTextField.getText().equals("") ? "0" : VIAAddressOptionTextField.getText());
            } catch (Exception e) {
                e.printStackTrace();
            };

            VIAAddressOptionHexLabel.setText("= $"+Integer.toHexString(VIAaddr));

            //Video Buffer Start Address Hex Decoding
            int VBStartAddr = 0;

            try {
                VBStartAddr = Integer.parseInt(GPUBufferAddressOptionTextField.getText().equals("") ? "0" : GPUBufferAddressOptionTextField.getText());
            } catch (Exception e) {
                e.printStackTrace();
            };

            GPUBufferAddressOptionHexLabel.setText("= $"+Integer.toHexString(VBStartAddr));

            VRAMSizeLabel.setText("("+Integer.parseInt(GPUColsTextField.getText())*Integer.parseInt(GPURowsTextField.getText())+" bytes)");
            VRAMRangeLabel.setText("VRAM Range: $"+Integer.toHexString(VBStartAddr)+" - $"+Integer.toHexString(VBStartAddr+VRAMSize-1));
		}

        //Buttons
        if (arg0.getSource().equals(openOptionsFileButton)) {
            fc.setCurrentDirectory(new File(openOptionsFileText.getText()));
            fc.setSelectedFile(new File(""));
			int returnVal = fc.showOpenDialog(this);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
                optionsFile = fc.getSelectedFile();
	            openOptionsFileText.setText(optionsFile.getAbsolutePath());
            }

            readDataFromFile(optionsFile);
            applySwingValues();
        } else
        if (arg0.getSource().equals(applyOptionsButton)) {
            applySwingValues();
        } else
        if (arg0.getSource().equals(saveOptionsButton)) {
            fc.setVisible(true);
            fc.setSelectedFile(new File(openOptionsFileText.getText()));
			int returnVal = fc.showSaveDialog(this);
            File newOptionsFile;

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
                newOptionsFile = fc.getSelectedFile();

                writeDataToFile(newOptionsFile);
            }
        }
	}

    private void readDataFromFile(File f) {
      try {
        OptionsData newData = null;

         FileInputStream fileIn = new FileInputStream(f);
         ObjectInputStream in = new ObjectInputStream(fileIn);
         newData = (OptionsData) in.readObject();
         in.close();
         fileIn.close();

         data = newData;
         updateSwingComponents();
      } catch (Exception e) {
        System.out.println("Reading options data failed!");
         e.printStackTrace();
      }
    }

    public void updateSwingComponents() {
        DefaultFileChooserPathOptionTextField.setText(data.defaultFileChooserDirectory);
        VIAAddressOptionTextField.setText(""+data.VIA_Address);
        GPUWidthTextField.setText(""+data.GPUWidth);
        GPUHeightTextField.setText(""+data.GPUHeight);
        GPUColsTextField.setText(""+data.GPUCols);
        GPURowsTextField.setText(""+data.GPURows);
        GPUBufferAddressOptionTextField.setText(""+data.GPUBufferBegin);
        GPUModeOptionTextField.setText(""+data.GPUMode);
        ForegroundColorChooser.setText("#"+Integer.toHexString(data.fgColor.getRGB()).substring(2));
        BackgroundColorChooser.setText("#"+Integer.toHexString(data.bgColor.getRGB()).substring(2));
    }

    private void writeDataToFile(File f) {
        try {
            FileOutputStream fileOut = new FileOutputStream(f);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(data);
            objectOut.close();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void applySwingValues() {
        data.optionsFileString = optionsFile.getAbsolutePath();
        data.defaultFileChooserDirectory = DefaultFileChooserPathOptionTextField.getText();
        data.VIA_Address = Integer.parseInt(VIAAddressOptionTextField.getText());
        data.GPUWidth = Integer.parseInt(GPUWidthTextField.getText());
        data.GPUHeight = Integer.parseInt(GPUHeightTextField.getText());
        data.GPUCols = Integer.parseInt(GPUColsTextField.getText());
        data.GPURows = Integer.parseInt(GPURowsTextField.getText());
        data.GPUBufferBegin = Integer.parseInt(GPUBufferAddressOptionTextField.getText());
        data.GPUMode = Integer.parseInt(GPUModeOptionTextField.getText());
        data.fgColor = Color.decode(ForegroundColorChooser.getText());
        data.bgColor = Color.decode(BackgroundColorChooser.getText());

        Bus.VIA_ADDRESS = data.VIA_Address;
        EaterEmulator.gpu.dispose();
        GPU.width = data.GPUWidth;
        GPU.height = data.GPUHeight;
        GPU.n_cols = data.GPUCols;
        GPU.n_rows = data.GPURows;
        GPU.VRAM_START_ADDRESS = data.GPUBufferBegin;
        EaterEmulator.gpu = new GPU(EaterEmulator.gpu.vram);

        DisplayPanel.fgColor = data.fgColor;
        DisplayPanel.bgColor = data.bgColor;

        EaterEmulator.GraphicsPanel.resetGraphics();

        EaterEmulator.fc.setCurrentDirectory(new File(data.defaultFileChooserDirectory));
        fc.setCurrentDirectory(new File(data.defaultFileChooserDirectory));
    }

    private void resetSwingPositions() {
        openOptionsFileButton.setBounds(575,10,100,25);
        openOptionsFileText.setBounds(175,10,400,25);
        openOptionsFileLabel.setBounds(25,10,200,25);

        DefaultFileChooserPathOptionLabel.setBounds(125,40,175,25);
        DefaultFileChooserPathOptionTextField.setBounds(300,40,350,25);

        VIAAddressOptionLabel.setBounds(200,80,100,25);
        VIAAddressOptionTextField.setBounds(300,80,100,25);
        VIAAddressOptionHexLabel.setBounds(400,80,100,25);

        GPUResolutionOptionLabel.setBounds(150,120,150,25);
        GPUWidthTextField.setBounds(300,120,50,25);
        GPUHeightTextField.setBounds(350,120,50,25);

        GPUCharGridOptionLabel.setBounds(150,160,150,25);
        GPUColsTextField.setBounds(300,160,50,25);
        GPURowsTextField.setBounds(350,160,50,25);
        VRAMSizeLabel.setBounds(400,160,100,25);

        GPUBufferAddressOptionLabel.setBounds(75,200,300,25);
        GPUBufferAddressOptionTextField.setBounds(300,200,100,25);
        GPUBufferAddressOptionHexLabel.setBounds(400,200,100,25);

        GPUModeOptionLabel.setBounds(225,240,75,25);
        GPUModeOptionTextField.setBounds(300,240,25,25);

        ForegroundColorLabel.setBounds(175,400,125,25);
        ForegroundColorChooser.setBounds(300,400,100,25);

        BackgroundColorLabel.setBounds(175,440,125,25);
        BackgroundColorChooser.setBounds(300,440,100,25);

        VRAMRangeLabel.setBounds(175,480,300,25);

        applyOptionsButton.setBounds(200,625,150,25);
        saveOptionsButton.setBounds(350,625,150,25);
    }
}
