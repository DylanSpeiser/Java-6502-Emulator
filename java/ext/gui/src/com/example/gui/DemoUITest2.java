package com.example.gui;

//java Program to create multiple frame and set icon to the frame
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
class DemoUITest2 extends JFrame 
{
 
    // frame
    static JFrame f;
 
    // label to display text
    static JLabel l, l1;
 
    // main class
    public static void main(String[] args)
    {
        // create a new frame
        f = new JFrame("frame");
 
        // set layout of frame
        f.setLayout(new FlowLayout());
 
        // create a internal frame
        JInternalFrame in = new JInternalFrame("frame 1",
                                 true, true, true, true);
 
        // create a internal frame
        JInternalFrame in1 = new JInternalFrame("frame 2",
                                   true, true, true, true);
 
        // set icon for internal frames
        in.setFrameIcon(new ImageIcon("f:/gfg.jpg"));
        in1.setFrameIcon(new ImageIcon("f:/gfg.jpg"));
 
        // create a Button
        JButton b = new JButton("button");
        JButton b1 = new JButton("button1");
 
        // create a label to display text
        l = new JLabel("This is a JInternal Frame no 1  ");
        l1 = new JLabel("This is a JInternal Frame no 2  ");
 
        // create a panel
        JPanel p = new JPanel();
        JPanel p1 = new JPanel();
 
        // add label and button to panel
        p.add(l);
        p.add(b);
        p1.add(l1);
        p1.add(b1);
 
        // set visibility internal frame
        in.setVisible(true);
        in1.setVisible(true);
 
        // add panel to internal frame
        in.add(p);
        in1.add(p1);
 
        // add internal frame to frame
        f.add(in);
        f.add(in1);
 
        // set the size of frame
        f.setSize(300, 300);
 
        f.show();
    }
}