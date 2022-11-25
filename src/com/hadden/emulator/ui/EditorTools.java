package com.hadden.emulator.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class EditorTools extends JPanel implements ActionListener
{
	protected JTextArea textArea;
	protected String newline = "\n";
	static final private String PREVIOUS = "previous";
	static final private String UP = "up";
	static final private String NEXT = "next";
	static final private String SOMETHING_ELSE = "other";
	static final private String TEXT_ENTERED = "text";

	public EditorTools()
	{
		super(new BorderLayout());

//Create the toolbar.
		JToolBar toolBar = new JToolBar("Still draggable");
		addButtons(toolBar);
		toolBar.setFloatable(false);
		toolBar.setRollover(true);

//Create the text area used for output.  Request
//enough space for 5 rows and 30 columns.
		textArea = new JTextArea(5, 30);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);

//Lay out the main panel.
		setPreferredSize(new Dimension(450, 130));
		add(toolBar, BorderLayout.PAGE_START);
		add(scrollPane, BorderLayout.CENTER);
	}

	protected void addButtons(JToolBar toolBar)
	{
		JButton button = null;

//first button
		button = makeNavigationButton("Back24", PREVIOUS, "Back to previous something-or-other", "Previous");
		toolBar.add(button);

//second button
		button = makeNavigationButton("Up24", UP, "Up to something-or-other", "Up");
		toolBar.add(button);

//third button
		button = makeNavigationButton("Forward24", NEXT, "Forward to something-or-other", "Next");
		toolBar.add(button);

//separator
		toolBar.addSeparator();

//fourth button
		button = new JButton("Another button");
		button.setActionCommand(SOMETHING_ELSE);
		button.setToolTipText("Something else");
		button.addActionListener(this);
		toolBar.add(button);

//fifth component is NOT a button!
		JTextField textField = new JTextField("A text field");
		textField.setColumns(10);
		textField.addActionListener(this);
		textField.setActionCommand(TEXT_ENTERED);
		toolBar.add(textField);
	}

	protected JButton makeNavigationButton(String imageName, String actionCommand, String toolTipText, String altText)
	{
//Look for the image.
		String imgLocation = "images/" + imageName + ".gif";
		URL imageURL = EditorTools.class.getResource(imgLocation);

//Create and initialize the button.
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);

		if (imageURL != null)
		{ // image found
			button.setIcon(new ImageIcon(imageURL, altText));
		}
		else
		{ // no image found
			button.setText(altText);
			System.err.println("Resource not found: " + imgLocation);
		}

		return button;
	}

	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();
		String description = null;

// Handle each button.
		if (PREVIOUS.equals(cmd))
		{ // first button clicked
			description = "taken you to the previous <something>.";
		}
		else if (UP.equals(cmd))
		{ // second button clicked
			description = "taken you up one level to <something>.";
		}
		else if (NEXT.equals(cmd))
		{ // third button clicked
			description = "taken you to the next <something>.";
		}
		else if (SOMETHING_ELSE.equals(cmd))
		{ // fourth button clicked
			description = "done something else.";
		}
		else if (TEXT_ENTERED.equals(cmd))
		{ // text field
			JTextField tf = (JTextField) e.getSource();
			String text = tf.getText();
			tf.setText("");
			description = "done something with this text: " + newline + "  \"" + text + "\"";
		}

		displayResult("If this were a real app, it would have " + description);
	}

	protected void displayResult(String actionDescription)
	{
		textArea.append(actionDescription + newline);
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event dispatch thread.
	 */
	private static void createAndShowGUI()
	{
//Create and set up the window.
		JFrame frame = new JFrame("ToolBarDemo2");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//Add content to the window.
		frame.add(new EditorTools());

//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args)
	{
//Schedule a job for the event dispatch thread:
//creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
//Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI();
			}
		});
	}
}