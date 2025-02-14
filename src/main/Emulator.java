package main;

import cpu.CPU;
import memory.Memory;

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
		System.out.println("cpu starting: " + Integer.toHexString(cpu.getAF()));
		
		testInterrupts(cpu, memory);
		
		//testOpcode(CPU, memory);
		/* runCPU(cpu); */
	}
	
	public static void testInterrupts(CPU cpu, Memory memory) {
		cpu.triggerVBlank();
		System.out.println("IE: " + Integer.toHexString(memory.read(0xFFFF)));
		System.out.println("IF: " + Integer.toHexString(memory.read(0xFF0F)));

		for(int i = 0; i < 30; i++) {
			cpu.step();
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
	
	public static void runCPU(CPU cpu) {
		int instructionCount = 0;
		while (true) {
		    byte opcode = cpu.fetch();  // Fetch the next instruction
		    if (opcode == (byte) 0x76) { // HALT condition
		        System.out.println("CPU halted at address: " + "0x" + Integer.toHexString(instructionCount));
		        break;
		    }
		    
		    cpu.execute(opcode);  // Execute the opcode
		    
		    instructionCount++;
		    if (instructionCount > 0x35) { // To avoid infinite loops during testing
		        System.out.println("Instruction count exceeded, breaking the loop.");
		        System.out.println("Final val in B: " + "0x" + Integer.toHexString(cpu.getB()));
		        
		        break;
		    }
		}
	}
}
