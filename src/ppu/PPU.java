package ppu;

import cpu.CPU;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import memory.Memory;

public class PPU extends JPanel {
	private BufferedImage screen;
	private Memory memory;
	
	public PPU(Memory memory) {
		this.memory = memory;
		
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
		return new Dimension(320,240);
	}
	
	public void drawPixel(int x, int y, int color) {
	    if (x >= 0 && x < 160 && y >= 0 && y < 144) { // Ensure pixel is within bounds
	        screen.setRGB(x, y, color);
	    }
	}
	
	public void refreshScreen() {
	    repaint(); // Calls `paintComponent()` to redraw the image
	}
	
	public void drawScanline(int scanline) {
	    int tileMapBase = (memory.read(0xFF40) & 0x08) != 0 ? 0x9C00 : 0x9800; // Tilemap selection
	    int tileDataBase = (memory.read(0xFF40) & 0x10) != 0 ? 0x8000 : 0x8800; // Tile data selection

	    for (int x = 0; x < 160; x++) {
	        int tileX = x / 8; // Tile index X position
	        int tileY = scanline / 8; // Tile index Y position
	        int tileIndex = memory.read(tileMapBase + tileY * 32 + tileX); // Fetch tile index

	        int tileAddress = tileDataBase + (tileIndex * 16); // Get tile memory location

	        int row = scanline % 8; // Get row within tile
	        int byte1 = memory.read(tileAddress + row * 2);
	        int byte2 = memory.read(tileAddress + row * 2 + 1);

	        int col = x % 8; // Column within tile
	        int bit1 = (byte1 >> (7 - col)) & 1;
	        int bit2 = (byte2 >> (7 - col)) & 1;
	        int colorIndex = (bit2 << 1) | bit1; // Combine 2-bit color index

	        int finalColor = switch (colorIndex) {
	            case 0 -> 0xFFFFFF; // White
	            case 1 -> 0xAAAAAA; // Light gray
	            case 2 -> 0x555555; // Dark gray
	            case 3 -> 0x000000; // Black
	            default -> 0xFF00FF; // Magenta (error)
	        };

	        drawPixel(x, scanline, finalColor);
	    }
	}


	public void renderScanline() {
	    int lcdc = memory.read(0xFF40); // Read LCDC register

	    if ((lcdc & 0x80) == 0) { // Check if LCD is OFF (bit 7 = 0)
	        System.out.println("🛑 LCD is OFF, skipping scanline render.");
	        return; // Don't render anything if LCD is OFF
	    }

	    if ((lcdc & 0x01) != 0) { 
	        renderBackground(); // Background enabled
	    }

	    if ((lcdc & 0x20) != 0) { 
	        renderWindow(); // Window enabled
	    }

	    if ((lcdc & 0x02) != 0) { 
	        renderSprites(); // Sprites enabled
	    }

	    repaint(); // Update the screen
	}

	
	public void updateScanline(int scanline) {
	    int lcdc = memory.read(0xFF40);

	    if ((lcdc & 0x80) == 0) { // Bit 7 of LCDC must be 1
	        if (scanline == 0) { // Log only once per frame
	            System.out.println("🛑 LCD is OFF, skipping frame rendering.");
	        }
	        return;
	    }

	    if (scanline < 144) { // Only render visible scanlines
	        renderScanline();
	    }

	    if (scanline == 143) { // Refresh once per frame
	        repaint();
	    }
	}



	private void renderWindow() {
	    int tileMapBase = (memory.read(0xFF40) & 0x40) != 0 ? 0x9C00 : 0x9800; // Window tilemap
	    int tileDataBase = (memory.read(0xFF40) & 0x10) != 0 ? 0x8000 : 0x8800; // Tile data
	    int windowX = memory.read(0xFF4B) - 7; // Window X (offset by 7)
	    int windowY = memory.read(0xFF4A); // Window Y
	    int scanline = memory.read(0xFF44); // Current scanline

	    if (scanline < windowY) return; // Window hasn't started yet

	    for (int x = 0; x < 160; x++) {
	        if (x < windowX) continue; // Only draw past Window X

	        int tileX = (x - windowX) / 8;
	        int tileY = (scanline - windowY) / 8;
	        int tileIndex = memory.read(tileMapBase + tileY * 32 + tileX);
	        int tileAddress = tileDataBase + (tileIndex * 16);

	        int row = (scanline - windowY) % 8;
	        int byte1 = memory.read(tileAddress + row * 2);
	        int byte2 = memory.read(tileAddress + row * 2 + 1);

	        int col = (x - windowX) % 8;
	        int bit1 = (byte1 >> (7 - col)) & 1;
	        int bit2 = (byte2 >> (7 - col)) & 1;
	        int colorIndex = (bit2 << 1) | bit1;

	        int finalColor = switch (colorIndex) {
	            case 0 -> 0xFFFFFF;
	            case 1 -> 0xAAAAAA;
	            case 2 -> 0x555555;
	            case 3 -> 0x000000;
	            default -> 0xFF00FF;
	        };

	        drawPixel(x, scanline, finalColor);
	    }
	}


