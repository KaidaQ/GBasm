package main;

import cpu.CPU;
import memory.Memory;

public class Emulator {
	public static void main(String[] args) {
		Memory memory = new Memory();
		CPU cpu = new CPU();
		
		cpu.setMemory(memory);
		memory.write(0xFFFF, 0x76);
		memory.write(0x120, (byte) 0x04); //simply increment the B register
		
		System.out.println("init emulator;");
		System.out.println("cpu starting: " + Integer.toHexString(cpu.getAF()));
		//testOpcode(cpu, memory);
		runCPU(cpu);

		
		
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
		        System.out.println("CPU halted.");
		        break;
		    }
		    
		    cpu.execute(opcode);  // Execute the opcode
		    
		    instructionCount++;
		    if (instructionCount > 150) { // To avoid infinite loops during testing
		        System.out.println("Instruction count exceeded, breaking the loop.");
		        System.out.println("Final val in B: " + Integer.toHexString(cpu.getB()));
		        
		        break;
		    }
		}
	}
}
