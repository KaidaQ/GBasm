package ppu;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PPU extends JPanel {
	private BufferedImage screen;
	
	public PPU() {
		screen = new BufferedImage(160, 144, BufferedImage.TYPE_INT_RGB);
		
		JFrame frame = new JFrame("Game Boy Emulator - PPU");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.add(this);
		frame.pack();
		frame.setVisible(true);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(160,144);
	}
	
	public void drawScanline(int scanline, int color) {
		if (scanline < 144) {
			for (int x = 0; x < 160; x++) {
				screen.setRGB(x, scanline, color);
			}
			repaint();
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(screen,0,0,null);
		
	}
}
