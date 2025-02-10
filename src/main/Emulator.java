package main;

import cpu.CPU;

public class Emulator {
	public static void main(String[] args) {
		CPU cpu = new CPU();
		System.out.println("init emulator;");
		System.out.println("cpu starting: " + Integer.toHexString(cpu.getAF()));
		
		cpu.step();
	}
}
