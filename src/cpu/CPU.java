package cpu;

public class CPU {
	private int A, B, C, D, E, H, L, F; //8 bit registers 
	private int SP, PC; //16 bit registers (AF,BC,DE,HL paired);
	
	private byte[] memory = new byte[0xFFFF]; //64KB of simulated "ram"
	
	public CPU() {
		reset();
	}
	
	/**
	 * init cpu state
	 */
	public void reset() {
		A = 0x01;
		F = 0xB0;
		B = 0x00;
		C = 0x13;
		D = 0x00;
		E = 0xD8;
		H = 0x01;
		L = 0x4D;
		//8 bit regs
		
		SP = 0xFFFE;
		PC = 0x0100; // all execution starts here
		//16 bit regs
		
		memory[0x0100] = (byte) 0x76; //test the halt
		memory[0x0101] = (byte) 0x01; //test ld
		memory[0x0101] = (byte) 0x03; //test add
		memory[0x0101] = (byte) 0x05; //test sub
	}
	
	//get paired 16bit regs via getter functs
	public int getAF() {
		return ((A & 0xFF) << 8) | (F & 0xFF);
	}
	public int getBC() {
		return ((B & 0xFF) << 8) | (C & 0xFF);
	}
	public int getDE() {
		return ((D & 0xFF) << 8) | (E & 0xFF);
	}
	public int getHL() {
		return ((H & 0xFF) << 8) | (L & 0xFF);
	}
	
	/**
	 * fetch the instructions
	 * @return
	 */
	public byte fetch() {
		return memory[PC++];
	}
	
	/**
	 * decode and execute the instruction
	 * @param opcode
	 */
	public void execute(byte opcode) {
		switch(opcode) {
		case (byte) 0x76: //"HALT"
			System.out.println("Halt instruction executed.");
			break;
		case (byte) 0x01: // LD A, B
			A = B;
			System.out.println("LD A, B executed: A = " + Integer.toHexString(A));
			break;
		case (byte) 0x03: // ADD A, B
			A += B;
			System.out.println("ADD A, B executed: A = " + Integer.toHexString(A));
			break;
		case (byte) 0x05: // SUB A, B
			A -= B;
			System.out.println("SUB A, B executed: A = " + Integer.toHexString(A));
			break;	
		case (byte) 0x00: //"Unknown / NOP"
			System.out.println("Unknown or NOP copcode 0x00!");
			default:
				System.out.println("Unknown opcode: 0x" + Integer.toHexString(opcode & 0xFF));
		}
	}
	
	/**
	 * emulate a cpu cycle
	 */
	public void step() {
		byte opcode = fetch();
		execute(opcode);
	}
	
	//set 16 bit regs
	public void setAF(int val) {
		A = (val >> 8) & 0xFF;
		F = val & 0xFF;
	}
	public void setBC(int val) {
		B = (val >> 8) & 0xFF;
		C = val & 0xFF;
	}
	public void setDE(int val) {
		D = (val >> 8) & 0xFF;
		E = val & 0xFF;
	}	
	public void setHL(int val) {
		H = (val >> 8) & 0xFF;
		L = val & 0xFF;
	}
}
