import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FileDialog;
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
	FileDialog fc = new java.awt.FileDialog((java.awt.Frame) null);
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

    JLabel GPUModeExplanationLabel = new JLabel("<html>GPU Mode 0 = Speiser Character Mode (customizable)<br>GPU Mode 1 = Eater Bitmap Mode (100x75, 64 colors, Buffer @ $2000)<br>GPU Mode 2 = Eater Bitmap Mode (customizable)</html>");

    JLabel GPUBitmapPixelScaleLabel = new JLabel("GPU Bitmap Pixel Scale: ");
    JTextField GPUBitmapPixelScaleTextField = new JTextField(""+data.GPUMode);

    JLabel LCDModeLabel = new JLabel("LCD Mode: ");
    ButtonGroup lcdModeButtonGroup = new ButtonGroup();
    JRadioButton LCDModeRadioSmall = new JRadioButton("16x2");
    JRadioButton LCDModeRadioLarge = new JRadioButton("20x4");

    JLabel KeyboardLocationLabel = new JLabel("Keyboard Memory Location: ");
    JTextField KeyboardLocationTextField = new JTextField(""+data.keyboardLocation);
    JLabel KeyboardLocationHexLabel = new JLabel(Integer.toHexString(data.GPUBufferBegin));

    JLabel ForegroundColorLabel = new JLabel("Foreground Color");
    JTextField ForegroundColorChooser = new JTextField("#"+Integer.toHexString(data.fgColor.getRGB()).substring(2));

    JLabel BackgroundColorLabel = new JLabel("Background Color");
    JTextField BackgroundColorChooser = new JTextField("#"+Integer.toHexString(data.bgColor.getRGB()).substring(2));

    int VRAMSize;
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

		fc.setVisible(false);
		fc.setDirectory(data.defaultFileChooserDirectory);

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
        SwingComponentsList.add(GPUModeExplanationLabel);
        SwingComponentsList.add(GPUBitmapPixelScaleLabel);
        SwingComponentsList.add(GPUBitmapPixelScaleTextField);
        SwingComponentsList.add(KeyboardLocationLabel);
        SwingComponentsList.add(KeyboardLocationTextField);
        SwingComponentsList.add(KeyboardLocationHexLabel);
        SwingComponentsList.add(LCDModeLabel);
        SwingComponentsList.add(LCDModeRadioLarge);
        SwingComponentsList.add(LCDModeRadioSmall);

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

        lcdModeButtonGroup.add(LCDModeRadioSmall);
        lcdModeButtonGroup.add(LCDModeRadioLarge);

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

            //Keyboard Address Hex Decoding
            int KBAddr = 0;

            try {
                KBAddr = Integer.parseInt(KeyboardLocationTextField.getText().equals("") ? "0" : KeyboardLocationTextField.getText());
            } catch (Exception e) {
                e.printStackTrace();
            };

            KeyboardLocationHexLabel.setText("= $"+Integer.toHexString(KBAddr));

            //VRAM Size
            if (data.GPUMode == 0) {
                VRAMSize = data.GPUCols*data.GPURows;
            } else if (data.GPUMode == 1 || data.GPUMode == 2) {
                data.GPUWidth = 100;
                data.GPUHeight = 75;
                data.GPUBufferBegin = 8192;
                VRAMSize = data.GPUWidth*data.GPUHeight;
            } else {
                VRAMSize = 0;
            }

            VRAMSizeLabel.setText("("+VRAMSize+" bytes)");
            VRAMRangeLabel.setText("VRAM Range: $"+Integer.toHexString(VBStartAddr)+" - $"+Integer.toHexString(VBStartAddr+VRAMSize-1));
		}

        //Buttons
        if (arg0.getSource().equals(openOptionsFileButton)) {
            fc.setDirectory(openOptionsFileText.getText());
            fc.setFile("");
			
            fc.setMode(FileDialog.LOAD);
            fc.setVisible(true);

	        if (fc.getFile() != null) {
                optionsFile = new File(fc.getDirectory()+fc.getFile());
	            openOptionsFileText.setText(optionsFile.getAbsolutePath());
            }

            readDataFromFile(optionsFile);
            applySwingValues();
            updateSwingComponents();
        } else
        if (arg0.getSource().equals(applyOptionsButton)) {
            applySwingValues();
            updateSwingComponents();
        } else
        if (arg0.getSource().equals(saveOptionsButton)) {
            File saveFile = new File(openOptionsFileText.getText());

            fc.setDirectory(saveFile.getParent());
            fc.setFile(saveFile.getName());

			fc.setMode(FileDialog.SAVE);

            fc.setVisible(true);
            File newOptionsFile;

	        if (fc.getFile() != null) {
                newOptionsFile = saveFile;
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

        GPUWidthTextField.setEditable(!(data.GPUMode == 1));
        GPUHeightTextField.setEditable(!(data.GPUMode == 1));
        GPUBufferAddressOptionTextField.setEditable(!(data.GPUMode == 1));

        if (data.GPUMode == 1) {
            GPUWidthTextField.setText(""+100);
            GPUHeightTextField.setText(""+75);
            GPUBufferAddressOptionTextField.setText(""+8192);
        } else {
            GPUWidthTextField.setText(""+data.GPUWidth);
            GPUHeightTextField.setText(""+data.GPUHeight);
            GPUBufferAddressOptionTextField.setText(""+data.GPUBufferBegin);
        }
        
        GPUColsTextField.setText(""+data.GPUCols);
        GPURowsTextField.setText(""+data.GPURows);

        GPUModeOptionTextField.setText(""+data.GPUMode);
        GPUBitmapPixelScaleTextField.setText(""+data.GPUBitmapPixelScale);
        ForegroundColorChooser.setText("#"+Integer.toHexString(data.fgColor.getRGB()).substring(2));
        BackgroundColorChooser.setText("#"+Integer.toHexString(data.bgColor.getRGB()).substring(2));

        LCDModeRadioSmall.setSelected(!data.lcdBigMode);
        LCDModeRadioLarge.setSelected(data.lcdBigMode);
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
        data.GPUBitmapPixelScale = Integer.parseInt(GPUBitmapPixelScaleTextField.getText());
        data.keyboardLocation = Integer.parseInt(KeyboardLocationTextField.getText());

        Bus.VIA_ADDRESS = data.VIA_Address;
        boolean gpuWasVisible = EaterEmulator.gpu.isVisible();
        EaterEmulator.gpu.dispose();

        data.GPUWidth = GPU.width = (data.GPUMode == 1) ? 100 : data.GPUWidth;
        data.GPUHeight = GPU.height = (data.GPUMode == 1) ? 75 : data.GPUHeight;
        data.GPUBufferBegin = GPU.VRAM_START_ADDRESS = (data.GPUMode == 1) ? 8192 : data.GPUBufferBegin;

        GPU.n_cols = data.GPUCols;
        GPU.n_rows = data.GPURows;
        GPU.gpuMode = data.GPUMode;
        GPU.GPUPixelScale = data.GPUBitmapPixelScale;

        EaterEmulator.gpu = new GPU(EaterEmulator.gpu.vram,gpuWasVisible);

        DisplayPanel.fgColor = data.fgColor;
        DisplayPanel.bgColor = data.bgColor;

        EaterEmulator.GraphicsPanel.resetGraphics();

        EaterEmulator.fc.setDirectory(data.defaultFileChooserDirectory);
        fc.setDirectory(data.defaultFileChooserDirectory);

        data.lcdBigMode = LCDModeRadioLarge.isSelected();
        EaterEmulator.lcd.bigMode = data.lcdBigMode;
        EaterEmulator.lcd.updateMode();
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
        VRAMSizeLabel.setBounds(400,(data.GPUMode == 1 || data.GPUMode == 2) ? 120 : 160,100,25);

        GPUBufferAddressOptionLabel.setBounds(75,200,225,25);
        GPUBufferAddressOptionTextField.setBounds(300,200,100,25);
        GPUBufferAddressOptionHexLabel.setBounds(400,200,100,25);

        GPUModeOptionLabel.setBounds(225,240,75,25);
        GPUModeOptionTextField.setBounds(300,240,25,25);

        GPUBitmapPixelScaleLabel.setBounds(150,280,150,25);
        GPUBitmapPixelScaleTextField.setBounds(300,280,25,25);

        VRAMRangeLabel.setBounds(175,320,300,25);

        GPUModeExplanationLabel.setBounds(175,340,500,100);

        KeyboardLocationLabel.setBounds(125,440,175,25);
        KeyboardLocationTextField.setBounds(300,440,100,25);
        KeyboardLocationHexLabel.setBounds(400,440,100,25);

        LCDModeLabel.setBounds(225,480,75,25);
        LCDModeRadioSmall.setBounds(300,480,100,25);
        LCDModeRadioLarge.setBounds(400,480,100,25);

        ForegroundColorLabel.setBounds(175,520,125,25);
        ForegroundColorChooser.setBounds(300,520,100,25);

        BackgroundColorLabel.setBounds(175,560,125,25);
        BackgroundColorChooser.setBounds(300,560,100,25);

        applyOptionsButton.setBounds(200,625,150,25);
        saveOptionsButton.setBounds(350,625,150,25);
    }
}
