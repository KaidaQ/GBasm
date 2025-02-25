package main;

import java.awt.Color;

import cpu.CPU;
import memory.Memory;
import ppu.PPU;


public class Emulator {
	public static void main(String[] args) {
		//bios setup
		int[] nintendoLogo = {
			    0xCE, 0xED, 0x66, 0x66, 0xCC, 0x0D, 0x00, 0x0B, 0x03, 0x73, 0x00, 0x83, 0x00, 0x0C, 0x00, 0x0D,
			    0x00, 0x08, 0x11, 0x1F, 0x88, 0x89, 0x00, 0x0E, 0xDC, 0xCC, 0x6E, 0xE6, 0xDD, 0xDD, 0xD9, 0x99,
			    0xBB, 0xBB, 0x67, 0x63, 0x6E, 0xEC, 0xCC, 0xDD, 0xDC, 0x99, 0x9F, 0xBB, 0xB9, 0x33, 0x3E
			};
		
		Memory memory = new Memory(); //set virtual ram
		memory.loadROM("test.gb"); //set to test.gb to test real roms
		
		memory.write(0xFF40, 0x91); // Force LCD ON for debugging
		System.out.println("🔧 Manually enabled LCD for debugging.");

		
		CPU cpu = new CPU();
		cpu.setMemory(memory);
		
		PPU ppu = new PPU(memory);
		memory.setPPU(ppu);
		
		for (int i = 0; i < nintendoLogo.length; i++) {
		    memory.write(0x8010 + i, nintendoLogo[i]); // Write logo data to VRAM
		}
		
		System.out.println("✅ Nintendo Logo Loaded into VRAM");
		
		for (int i = 0x0104; i <= 0x0133; i++) {
		    System.out.print(Integer.toHexString(memory.read(i)) + " ");
		}
		System.out.println();
		
		for (int i = 0x8000; i < 0x8100; i++) {
		    System.out.print(Integer.toHexString(memory.read(i)) + " ");
		}
		System.out.println();

		/*
		 * memory.write(0x40, 0x3E); memory.write(0x41, 0x55); memory.write(0x42, 0xC9);
		 */

		System.out.println("init emulator;");
		System.out.println("🔍 Initial Stack Pointer (SP): " + Integer.toHexString(cpu.getSP()));
		System.out.println("cpu starting: " + Integer.toHexString(cpu.getAF()));
		
		
		//Enable the vblank interrupt.
		System.out.println("✅ Enabling VBlank Interrupt in IE (0xFFFF)");
		memory.write(0xFFFF, memory.read(0xFFFF) | 0x01);
		
		//enable the IME to debug
		cpu.setIME(true);
		
		/* testInterrupts(cpu,memory); */
		runCPU(cpu, memory, ppu);
		//testOpcode(CPU, memory);
		/* runCPU(cpu); */
		
	}
	
	public static void testInterrupts(CPU cpu, Memory memory) {
	    cpu.triggerVBlank();
	    System.out.println("IE: " + Integer.toHexString(memory.read(0xFFFF)));
	    System.out.println("IF: " + Integer.toHexString(memory.read(0xFF0F)));

	    for (int i = 0; i < 500; i++) {  // ✅ Reduce step count for debugging
	        cpu.step();
	        
	        // Print stack memory periodically
	        if (i % 50 == 0) { // Every 50 steps
	            System.out.println("Stack Dump (SP = " + Integer.toHexString(cpu.getSP()) + "): ");
	            for (int j = 0; j < 6; j++) {
	                System.out.println("SP+" + j + " = " + Integer.toHexString(memory.read(cpu.getSP() + j) & 0xFF));
	            }
	        }
	    }
	}

	public static void ranInstruct(CPU cpu, Memory memory) {
		for(int i = 0x100; i < 0xFFFF; i++) {
			if(Math.floor(Math.random() * 5)> 3) {
				memory.write(i, 0x04);
			}
			if(Math.floor(Math.random() * 5)> 3) {
				memory.write(i, 0x80);
			}
		}
	}
	
	public static void testOpcode(CPU cpu, Memory memory) {
		memory.write(0x0100, (byte) 0x03E);
		memory.write(0x101, (byte) 0x20);
		
		cpu.setBC(0x1234);
		
		cpu.setA(0x00);
		
		cpu.step();
		
		System.out.println("Final val in A: " + Integer.toHexString(cpu.getAF() >> 8));
	}
	
		public static void runCPU(CPU cpu, Memory memory, PPU ppu) {
		    int instructionCount = 0;
		    long lastFrameTime = System.nanoTime();
		    
		    while (true) {
		    	long current = System.nanoTime();
		    	long elapsedTime = current - lastFrameTime;
		    	
		    	
		    	if (elapsedTime >= 16_700_000) {
		    		lastFrameTime = current;
		    		memory.incrementScanline();
		    		int ly = memory.read(0xFF44);
		    		ppu.drawPixel(instructionCount, ly, ly);
		    	}
		    	
				/* old code
				 * //Update the LCD scanline before checking IME if (instructionCount % 2 == 0)
				 * { memory.incrementScanline(); int ly = memory.read(0xFF44);
				 * 
				 * ppu.drawScanline(ly, (ly % 2 == 0) ? Color.black.getRGB() :
				 * Color.WHITE.getRGB()); }
				 */
		    	
		        System.out.println("📌 IME: " + cpu.isIME() + 
		                " | IF: " + Integer.toBinaryString(memory.read(0xFF0F)) +
		                " | IE: " + Integer.toBinaryString(memory.read(0xFFFF)));
		        System.out.println();
		        
		        
		        if (memory.read(0xFFFF) == 0) {
		            System.out.println("🚨 IE (0xFFFF) was reset to 0! Re-enabling...");
		            memory.write(0xFFFF, memory.read(0xFFFF) | 0x01);
		        }
		        
		        // Check for interrupts before fetching next instruction
		        if (cpu.isIME()) { // Interrupt Master Enable must be true
		            int interruptFlags = memory.read(0xFF0F); // IF Register (Interrupt Request)
		            int enabledInterrupts = memory.read(0xFFFF); // IE Register (Interrupt Enable)
	
		            int pendingInterrupts = interruptFlags & enabledInterrupts;
		            
		            if (pendingInterrupts != 0) {
		            	System.out.println("⚡ Interrupt Requested: " + Integer.toBinaryString(pendingInterrupts));
		                
		                for (int i = 0; i < 5; i++) {
		                    if ((pendingInterrupts & (1 << i)) != 0) { 
		                        cpu.handleInterrupt(i); // Handle the highest-priority interrupt
		                        break;
		                    }
		                }
		            }
		        }
	
		        // Fetch and execute the next instruction
		        byte opcode = cpu.fetch();  
		        cpu.execute(opcode);
		        
		        
		        try {
		        	Thread.sleep(5);
		        } catch (InterruptedException e) {
		        	e.printStackTrace();
		        }
		        
		        instructionCount++;
		        if (instructionCount > 2200000) { // Limit execution to prevent infinite loops
		            System.out.println("Instruction count exceeded, breaking the loop.");
		            break;
		        }
		    }
		}
}
