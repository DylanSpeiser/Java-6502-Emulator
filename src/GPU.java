import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Scanner;

import javax.swing.*;

public class GPU extends JFrame implements ActionListener {
	GPUPanel p = new GPUPanel();

	Timer t;
	Scanner s;

    boolean scanned = false;

    public static int VRAM_START_ADDRESS = 0x6000;
	
	public static int n_cols = 80;
    public static int n_rows = 60;

    public static int width = 100;
    public static int height = 75;

    public static int gpuMode = 1;
    public static int GPUPixelScale = 8;

    int charWidth;
    int charHeight;

    int effectiveCharWidth;
    int effectiveCharHeight;
	
	boolean debug = false;

    public static InputStream charsetStream;
    BufferedImage[] charImages = new BufferedImage[256];

    RAM vram;
	
	public GPU(RAM vram,boolean isVisible) {
        this.setSize(GPUPixelScale*width,(GPUPixelScale*height)+30); //+30 for title bar
		
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
		this.setVisible(isVisible);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setResizable(false);
	}

    public GPU(boolean isVisible) {
        this(new RAM(),isVisible);
    }
	
	public static void main(String[] args) {
		GPU gpu = new GPU(true);
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

            System.arraycopy(newData, 0, newRAMArray, (gpuMode == 1 | gpuMode == 2) ? 0 : (GPU.VRAM_START_ADDRESS), Math.min(newData.length,newRAMArray.length));

            gpu.vram.setRAMArray(newRAMArray);

            gpu.scanned = true;
            // System.out.println(ROMLoader.ROMString(gpu.vram.getRAMArray(),40,false));
            // System.exit(0);
		}
	}
	
	public void reset() {
		p.repaint();
	}
	
	public class GPUPanel extends JPanel {
		public GPUPanel() {
			
		}
		
		public void paintComponent(Graphics g) {
            if (this.isVisible()) {
    			g.setColor(Color.BLACK);
    			g.fillRect(0, 0, p.getWidth(), p.getHeight());

    			g.setColor(Color.white);

                if (gpuMode == 0) {
                    //Speiser Character Mode
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
                } else if (gpuMode == 1 || gpuMode == 2) {
                    //Eater Bitmap Mode
                    int nextPowerOf2 = (int)Math.pow(2,((int)Math.ceil(Math.log(width)/Math.log(2))));
                    for (int i = 0; i<height; i++) {
                        for (int j = 0; j<width; j++) {
                            int index = j+nextPowerOf2*i;

                            /*
                            System.out.println(" i: "+Integer.toBinaryString(i)+
                                               " j: "+Integer.toBinaryString(j)+
                                               " i (shifted): "+Integer.toBinaryString((i >> 3))+
                                               " j (shifted): "+Integer.toBinaryString((j >> 1))+
                                               " index: "+Integer.toBinaryString(index));
                            */

                            byte pixelData = vram.read( (short) Math.min((((gpuMode == 1 || gpuMode == 2) && debug == true) ? 0 : VRAM_START_ADDRESS)+index,vram.getRAMArray().length-1) );
                            //pixelData = 0b001010;

                            byte red = (byte)((pixelData & 0b00110000) >> 4);
                            byte green = (byte)((pixelData & 0b00001100) >> 2);
                            byte blue = (byte)((pixelData & 0b00000011) >> 0);

                            Color c = Color.decode("#"+
                                Integer.toHexString(red*5)+Integer.toHexString(red*5)+
                                Integer.toHexString(green*5)+Integer.toHexString(green*5)+
                                Integer.toHexString(blue*5)+Integer.toHexString(blue*5)
                            );

                            if (scanned && debug) {
                                System.out.println("PixelData: "+ROMLoader.byteToHexString(pixelData)+" Color "+c.toString()+" @ ("+i+","+j+"), Index "+Integer.toHexString(index));
                                System.out.println("PixelData "+Integer.toBinaryString(pixelData)+" R:"+Integer.toBinaryString(red)+" G:"+Integer.toBinaryString(green)+" B:"+Integer.toBinaryString(blue));
                                System.out.println("NextPowerOf2: "+nextPowerOf2);
                            }

                            g.setColor(c);

                            g.fillRect(GPUPixelScale*j, GPUPixelScale*i, GPUPixelScale, GPUPixelScale);
                        }
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
