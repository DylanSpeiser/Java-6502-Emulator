import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import java.nio.file.*;
import java.util.Scanner;

import javax.swing.*;

public class GPU extends JFrame implements ActionListener {
	GPUPanel p = new GPUPanel();
	Timer t;
	Scanner s;

    public static int VRAM_START_ADDRESS = 0x6000;
	
	public static int n_cols = 80;
    public static int n_rows = 60;

    public static int width = 640;
    public static int height = 480;

    int charWidth;
    int charHeight;

    int effectiveCharWidth;
    int effectiveCharHeight;
	
	boolean debug = false;

    public static InputStream charsetStream;
    BufferedImage[] charImages = new BufferedImage[256];

    RAM vram;
	
	public GPU(RAM vram) {
		this.setSize(width,height);
		t = new Timer(16,this);
		t.start();

        this.vram = vram;

        charWidth = 8;
        charHeight = 8;

        effectiveCharWidth = width/n_cols;
        effectiveCharHeight = height/n_rows;

        if (debug)
            System.out.println("charWidth, charHeight = "+charWidth+", "+charHeight);

        charsetStream = this.getClass().getClassLoader().getResourceAsStream("DylSCII.bin");
        byte[] charsetBytes = {};

        try {
            charsetBytes = charsetStream.readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to read charset.");
            System.exit(ABORT);
        }

        for (int i = 0; i < charImages.length; i++) {
            charImages[i] = new BufferedImage(8,8,BufferedImage.TYPE_BYTE_BINARY);
            if (debug)
                System.out.print("Byte index: "+(i*charHeight)+" ");

            for (int y = 0; y < charHeight; y++) {
                byte lineData = charsetBytes[i*charHeight + y];

                for (int x = 0; x < charWidth; x++) {
                    Graphics gr = charImages[i].getGraphics();
                    gr.setColor(getBit(lineData,7-x)==1 ? Color.WHITE : Color.black);
                    gr.drawRect(x, y, 1, 1);

                    //charImages[i].setRGB(x, y, getBit(lineData,x)==1 ? 255 : 0);
                }

                if (debug)
                    System.out.print(ROMLoader.byteToHexString(lineData)+" ");
            }

            // if (debug) {
            //     try {
            //         ImageIO.write(charImages[i], "png", new File("charImg/"+i+".png"));
            //     } catch (Exception e) {
            //         e.printStackTrace();
            //         System.out.println("ERROR WRITING CHAR IMAGE");
            //     }
            // }

            if (debug)
                System.out.println();
        }

        if (debug) {
            for (int i = 0; i < 255; i++)
                vram.write((short)i, (byte)i);
        }
		
		this.setTitle("GPU");
		this.setContentPane(p);
		this.setAlwaysOnTop(true);
		this.setVisible(false);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setResizable(false);
	}

    public GPU() {
        this(new RAM());
    }
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		GPU gpu = new GPU();
        gpu.setVisible(true);
		
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter the path to a .bin file with character data:");
		
		while (true) {
			String input = scan.nextLine();
			
			File f = new File(input);
            byte[] newData = new byte[0];
            byte[] newRAMArray = new byte[0x8000];

            System.out.println("Created new RAM Array with "+newRAMArray.length+" bytes.");

            try {
                newData = Files.readAllBytes(f.toPath());
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to read character data.");
                System.exit(ABORT);
            }

            System.out.println("Read "+newData.length+" bytes.");

            System.arraycopy(newData, 0, newRAMArray, GPU.VRAM_START_ADDRESS, Math.min(newData.length,newRAMArray.length));

            gpu.vram.setRAMArray(newRAMArray);

            //System.out.println(ROMLoader.ROMString(gpu.vram.getRAMArray(),40,false));
		}
	}
	
	public void reset() {
		p.repaint();
	}
	
	public class GPUPanel extends JPanel {
		public GPUPanel() {
			
		}
		
		public void paintComponent(Graphics g) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, p.getWidth(), p.getHeight());

			g.setColor(Color.white);

			for (int i = 0; i<n_rows; i++) {
				for (int j = 0; j<n_cols; j++) {
                    int index = i*n_cols + j;
                    int character = Byte.toUnsignedInt(vram.read((short)(VRAM_START_ADDRESS+index)));

					g.drawImage(charImages[character],j*effectiveCharWidth,i*effectiveCharHeight,effectiveCharWidth,effectiveCharHeight,this);
                    if (debug) {
                        System.out.println("Painted Char #"+character+" @ index "+index+" ("+j*charWidth+","+i*charHeight+")");
                    }
				}
			}
		}
	}

    public void setRAM(RAM r) {
        this.vram = r;
    }

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(t)) {
			p.repaint();
		}
	}

    private static byte getBit(byte b, int position) {
       return (byte)((b >> position) & 1);
    }
}
