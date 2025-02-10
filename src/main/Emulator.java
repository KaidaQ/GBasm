package main;

import cpu.CPU;
import memory.Memory;

public class Emulator {
	public static void main(String[] args) {
		Memory memory = new Memory();
		CPU cpu = new CPU();
		
		System.out.println("init emulator;");
		System.out.println("cpu starting: " + Integer.toHexString(cpu.getAF()));

		while(true) {
			if (cpu.fetch() == (byte) 0x76) {
				System.out.println("CPU halted.");
				break;
			}
		}
		
	}
}
