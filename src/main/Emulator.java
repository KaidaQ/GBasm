package main;

import cpu.CPU;
import memory.Memory;
import ppu.PPU;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class Emulator {
	public static void main(String[] args) {
		Memory memory = new Memory(); //set virtual ram
		memory.loadROM("pokeRed.gb"); //set to test.gb to test real rom s
		CPU cpu = new CPU();
		cpu.setMemory(memory);

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
		runCPU(cpu, memory);
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
	
		public static void runCPU(CPU cpu, Memory memory) {
		    int instructionCount = 0;
	
		    while (true) {
		    	//Update the LCD scanline before checking IME
		    	if (instructionCount % 2 == 0) {
		    		memory.incrementScanline();
		    	}
		    	
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
	
		        instructionCount++;
		        if (instructionCount > 0xFFFF) { // Limit execution to prevent infinite loops
		            System.out.println("Instruction count exceeded, breaking the loop.");
		            break;
		        }
		    }
		}
}
