package cpu;
import memory.Memory;

public class CPU {
	private int A, B, C, D, E, H, L, F; //8 bit registers 
	private int SP, PC; //16 bit registers (AF,BC,DE,HL paired);
	
	private Memory memory;
	
	public CPU() {
		reset();
	}
	
	public void setMemory(Memory memory) {
		this.memory = memory;
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
	
	 // Setter methods for the registers
    public void setA(int value) {
        A = value & 0xFF;  // Ensure the value is 8 bits
    }

    public void setB(int value) {
        B = value & 0xFF;  // Ensure the value is 8 bits
    }

    public void setC(int value) {
        C = value & 0xFF;  // Ensure the value is 8 bits
    }

    public void setD(int value) {
        D = value & 0xFF;  // Ensure the value is 8 bits
    }

    public void setE(int value) {
        E = value & 0xFF;  // Ensure the value is 8 bits
    }

    public void setH(int value) {
        H = value & 0xFF;  // Ensure the value is 8 bits
    }

    public void setL(int value) {
        L = value & 0xFF;  // Ensure the value is 8 bits
    }

    public void setF(int value) {
        F = value & 0xFF;  // Ensure the value is 8 bits
    }

	/**
	 * fetch the instructions
	 * @return
	 */
	public byte fetch() {
		if(PC >= 0x10000) {
			PC = 0x0000;
		}
		return (byte) memory.read(PC++);
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
	        case (byte) 0x02: // LD (BC), A
	            memory.write(getBC(), (byte) A);
	            System.out.println("LD (BC), A executed: (BC) = " + Integer.toHexString(A));
	            break;
	        case (byte) 0x03: // ADD A, B
	            A += B;
	            System.out.println("ADD A, B executed: A = " + Integer.toHexString(A));
	            break;
	        case (byte) 0x05: // SUB A, B
	            A -= B;
	            System.out.println("SUB A, B executed: A = " + Integer.toHexString(A));
	            break;
	        case (byte) 0x3E: // LD A, (d8) - Load immediate value into A (using memory)
	            A = memory.read(PC++);
	            System.out.println("LD A, (d8) executed: A = " + Integer.toHexString(A));
	            break;
	        case (byte) 0xEA: // LD (a16), A
	            int addr = (memory.read(PC++) & 0xFF) | ((memory.read(PC++) & 0xFF) << 8);
	            memory.write(addr, (byte) A);
	            System.out.println("LD (a16), A executed: (a16) = " + Integer.toHexString(addr) + ", A = " + Integer.toHexString(A));
	            break;
	        case (byte) 0x00: // NOP - No operation (commonly used as a placeholder)
	            System.out.println("NOP executed.");
	            break;
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
