package com.example.gui.docking;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class ElegantPanel extends JPanel
{
	private static final Color GRAD_MID = new Color(168, 203, 239);
	private static final Color GRAD_START = new Color(10, 36, 106);
	private Dockable dockable;
	private JLabel titleLabel;

	public ElegantPanel(String title)
	{
		super.add(getTitleLabel());
		setTitle(title);
		dockable = getDockable();
	}

	public void doLayout()
	{
		Insets insets = getInsets();
		int w = getWidth() - insets.left - insets.right;
		int h = getHeight() - insets.top - insets.bottom;
		getTitleLabel().setBounds(insets.left + 3, insets.top, w, 25);
	}

	public Dockable getDockable()
	{
		if (dockable == null)
			dockable = new DockableImpl(this, getTitleLabel());
		return dockable;
	}

	public String getTitle()
	{
		return getTitleLabel().getText();
	}

	private JLabel getTitleLabel()
	{
		if (titleLabel != null)
			return titleLabel;

		titleLabel = new JLabel();
		titleLabel.setForeground(Color.white);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN));
		return titleLabel;
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Insets in = getInsets();
		int mid = getWidth() / 2;
		int y = in.top + 13;
		int farRight = getWidth() - in.right;
		int w = farRight - in.left;

		GradientPaint firstHalf = new GradientPaint(in.left, y, GRAD_START, mid, y, GRAD_MID);
		GradientPaint secondHalf = new GradientPaint(mid, y, GRAD_MID, farRight, y, getBackground());

		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(firstHalf);
		g2.fillRect(in.left, in.top, w / 2, 25);
		g2.setPaint(secondHalf);
		g2.fillRect(mid - 1, in.top, w / 2, 25);

		g.setColor(getBackground().brighter());
		g.drawLine(in.left, in.top, farRight, in.top);
		g.drawLine(in.left, in.top, in.left, in.top + 25);

		g.setColor(getBackground().darker());
		g.drawLine(in.left, in.top + 25, farRight, in.top + 25);
	}

	public void setLayout(LayoutManager mgr)
	{
		// do nothing. we handle our own layout.
	}

	public void setTitle(String title)
	{
		if (title == null)
			title = "";
		title = title.trim();
		getTitleLabel().setText(title);
	}
}
