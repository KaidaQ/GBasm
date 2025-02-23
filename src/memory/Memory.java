package memory;
import cpu.CPU;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Memory {
	
	private CPU cpu;
	
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
	    // Handle special I/O register LY (FF44)
	    if (address == 0xFF44) {
	    	return 0x91;
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
}
