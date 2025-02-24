package memory;
import cpu.CPU;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Memory {
	
	private CPU cpu;
	private int joypadState = 0xFF;  // P1/JOYP Register (0xFF00)
	private int timerCounter = 0;    // TIMA (0xFF05)
	private int timerModulo = 0;     // TMA (0xFF06)
	private int timerControl = 0;    // TAC (0xFF07)
	private int interruptFlags = 0;  // IF Register (0xFF0F)
	private int lcdControl = 0;      // LCDC (0xFF40)
	private int lcdStatus = 0;       // STAT (0xFF41)
	private int scrollY = 0;         // SCY (0xFF42)
	private int scrollX = 0;         // SCX (0xFF43)
	private int scanline = 0;        // LY (0xFF44)
	private int interruptEnable = 0; // IE Register (0xFFFF)
	private int dividerRegister = 0; // Divider Register (0xFF04)
	
	public void setCPU(CPU cpu) {
		this.cpu = cpu;
	}
	
	private byte[] memory = new byte[0x10000]; //64kb of memory
	
	private int romBank = 1;
	private int ramBank = 0;
	
	private boolean ramEnabled = false;
	private boolean bankingMode = false;
	
	private int mbcType = 0;
	
	private int IE = 0;
	private int IF= 0;
	
	public void loadROM(String filePath) {
		try(FileInputStream fis = new FileInputStream(new File(filePath))) {
			int bytesRead = fis.read(getMemory(), 0x0000, Math.min(fis.available(), 0x8000));
			System.out.println("Loaded " + bytesRead + " bytes into available memory.");
			
			mbcType = getMemory()[0x0147] & 0xFF;
			//detect MBC type at $0147;
			
			switch(mbcType) {
			case 0x00:
				System.out.println("ROM uses NO MBC (Simple 32kb ROM).");
				break;
			case 0x01: case 0x02: case 0x03:
				System.out.println("ROM uses MBC1");
				mbcType = 1;
				break;
			case 0x0F: case 0x10: case 0x11: case 0x12: case 0x13:
				System.out.println("ROM uses MBC3.");
				mbcType = 3;
				break;
			default :
				System.out.println("Unknown or unsupported MBC Type: " + Integer.toHexString(mbcType) + " is this a valid GB rom?");
			} 
			
		} catch (IOException e) {
			System.err.println("Error loading ROM: " + e.getMessage());
		}
	}

	public int read(int address) {	
	    //IO Switch Cases
	    switch (address) {
	    case 0xFF00: //joypad input
	    	return handleJoypadRead();
	    
	    case 0xFF04: //Divider register
	    	return (int) ((System.nanoTime() / 256) & 0xFF);
	    	
        case 0xFF05: // Timer Counter
            return timerCounter;

        case 0xFF06: // Timer Modulo
            return timerModulo;

        case 0xFF07: // Timer Control
            return timerControl;

        case 0xFF0F: // Interrupt Flags
            return interruptFlags;

        case 0xFF40: // LCD Control
            return lcdControl;

        case 0xFF41: // LCD Status
            return lcdStatus;

        case 0xFF42: // Background Scroll Y
            return scrollY;

        case 0xFF43: // Background Scroll X
            return scrollX;

        case 0xFF44: // LY (Current Scanline)
        	System.out.println("📖 Read LY ($FF44) = " + Integer.toHexString(scanline));
            return scanline;

        case 0xFFFF: // Interrupt Enable
            return interruptEnable;
            
	    }
	    
	    
		//handle IE and IF registers-
		if (address == 0xFFFF) return IE;
		if (address == 0xFF0F) return IF;
		
		//read bytes
		if(address >= 0 && address < getMemory().length) {
			return getMemory()[address] & 0xFF;
		} else if (address >= 0x4000 && address <= 0x7FFF) {
			//banked rom header
			int newAddress = (romBank * 0x4000) + (address - 0x4000);
			return getMemory()[newAddress] & 0xFF;
		} else if (address >= 0xA000 && address <= 0xBFFF && ramEnabled) {
			//banked ram read
			int ramAddr = (ramBank * 0x2000) + (address - 0xA000);
			return getMemory()[ramAddr] & 0xFF;
		}
		return 0xFF; // unmapped memory
	}
	
	public void write(int address, int value) {
	    if (address == 0xFFFF) {
	        System.out.println("⚠️ Writing to IE Register (0xFFFF) | Old: " + Integer.toBinaryString(interruptEnable) +
	                           " -> New: " + Integer.toBinaryString(value));
	    }
	    if (address == 0xFF0F) {
	        System.out.println("⚠️ Writing to IF Register (0xFF0F) | Old: " + Integer.toBinaryString(interruptFlags) +
	                           " -> New: " + Integer.toBinaryString(value));
	    }
	    
		//io registers
		switch (address) {
        case 0xFF00: // Joypad Input
            joypadState = value;
            return;

        case 0xFF04: // Divider Register (Reset)
            dividerRegister = 0;
            return;

        case 0xFF05: // Timer Counter
            timerCounter = value;
            return;

        case 0xFF06: // Timer Modulo
            timerModulo = value;
            return;

        case 0xFF07: // Timer Control
            timerControl = value;
            return;

        case 0xFF0F: // Interrupt Flags
            interruptFlags = value;
            return;

        case 0xFF40: // LCD Control
            lcdControl = value;
            return;

        case 0xFF41: // LCD Status
            lcdStatus = value;
            return;

        case 0xFF42: // Background Scroll Y
            scrollY = value;
            return;

        case 0xFF43: // Background Scroll X
            scrollX = value;
            return;

        case 0xFF44: // LY (Current Scanline) (Writing resets it)
            scanline = 0;
            return;

        case 0xFFFF: // Interrupt Enable
            interruptEnable = value;
            return;
    }
			 // Detect and log stack overwrites
	    if (address == cpu.getSP() || address == cpu.getSP() + 1) { 
	        System.out.println("🚨 Stack overwrite detected at " + Integer.toHexString(address) +
	            " | Value: " + Integer.toHexString(value) + 
	            " | Written by instruction at PC: " + Integer.toHexString(cpu.getPC()));

	        // Dump stack before the write
	        System.out.println("⚠️ Stack Dump Before Overwrite:");
	        for (int i = 0; i < 6; i++) {
	            int stackAddr = cpu.getSP() + i;
	            if (stackAddr < memory.length) {  // Prevent out-of-bounds access
	                System.out.println("SP+" + i + " = " + Integer.toHexString(memory[stackAddr] & 0xFF));
	            }
	        }
	    }

	    // Handle I/O register writes separately
	    if (address >= 0xFF00 && address <= 0xFFFF) {
	        System.out.println("⚠️ Writing to I/O register: " + Integer.toHexString(address) + 
	                           " | Value: " + Integer.toHexString(value));
	    }

	    // Normal memory write (ensure address is valid)
	    if (address >= 0 && address < memory.length) {
	        memory[address] = (byte) (value & 0xFF);
	    } else {
	        System.out.println("Invalid write access at address: " + Integer.toHexString(address) + 
	                           " with write value: " + Integer.toHexString(value));
	    }
		
		//handle IE and IF registers-
		if (address == 0xFFFF) IE = value & 0xFF;
		if (address == 0xFF0F) IF = value & 0xFF;
		
		// Check stack memory
		if (address == 0xFFFF && value == 0) {
		    System.out.println("⚠️ Prevented overwriting IE register with zero!");
		    return; // Ignore accidental writes to IE
		}

		
		if (address >= 0xC000 && address <= 0xFFFF) {
		    System.out.println("🛑 Stack write detected at: " + Integer.toHexString(address) + 
		                       " | Value: " + Integer.toHexString(value) + 
		                       " | Written by instruction at PC: " + Integer.toHexString(cpu.getPC()) + 
		                       " | Current SP: " + Integer.toHexString(cpu.getSP()));
		}

		
		if (address == 0x2040) {
		    System.out.println("🚨 Writing to address 0x2040 | Value: " + Integer.toHexString(value & 0xFF));
		}
		
		if (address == cpu.getSP() || address == cpu.getSP() + 1) { 
		    System.out.println("🚨 Stack overwrite detected at " + Integer.toHexString(address) +
		        " | Value: " + Integer.toHexString(value) + 
		        " | Written by instruction at PC: " + Integer.toHexString(cpu.getPC()));
		}

		if (address >= 0xFF00 && address <= 0xFFFF) {
		    System.out.println("⚠️ Writing to I/O register: " + Integer.toHexString(address) + " | Value: " + Integer.toHexString(value));
		}
		
		// RAM Enable Register
		if (address >= 0x0000 && address <= 0x1FFF) {
			ramEnabled = (value & 0x0F) == 0x0A;
		} else if (address >= 0x2000 && address <= 0x3FFF) {
			// ROM Bank select
			romBank = value & 0x1F;
			if (romBank == 0) romBank = 1; //bank 0 is == to 1 in MBC1
		} else if (address >= 0x4000 && address <= 0x5FFF) {
			//RAM Bank select OR Upper ROM Bank Bits
			if (bankingMode) {
				ramBank = value & 0x03; // 2-bit RAM Bank no.
			} else {
				romBank |= (value & 0x03) << 5; //extend ROM bank 
			}
		} else if (address >= 0x6000 && address <= 0x7FFF) {
			//Banking mode select
			bankingMode = (value & 0x01) != 0;
		} else if (address >= 0xA000 && address <= 0xBFFF && ramEnabled) {
			//RAM bank write
			int ramAddr = (ramBank * 0x2000) + (address - 0xA000);
			getMemory()[ramAddr] = (byte) value;
		} 
		
		if(mbcType == 3) {
			if(address >= 0x0000 && address <= 0x1FFF) {
				//enable or disable ram
				ramEnabled = (value & 0x0F) == 0x0A;
			} else if (address >= 0x2000 && address <= 0x3FFF) {
				//rom bank select (7bit, 0x01-0x7F)
				romBank = value & 0x7F;
				if (romBank == 0) romBank = 1;
				System.out.println("MBC3 ROM Bank switched to: " + romBank);
			} else if (address >= 0x4000 && address >= 0x5FFF) {
				//Ram bank sel or RTC reg sel
				ramBank = value & 0x0F;
				if (ramBank >= 0x08) {
					System.out.println("MBC3 selected RTC register: " + ramBank);
				} else {
					System.out.println("MBC3 RAM Bank switched to: " + ramBank);
				}
			} else if (address >= 0x6000 && address <= 0x7FFF) {
				//latch clock data - implement later
				System.out.println("MBC3 RTC Latch: " + value);
			} else if (address >= 0xA000 && address <= 0xBFFF && ramEnabled) {
				int ramAddr = (ramBank * 0x2000) + (address - 0xA000);
				getMemory()[ramAddr] = (byte) value;
			}
		}
		
		//edge case to default memory write-
		else if(address >= 0 && address < getMemory().length) {
			getMemory()[address] = (byte) (value & 0xFF);
		} else {
			System.out.println("Invalid write access at address: " + Integer.toHexString(address) + " with write value: " + Integer.toHexString(value));
		}
	}

	public byte[] getMemory() {
		return memory;
	}

	public void setMemory(byte[] memory) {
		this.memory = memory;
	}
	
	private int handleJoypadRead() {
	    int result = 0xFF;

	    // Simulated button states (example: A and Start are pressed)
	    boolean A = false;
	    boolean B = false;
	    boolean Select = false;
	    boolean Start = true;
	    boolean Right = false;
	    boolean Left = false;
	    boolean Up = false;
	    boolean Down = false;

	    // Check which buttons are selected
	    if ((joypadState & 0x10) == 0) { // Action Buttons (A, B, Select, Start)
	        result &= ~(A ? 0x1 : 0);
	        result &= ~(B ? 0x2 : 0);
	        result &= ~(Select ? 0x4 : 0);
	        result &= ~(Start ? 0x8 : 0);
	    }

	    if ((joypadState & 0x20) == 0) { // D-Pad (Up, Down, Left, Right)
	        result &= ~(Right ? 0x1 : 0);
	        result &= ~(Left ? 0x2 : 0);
	        result &= ~(Up ? 0x4 : 0);
	        result &= ~(Down ? 0x8 : 0);
	    }

	    return result;
	}
	
	public void incrementScanline() {
		boolean debug = true;
		scanline = (scanline + 1) % 154;
		System.out.println("🔄 Scanline updated: LY = " + Integer.toHexString(scanline));
		
		if(scanline == 144) {
			if(debug) {
				cpu.setIME(true);
				System.out.println("Forcibly setting IME to enable.");
			}
			
			cpu.triggerVBlank();
			System.out.println("⚡ VBlank Interrupt Set: IF = " + Integer.toHexString(interruptFlags));
		}
	}
}