	private void renderBackground() {
	    int tileMapBase = (memory.read(0xFF40) & 0x08) != 0 ? 0x9C00 : 0x9800; // Tilemap selection
	    int tileDataBase = (memory.read(0xFF40) & 0x10) != 0 ? 0x8000 : 0x8800; // Tile data selection
	    int scrollX = memory.read(0xFF43); // Scroll X
	    int scrollY = memory.read(0xFF42); // Scroll Y
	    int scanline = memory.read(0xFF44); // Current scanline

	    for (int x = 0; x < 160; x++) {
	        int tileX = (x + scrollX) / 8;
	        int tileY = (scanline + scrollY) / 8;
	        int tileIndex = memory.read(tileMapBase + tileY * 32 + tileX); // Fetch tile index
	        int tileAddress = tileDataBase + (tileIndex * 16); // Get tile memory location

	        int row = (scanline + scrollY) % 8;
	        int byte1 = memory.read(tileAddress + row * 2);
	        int byte2 = memory.read(tileAddress + row * 2 + 1);

	        int col = (x + scrollX) % 8;
	        int bit1 = (byte1 >> (7 - col)) & 1;
	        int bit2 = (byte2 >> (7 - col)) & 1;
	        int colorIndex = (bit2 << 1) | bit1;

	        int finalColor = switch (colorIndex) {
	            case 0 -> 0xFFFFFF; // White
	            case 1 -> 0xAAAAAA; // Light gray
	            case 2 -> 0x555555; // Dark gray
	            case 3 -> 0x000000; // Black
	            default -> 0xFF00FF;
	        };

	        drawPixel(x, scanline, finalColor);
	    }
	}

	private void renderSprites() {
	    int lcdc = memory.read(0xFF40);
	    boolean tallSprites = (lcdc & 0x04) != 0;
	    int spriteSize = tallSprites ? 16 : 8;

	    for (int i = 0; i < 40; i++) { // 40 sprites max
	        int spriteAddress = 0xFE00 + (i * 4);
	        int yPos = memory.read(spriteAddress) - 16;
	        int xPos = memory.read(spriteAddress + 1) - 8;
	        int tileIndex = memory.read(spriteAddress + 2);
	        int attributes = memory.read(spriteAddress + 3);

	        boolean flipY = (attributes & 0x40) != 0;
	        boolean flipX = (attributes & 0x20) != 0;

	        int scanline = memory.read(0xFF44);
	        if (scanline < yPos || scanline >= yPos + spriteSize) continue; // Out of range

	        int row = flipY ? (spriteSize - 1 - (scanline - yPos)) : (scanline - yPos);
	        int tileAddress = 0x8000 + (tileIndex * 16);

	        int byte1 = memory.read(tileAddress + row * 2);
	        int byte2 = memory.read(tileAddress + row * 2 + 1);

	        for (int col = 0; col < 8; col++) {
	            int bit1 = (byte1 >> (flipX ? col : (7 - col))) & 1;
	            int bit2 = (byte2 >> (flipX ? col : (7 - col))) & 1;
	            int colorIndex = (bit2 << 1) | bit1;
	            if (colorIndex == 0) continue; // Color 0 is transparent

	            int finalColor = switch (colorIndex) {
	                case 1 -> 0xAAAAAA;
	                case 2 -> 0x555555;
	                case 3 -> 0x000000;
	                default -> 0xFFFFFF;
	            };

	            int pixelX = xPos + col;
	            if (pixelX >= 0 && pixelX < 160) {
	                drawPixel(pixelX, scanline, finalColor);
	            }
	        }
	    }
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
