package cpu;
import memory.Memory;

public class CPU {
	private int A, B, C, D, E, H, L, F; //8 bit registers 
	private int SP, PC; //16 bit registers (AF,BC,DE,HL paired);
	private boolean IME = false;
	
	private Memory memory;
	
	public CPU() {
		reset();
	}
	
	public void setMemory(Memory memory) {
		this.memory = memory;
	}
	
	//Interrupts
	private void checkInterrupts() {
		int interruptFlags = memory.read(0xFF0F); //read IF register
		int enabledInterrupts = memory.read(0xFFFF); //read IE register
		int pending = interruptFlags & enabledInterrupts; //active and enabled interrupts
		
		if(IME & pending != 0) {
			for(int i = 0; i < 5; i++) {
				if ((pending & (1 << i)) != 0) {
					handleInterrupt(i);
					return;
				}
			}
		}
		
	}
	
	private void handleInterrupt(int interruptType) {
		System.out.println("Handling Interrupt: " + interruptType);
		IME = false; //disable interrupts
		memory.write(0xFF0F, memory.read(0xFF0F) & ~(1 << interruptType)); //clear the IF flag
		
		//push PC onto stack
		SP -= 2;
		memory.write(SP + 1,  (PC >> 8) & 0xFF); //high byte
		memory.write(SP, PC & 0xFF); //low byte
		
		switch (interruptType) {
		case 0: // VBlank
			PC = 0x40;
			System.out.println("Jumping to Interrupt Vector: " + "0x" + Integer.toHexString(PC));
			break;
		case 1: // LCD
			PC = 0x48;
			break;
		case 2: // Timer
			PC = 0x50;
			break;
		case 3: // Serial
			PC = 0x58;
			break;
		case 4: // Joy-Pad
			PC = 0x60;
			break;
		}
		System.out.println("Interrupt Handled: " + interruptType + " Jumping to " + Integer.toHexString(PC).toUpperCase());
	}
	
	//VBlank Interrupt
	public void triggerVBlank() {
		memory.write(0xFFFF, memory.read(0xFFFF) | 0x01);
		memory.write(0xFF0F, memory.read(0xFF0F) | 0x01);
		
		IME = true;
		System.out.println("VBlank Interrupt Triggered");
	}
	
	/**
	 * Reset CPU state
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

    //getter methods for the registers
    public int getA() {
        return A;
    }

    public int getB() {
        return B;
    }

    public int getC() {
        return C;
    }

    public int getD() {
        return D;
    }

    public int getE() {
        return E;
    }

    public int getH() {
        return H;
    }

    public int getL() {
        return L;
    }

    public int getF() {
        return F;
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
	public void execute(byte opcode) { //oh god
		int addr;
		int value;
		int result;
	    switch(opcode) {
	        // Placeholder instruction
	        case (byte) 0x00: // NOP - No operation (commonly used as a placeholder)
	            System.out.println("NOP executed at address: " + "$" + Integer.toHexString(PC - 1));
	            break;

	        // CPU stop instructions
	        case (byte) 0x76: // HALT
	            System.out.println("Halt instruction executed.");
	            break;

	        // Load instructions
	        case (byte) 0x78: // LD A, B
	            A = B;
	            System.out.println("LD A, B executed: A = " + Integer.toHexString(A));
	            break;

	        case (byte) 0x02: // LD (BC), A
	            memory.write(getBC(), (byte) A);
	            System.out.println("LD (BC), A executed: (BC) = " + Integer.toHexString(A));
	            break;

	        case (byte) 0x79: // LD A, C
	            A = C;
	            System.out.println("LD A, C executed.");
	            break;

	        case (byte) 0x7A: // LD A, D
	            A = D;
	            System.out.println("LD A, D executed.");
	            break;

	        case (byte) 0x7B: // LD A, E
	            A = E;
	            System.out.println("LD A, E executed.");
	            break;

	        case (byte) 0x7C: // LD A, H
	            A = H;
	            System.out.println("LD A, H executed.");
	            break;

	        case (byte) 0x7D: // LD A, L
	            A = L;
	            System.out.println("LD A, L executed.");
	            break;

	        case (byte) 0x7E: // LD A, (HL)
	            A = memory.read(getHL());
	            System.out.println("LD A, (HL) executed.");
	            break;
	        case (byte) 0x66:
	        	H = memory.read(getHL()); //LD H, (HL)
	        	System.out.println("LD H, (HL) executed : " + Integer.toHexString(H));
	        	break;
	        case (byte) 0x77: // LD (HL), A
	            memory.write(getHL(), A);
	            System.out.println("LD (HL), A executed.");
	            break;
	            //0xED is a invalid operation in the gameboy z80, but a full z80 would use it. this is just here so its outputting "illegal action : 0xED"
	        case (byte) 0xED:
	        	System.out.println("0xED appears in every Game Boy rom, at byte $105.");
	        	break;
	        	
	        case (byte) 0x3E: // LD A, (d8) - Load immediate value into A (using memory)
	            A = memory.read(PC++);
	            System.out.println("LD A, (d8) executed: A = " + Integer.toHexString(A));
	            break;
	            
	        case (byte) 0x06:
	        	B = memory.read(PC++);
	        	System.out.println("LD B, (d8) executed: B = " + Integer.toHexString(B));
	        	break;
	        	
 	        case (byte) 0xEA: // LD (a16), A
	            addr = (memory.read(PC++) & 0xFF) | ((memory.read(PC++) & 0xFF) << 8);
	            memory.write(addr, (byte) A);
	            System.out.println("LD (a16), A executed: (a16) = " + Integer.toHexString(addr) + ", A = " + Integer.toHexString(A));
	            break;
 	        case (byte) 0xFB: // EI (Enable interrupts)
 	        	IME = true;
 	        	System.out.println("EI executed: IME set to true");
 	        	break;
	        // Arithmetic instructions
	        case (byte) 0x80: { // ADD A, B
	            result = A + B;
	            setFlag(7, (result & 0xFF) == 0);
	            setFlag(6, false);
	            setFlag(5, ((A & 0xF) + (B & 0xF)) > 0xF);
	            setFlag(4, result > 0xFF);
	            A = result & 0xFF;
	            System.out.println("ADD A, B executed: A = " + Integer.toHexString(A));
	            break;
	        }

	        case (byte) 0x81: { // ADD A, C
	            result = A + C;
	            setFlag(7, (result & 0xFF) == 0);
	            setFlag(6, false);
	            setFlag(5, ((A & 0xF) + (C & 0xF)) > 0xF);
	            setFlag(4, result > 0xFF);
	            A = result & 0xFF;
	            System.out.println("ADD A, C executed: A = " + Integer.toHexString(A));
	            break;
	        }

	        case (byte) 0x82: { // ADD A, D
	            result = A + D;
	            setFlag(7, (result & 0xFF) == 0);
	            setFlag(6, false);
	            setFlag(5, ((A & 0xF) + (D & 0xF)) > 0xF);
	            setFlag(4, result > 0xFF);
	            A = result & 0xFF;
	            System.out.println("ADD A, D executed: A = " + Integer.toHexString(A));
	            break;
	        }

	        case (byte) 0x83: { // ADD A, E
	            result = A + E;
	            setFlag(7, (result & 0xFF) == 0);
	            setFlag(6, false);
	            setFlag(5, ((A & 0xF) + (E & 0xF)) > 0xF);
	            setFlag(4, result > 0xFF);
	            A = result & 0xFF;
	            System.out.println("ADD A, E executed: A = " + Integer.toHexString(A));
	            break;
	        }

	        case (byte) 0x84: { // ADD A, H
	            result = A + H;
	            setFlag(7, (result & 0xFF) == 0);
	            setFlag(6, false);
	            setFlag(5, ((A & 0xF) + (H & 0xF)) > 0xF);
	            setFlag(4, result > 0xFF);
	            A = result & 0xFF;
	            System.out.println("ADD A, H executed: A = " + Integer.toHexString(A));
	            break;
	        }

	        case (byte) 0x85: { // ADD A, L
	            result = A + L;
	            setFlag(7, (result & 0xFF) == 0);
	            setFlag(6, false);
	            setFlag(5, ((A & 0xF) + (L & 0xF)) > 0xF);
	            setFlag(4, result > 0xFF);
	            A = result & 0xFF;
	            System.out.println("ADD A, L executed: A = " + Integer.toHexString(A));
	            break;
	        }

	        case (byte) 0x86: { // ADD A, (HL)
	            value = memory.read(getHL());
	            result = A + value;
	            setFlag(7, (result & 0xFF) == 0);
	            setFlag(6, false);
	            setFlag(5, ((A & 0xF) + (value & 0xF)) > 0xF);
	            setFlag(4, result > 0xFF);
	            A = result & 0xFF;
	            System.out.println("ADD A, (HL) executed: A = " + Integer.toHexString(A));
	            break;
	        }

	        case (byte) 0xC6: { // ADD A, d8
	            value = memory.read(PC++);
	            result = A + value;
	            setFlag(7, (result & 0xFF) == 0);
	            setFlag(6, false);
	            setFlag(5, ((A & 0xF) + (value & 0xF)) > 0xF);
	            setFlag(4, result > 0xFF);
	            A = result & 0xFF;
	            System.out.println("ADD A, d8 executed: A = " + Integer.toHexString(A));
	            break;
	        }

	        case (byte) 0x90: // SUB A, B
	            A -= B;
	            setFlag(7, A == 0);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) > (B & 0xF));
	            setFlag(4, A < 0);
	            A &= 0xFF;
	            System.out.println("SUB A, B executed: A = " + Integer.toHexString(A));
	            break;

	        case (byte) 0x91: // SUB A, C
	            A -= C;
	            setFlag(7, A == 0);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) > (C & 0xF));
	            setFlag(4, A < 0);
	            A &= 0xFF;
	            System.out.println("SUB A, C executed: A = " + Integer.toHexString(A));
	            break;

	        case (byte) 0x92: // SUB A, D
	            A -= D;
	            setFlag(7, A == 0);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) > (D & 0xF));
	            setFlag(4, A < 0);
	            A &= 0xFF;
	            System.out.println("SUB A, D executed: A = " + Integer.toHexString(A));
	            break;

	        case (byte) 0x93: // SUB A, E
	            A -= E;
	            setFlag(7, A == 0);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) > (E & 0xF));
	            setFlag(4, A < 0);
	            A &= 0xFF;
	            System.out.println("SUB A, E executed: A = " + Integer.toHexString(A));
	            break;

	        case (byte) 0x94: // SUB A, H
	            A -= H;
	            setFlag(7, A == 0);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) > (H & 0xF));
	            setFlag(4, A < 0);
	            A &= 0xFF;
	            System.out.println("SUB A, H executed: A = " + Integer.toHexString(A));
	            break;

	        case (byte) 0x95: // SUB A, L
	            A -= L;
	            setFlag(7, A == 0);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) > (L & 0xF));
	            setFlag(4, A < 0);
	            A &= 0xFF;
	            System.out.println("SUB A, L executed: A = " + Integer.toHexString(A));
	            break;

	        case (byte) 0x96: { // SUB A, (HL)
	            value = memory.read(getHL());
	            A -= value;
	            setFlag(7, A == 0);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) > (value & 0xF));
	            setFlag(4, A < 0);
	            A &= 0xFF;
	            System.out.println("SUB A, (HL) executed: A = " + Integer.toHexString(A));
	            break;
	        }

	        case (byte) 0xD6: { // SUB A, d8
	            value = memory.read(PC++);
	            A -= value;
	            setFlag(7, A == 0);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) > (value & 0xF));
	            setFlag(4, A < 0);
	            A &= 0xFF;
	            System.out.println("SUB A, d8 executed: A = " + Integer.toHexString(A));
	            break;
	        }

	        // Logical instructions
	        case (byte) 0xA0: // AND A, B
	            A = A & B;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, true);
	            setFlag(4, false);
	            System.out.println("AND A, B executed.");
	            break;

	        case (byte) 0xA1: // AND A, C
	            A = A & C;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, true);
	            setFlag(4, false);
	            System.out.println("AND A, C executed.");
	            break;

	        case (byte) 0xA2: // AND A, D
	            A = A & D;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, true);
	            setFlag(4, false);
	            System.out.println("AND A, D executed.");
	            break;

	        case (byte) 0xA3: // AND A, E
	            A = A & E;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, true);
	            setFlag(4, false);
	            System.out.println("AND A, E executed.");
	            break;

	        case (byte) 0xA4: // AND A, H
	            A = A & H;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, true);
	            setFlag(4, false);
	            System.out.println("AND A, H executed.");
	            break;

	        case (byte) 0xA5: // AND A, L
	            A = A & L;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, true);
	            setFlag(4, false);
	            System.out.println("AND A, L executed.");
	            break;

	        case (byte) 0xA6: { // AND A, (HL)
	            value = memory.read(getHL());
	            A = A & value;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, true);
	            setFlag(4, false);
	            System.out.println("AND A, (HL) executed.");
	            break;
	        }

	        case (byte) 0xE6: { // AND A, d8
	            value = memory.read(PC++);
	            A = A & value;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, true);
	            setFlag(4, false);
	            System.out.println("AND A, d8 executed.");
	            break;
	        }

	        case (byte) 0xA8: // XOR A, B
	            A = A ^ B;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("XOR A, B executed.");
	            break;

	        case (byte) 0xA9: // XOR A, C
	            A = A ^ C;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("XOR A, C executed.");
	            break;

	        case (byte) 0xAA: // XOR A, D
	            A = A ^ D;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("XOR A, D executed.");
	            break;

	        case (byte) 0xAB: // XOR A, E
	            A = A ^ E;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("XOR A, E executed.");
	            break;

	        case (byte) 0xAC: // XOR A, H
	            A = A ^ H;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("XOR A, H executed.");
	            break;

	        case (byte) 0xAD: // XOR A, L
	            A = A ^ L;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("XOR A, L executed.");
	            break;

	        case (byte) 0xAE: { // XOR A, (HL)
	            value = memory.read(getHL());
	            A = A ^ value;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("XOR A, (HL) executed.");
	            break;
	        }

	        case (byte) 0xEE: { // XOR A, d8
	            value = memory.read(PC++);
	            A = A ^ value;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("XOR A, d8 executed.");
	            break;
	        }

	        case (byte) 0xB0: // OR A, B
	            A = A | B;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("OR A, B executed.");
	            break;

	        case (byte) 0xB1: // OR A, C
	            A = A | C;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("OR A, C executed.");
	            break;

	        case (byte) 0xB2: // OR A, D
	            A = A | D;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("OR A, D executed.");
	            break;

	        case (byte) 0xB3: // OR A, E
	            A = A | E;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("OR A, E executed.");
	            break;

	        case (byte) 0xB4: // OR A, H
	            A = A | H;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("OR A, H executed.");
	            break;

	        case (byte) 0xB5: // OR A, L
	            A = A | L;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("OR A, L executed.");
	            break;

	        case (byte) 0xB6: // OR A, (HL)
	            value = memory.read(getHL());
	            A = A | value;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("OR A, (HL) executed.");
	            break;

	        case (byte) 0xF6: // OR A, d8
	            value = memory.read(PC++);
	            A = A | value;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("OR A, d8 executed.");
	            break;

	        case (byte) 0xB7: // CP A, A
	            setFlag(7, A == 0);
	            setFlag(6, true);
	            setFlag(5, false);
	            setFlag(4, false);
	            System.out.println("CP A, A executed.");
	            break;

	        case (byte) 0xB8: // CP A, B
	            setFlag(7, A == B);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) < (B & 0xF));
	            setFlag(4, A < B);
	            System.out.println("CP A, B executed.");
	            break;

	        case (byte) 0xB9: // CP A, C
	            setFlag(7, A == C);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) < (C & 0xF));
	            setFlag(4, A < C);
	            System.out.println("CP A, C executed.");
	            break;

	        case (byte) 0xBA: // CP A, D
	            setFlag(7, A == D);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) < (D & 0xF));
	            setFlag(4, A < D);
	            System.out.println("CP A, D executed.");
	            break;

	        case (byte) 0xBB: // CP A, E
	            setFlag(7, A == E);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) < (E & 0xF));
	            setFlag(4, A < E);
	            System.out.println("CP A, E executed.");
	            break;

	        case (byte) 0xBC: // CP A, H
	            setFlag(7, A == H);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) < (H & 0xF));
	            setFlag(4, A < H);
	            System.out.println("CP A, H executed.");
	            break;

	        case (byte) 0xBD: // CP A, L
	            setFlag(7, A == L);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) < (L & 0xF));
	            setFlag(4, A < L);
	            System.out.println("CP A, L executed.");
	            break;

	        case (byte) 0xBE: // CP A, (HL)
	            value = memory.read(getHL());
	            setFlag(7, A == value);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) < (value & 0xF));
	            setFlag(4, A < value);
	            System.out.println("CP A, (HL) executed.");
	            break;

	        case (byte) 0xFE: // CP A, d8
	            value = memory.read(PC++);
	            setFlag(7, A == value);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) < (value & 0xF));
	            setFlag(4, A < value);
	            System.out.println("CP A, d8 executed.");
	            break;

	        // Increment/Decrement instructions
	        case (byte) 0x04: // INC B
	            B++;
	            setFlag(7, B == 0);
	            setFlag(6, false);
	            setFlag(5, (B & 0xF) == 0);
	            System.out.println("INC B executed: B = " + Integer.toHexString(B & 0xFF));
	            break;

	        case (byte) 0x05: // DEC B
	            B--;
	            setFlag(7, B == 0);
	            setFlag(6, true);
	            setFlag(5, (B & 0xF) == 0xF);
	            System.out.println("DEC B executed: B = " + Integer.toHexString(B & 0xFF));
	            break;

	        case (byte) 0x0C: // INC C
	            C++;
	            setFlag(7, C == 0);
	            setFlag(6, false);
	            setFlag(5, (C & 0xF) == 0);
	            System.out.println("INC C executed: C = " + Integer.toHexString(C & 0xFF));
	            break;

	        case (byte) 0x0D: // DEC C
	            C--;
	            setFlag(7, C == 0);
	            setFlag(6, true);
	            setFlag(5, (C & 0xF) == 0xF);
	            System.out.println("DEC C executed: C = " + Integer.toHexString(C & 0xFF));
	            break;

	        case (byte) 0x14: // INC D
	            D++;
	            setFlag(7, D == 0);
	            setFlag(6, false);
	            setFlag(5, (D & 0xF) == 0);
	            System.out.println("INC D executed: D = " + Integer.toHexString(D & 0xFF));
	            break;

	        case (byte) 0x15: // DEC D
	            D--;
	            setFlag(7, D == 0);
	            setFlag(6, true);
	            setFlag(5, (D & 0xF) == 0xF);
	            System.out.println("DEC D executed: D = " + Integer.toHexString(D & 0xFF));
	            break;

	        case (byte) 0x1C: // INC E
	            E++;
	            setFlag(7, E == 0);
	            setFlag(6, false);
	            setFlag(5, (E & 0xF) == 0);
	            System.out.println("INC E executed: E = " + Integer.toHexString(E & 0xFF));
	            break;

	        case (byte) 0x1D: // DEC E
	            E--;
	            setFlag(7, E == 0);
	            setFlag(6, true);
	            setFlag(5, (E & 0xF) == 0xF);
	            System.out.println("DEC E executed: E = " + Integer.toHexString(E & 0xFF));
	            break;

	        case (byte) 0x24: // INC H
	            H++;
	            setFlag(7, H == 0);
	            setFlag(6, false);
	            setFlag(5, (H & 0xF) == 0);
	            System.out.println("INC H executed: H = " + Integer.toHexString(H & 0xFF));
	            break;

	        case (byte) 0x25: // DEC H
	            H--;
	            setFlag(7, H == 0);
	            setFlag(6, true);
	            setFlag(5, (H & 0xF) == 0xF);
	            System.out.println("DEC H executed: H = " + Integer.toHexString(H & 0xFF));
	            break;

	        case (byte) 0x2C: // INC L
	            L++;
	            setFlag(7, L == 0);
	            setFlag(6, false);
	            setFlag(5, (L & 0xF) == 0);
	            System.out.println("INC L executed: L = " + Integer.toHexString(L & 0xFF));
	            break;

	        case (byte) 0x2D: // DEC L
	            L--;
	            setFlag(7, L == 0);
	            setFlag(6, true);
	            setFlag(5, (L & 0xF) == 0xF);
	            System.out.println("DEC L executed: L = " + Integer.toHexString(L & 0xFF));
	            break;

	        case (byte) 0x34: // INC (HL)
	            value = memory.read(getHL()) + 1;
	            memory.write(getHL(), value & 0xFF);
	            setFlag(7, value == 0);
	            setFlag(6, false);
	            setFlag(5, (value & 0xF) == 0);
	            System.out.println("INC (HL) executed: (HL) = " + Integer.toHexString(value & 0xFF));
	            break;

	        case (byte) 0x35: // DEC (HL)
	            value = memory.read(getHL()) - 1;
	            memory.write(getHL(), value & 0xFF);
	            setFlag(7, value == 0);
	            setFlag(6, true);
	            setFlag(5, (value & 0xF) == 0xF);
	            System.out.println("DEC (HL) executed: (HL) = " + Integer.toHexString(value & 0xFF));
	            break;

	        case (byte) 0x3C: // INC A
	            A++;
	            setFlag(7, A == 0);
	            setFlag(6, false);
	            setFlag(5, (A & 0xF) == 0);
	            System.out.println("INC A executed: A = " + Integer.toHexString(A & 0xFF));
	            break;

	        case (byte) 0x3D: // DEC A
	            A--;
	            setFlag(7, A == 0);
	            setFlag(6, true);
	            setFlag(5, (A & 0xF) == 0xF);
	            System.out.println("DEC A executed: A = " + Integer.toHexString(A & 0xFF));
	            break;

	        // Miscellaneous instructions
	        case (byte) 0x27: // DAA
	            if (!getFlag(6)) { // After an addition
	                if (getFlag(4) || (A > 0x99)) {
	                    A += 0x60;
	                    setFlag(4, true);
	                }
	                if (getFlag(5) || ((A & 0x0F) > 0x09)) {
	                    A += 0x06;
	                }
	            } else { // After a subtraction
	                if (getFlag(4)) {
	                    A -= 0x60;
	                }
	                if (getFlag(5)) {
	                    A -= 0x06;
	                }
	            }
	            setFlag(7, (A & 0xFF) == 0);
	            setFlag(5, false);
	            A &= 0xFF;
	            System.out.println("DAA executed: A = " + Integer.toHexString(A & 0xFF));
	            break;

	        case (byte) 0x2F: // CPL
	            A = ~A;
	            setFlag(6, true);
	            setFlag(5, true);
	            System.out.println("CPL executed: A = " + Integer.toHexString(A & 0xFF));
	            break;

	        case (byte) 0x37: // SCF
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, true);
	            System.out.println("SCF executed.");
	            break;

	        case (byte) 0x3F: // CCF
	            setFlag(6, false);
	            setFlag(5, false);
	            setFlag(4, !getFlag(4));
	            System.out.println("CCF executed.");
	            break;

	        // Jump instructions
	        case (byte) 0xC3: { // JP nn
	            addr = (memory.read(PC++) & 0xFF) | ((memory.read(PC++) & 0xFF) << 8);
	            PC = addr;
	            System.out.println("JP nn executed: PC = " + Integer.toHexString(PC));
	            break;
	        }

	        case (byte) 0xC2:  // JP NZ, nn
	            addr = (memory.read(PC++) & 0xFF) | ((memory.read(PC++) & 0xFF) << 8);
	            if (!getFlag(7)) { // Zero flag is not set
	                PC = addr;
	                System.out.println("JP NZ, nn executed: PC = " + Integer.toHexString(PC));
	                break;
	            } else {
	                System.out.println("JP NZ, nn not taken.");
	            }
	            break;
	        

	        case (byte) 0xCA:  // JP Z, nn
	            addr = (memory.read(PC++) & 0xFF) | ((memory.read(PC++) & 0xFF) << 8);
	            if (getFlag(7)) { // Zero flag is set
	                PC = addr;
	                System.out.println("JP Z, nn executed: PC = " + Integer.toHexString(PC));
	            } else {
	                System.out.println("JP Z, nn not taken.");
	            }
	            break;

	        case (byte) 0xD2:  // JP NC, nn
	            addr = (memory.read(PC++) & 0xFF) | ((memory.read(PC++) & 0xFF) << 8);
	            if (!getFlag(4)) { // Carry flag is not set
	                PC = addr;
	                System.out.println("JP NC, nn executed: PC = " + Integer.toHexString(PC));
	            } else {
	                System.out.println("JP NC, nn not taken.");
	            }
	            break;
	        

	        case (byte) 0xDA:  // JP C, nn
	            addr = (memory.read(PC++) & 0xFF) | ((memory.read(PC++) & 0xFF) << 8);
	            if (getFlag(4)) { // Carry flag is set
	                PC = addr;
	                System.out.println("JP C, nn executed: PC = " + Integer.toHexString(PC));
	            } else {
	                System.out.println("JP C, nn not taken.");
	            }
	            break;

	        case (byte) 0xE9: // JP (HL)
	            PC = getHL();
	            System.out.println("JP (HL) executed: PC = " + Integer.toHexString(PC));
	            break;

	        case (byte) 0x18: { // JR n
	            int offset = memory.read(PC++);
	            if (offset > 127) offset -= 256; // Convert to signed
	            PC += offset;
	            System.out.println("JR n executed: PC = " + Integer.toHexString(PC));
	            break;
	        }

	        case (byte) 0x20: { // JR NZ, n
	            int offset = memory.read(PC++);
	            if (offset > 127) offset -= 256; // Convert to signed
	            if (!getFlag(7)) { // Zero flag is not set
	                PC += offset;
	                System.out.println("JR NZ, n executed: PC = " + Integer.toHexString(PC));
	            } else {
	                System.out.println("JR NZ, n not taken.");
	            }
	            break;
	        }

	        case (byte) 0x28: { // JR Z, n
	            int offset = memory.read(PC++);
	            if (offset > 127) offset -= 256; // Convert to signed
	            if (getFlag(7)) { // Zero flag is set
	                PC += offset;
	                System.out.println("JR Z, n executed: PC = " + Integer.toHexString(PC));
	            } else {
	                System.out.println("JR Z, n not taken.");
	            }
	            break;
	        }

	        case (byte) 0x30: { // JR NC, n
	            int offset = memory.read(PC++);
	            if (offset > 127) offset -= 256; // Convert to signed
	            if (!getFlag(4)) { // Carry flag is not set
	                PC += offset;
	                System.out.println("JR NC, n executed: PC = " + Integer.toHexString(PC));
	            } else {
	                System.out.println("JR NC, n not taken.");
	            }
	            break;
	        }

	        case (byte) 0x38: { // JR C, n
	            int offset = memory.read(PC++);
	            if (offset > 127) offset -= 256; // Convert to signed
	            if (getFlag(4)) { // Carry flag is set
	                PC += offset;
	                System.out.println("JR C, n executed: PC = " + Integer.toHexString(PC));
	            } else {
	                System.out.println("JR C, n not taken.");
	            }
	            break;
	        }

	        case (byte) 0xCD: { // CALL nn
	            addr = (memory.read(PC++) & 0xFF) | ((memory.read(PC++) & 0xFF) << 8);
	            SP -= 2;
	            memory.write(SP, PC & 0xFF);
	            memory.write(SP + 1, (PC >> 8) & 0xFF);
	            PC = addr;
	            System.out.println("CALL nn executed: PC = " + Integer.toHexString(PC));
	            break;
	        }

	        case (byte) 0xC4: { // CALL NZ, nn
	            addr = (memory.read(PC++) & 0xFF) | ((memory.read(PC++) & 0xFF) << 8);
	            if (!getFlag(7)) { // Zero flag is not set
	                SP -= 2;
	                memory.write(SP, PC & 0xFF);
	                memory.write(SP + 1, (PC >> 8) & 0xFF);
	                PC = addr;
	                System.out.println("CALL NZ, nn executed: PC = " + Integer.toHexString(PC));
	            } else {
	                System.out.println("CALL NZ, nn not taken.");
	            }
	            break;
	        }

	        case (byte) 0xCC: { // CALL Z, nn
	            addr = (memory.read(PC++) & 0xFF) | ((memory.read(PC++) & 0xFF) << 8);
	            if (getFlag(7)) { // Zero flag is set
	                SP -= 2;
	                memory.write(SP, PC & 0xFF);
	                memory.write(SP + 1, (PC >> 8) & 0xFF);
	                PC = addr;
	                System.out.println("CALL Z, nn executed: PC = " + Integer.toHexString(PC) + " CALL Z, nn target address: " + Integer.toHexString(addr));
	                System.out.println("CALL Z, nn low byte: " + Integer.toHexString(memory.read(PC) & 0xFF));
	                System.out.println("CALL Z, nn high byte: " + Integer.toHexString(memory.read(PC + 1) & 0xFF));
	            } else {
	                System.out.println("CALL Z, nn not taken.");
	            }
	            break;
	        }

	        case (byte) 0xD4: { // CALL NC, nn
	            addr = (memory.read(PC++) & 0xFF) | ((memory.read(PC++) & 0xFF) << 8);
	            if (!getFlag(4)) { // Carry flag is not set
	                SP -= 2;
	                memory.write(SP, PC & 0xFF);
	                memory.write(SP + 1, (PC >> 8) & 0xFF);
	                PC = addr;
	                System.out.println("CALL NC, nn executed: PC = " + Integer.toHexString(PC));
	            } else {
	                System.out.println("CALL NC, nn not taken.");
	            }
	            break;
	        }

	        case (byte) 0xDC: { // CALL C, nn
	            addr = (memory.read(PC++) & 0xFF) | ((memory.read(PC++) & 0xFF) << 8);
	            if (getFlag(4)) { // Carry flag is set
	                SP -= 2;
	                memory.write(SP, PC & 0xFF);
	                memory.write(SP + 1, (PC >> 8) & 0xFF);
	                PC = addr;
	                System.out.println("CALL C, nn executed: PC = " + Integer.toHexString(PC));
	            } else {
	                System.out.println("CALL C, nn not taken.");
	            }
	            break;
	        }

	        // Return instructions
	        case (byte) 0xC9: { // RET
	            PC = (memory.read(SP++) & 0xFF) | ((memory.read(SP++) & 0xFF) << 8);
	            System.out.println("RET executed: PC = " + Integer.toHexString(PC));
	            break;
	        }

	        case (byte) 0xC0: // RET NZ
	            if (!getFlag(7)) { // Zero flag is not set
	                PC = (memory.read(SP++) & 0xFF) | ((memory.read(SP++) & 0xFF) << 8);
	                System.out.println("RET NZ executed: PC = " + Integer.toHexString(PC));
	            } else {
	                System.out.println("RET NZ not taken.");
	            }
	            break;

	        case (byte) 0xC8: // RET Z
	            if (getFlag(7)) { // Zero flag is set
	                PC = (memory.read(SP++) & 0xFF) | ((memory.read(SP++) & 0xFF) << 8);
	                System.out.println("RET Z executed: PC = " + Integer.toHexString(PC));
	            } else {
	                System.out.println("RET Z not taken.");
	            }
	            break;

	        case (byte) 0xD0: // RET NC
	            if (!getFlag(4)) { // Carry flag is not set
	                PC = (memory.read(SP++) & 0xFF) | ((memory.read(SP++) & 0xFF) << 8);
	                System.out.println("RET NC executed: PC = " + Integer.toHexString(PC));
	            } else {
	                System.out.println("RET NC not taken.");
	            }
	            break;

	        case (byte) 0xD8: // RET C
	            if (getFlag(4)) { // Carry flag is set
	                PC = (memory.read(SP++) & 0xFF) | ((memory.read(SP++) & 0xFF) << 8);
	                System.out.println("RET C executed: PC = " + Integer.toHexString(PC));
	            } else {
	                System.out.println("RET C not taken.");
	            }
	            break;

	        case (byte) 0xD9: { // RETI
	        	IME = true; // Enable interrupts
	            PC = (memory.read(SP++) & 0xFF) | ((memory.read(SP++) & 0xFF) << 8);
	            SP += 2;
	            System.out.println("RETI executed: PC = " + Integer.toHexString(PC));
	            break;
	        }

	        // Restart instructions
	        case (byte) 0xC7: // RST 00H
	            SP -= 2;
	            memory.write(SP, PC & 0xFF);
	            memory.write(SP + 1, (PC >> 8) & 0xFF);
	            PC = 0x00;
	            System.out.println("RST 00H executed.");
	            break;

	        case (byte) 0xCF: // RST 08H
	            SP -= 2;
	            memory.write(SP, PC & 0xFF);
	            memory.write(SP + 1, (PC >> 8) & 0xFF);
	            PC = 0x08;
	            System.out.println("RST 08H executed.");
	            break;

	        case (byte) 0xD7: // RST 10H
	            SP -= 2;
	            memory.write(SP, PC & 0xFF);
	            memory.write(SP + 1, (PC >> 8) & 0xFF);
	            PC = 0x10;
	            System.out.println("RST 10H executed.");
	            break;

	        case (byte) 0xDF: // RST 18H
	            SP -= 2;
	            memory.write(SP, PC & 0xFF);
	            memory.write(SP + 1, (PC >> 8) & 0xFF);
	            PC = 0x18;
	            System.out.println("RST 18H executed.");
	            break;

	        case (byte) 0xE7: // RST 20H
	            SP -= 2;
	            memory.write(SP, PC & 0xFF);
	            memory.write(SP + 1, (PC >> 8) & 0xFF);
	            PC = 0x20;
	            System.out.println("RST 20H executed.");
	            break;

	        case (byte) 0xEF: // RST 28H
	            SP -= 2;
	            memory.write(SP, PC & 0xFF);
	            memory.write(SP + 1, (PC >> 8) & 0xFF);
	            PC = 0x28;
	            System.out.println("RST 28H executed.");
	            break;

	        case (byte) 0xF7: // RST 30H
	            SP -= 2;
	            memory.write(SP, PC & 0xFF);
	            memory.write(SP + 1, (PC >> 8) & 0xFF);
	            PC = 0x30;
	            System.out.println("RST 30H executed.");
	            break;

	        case (byte) 0xFF: // RST 38H
	            SP -= 2;
	            memory.write(SP, PC & 0xFF);
	            memory.write(SP + 1, (PC >> 8) & 0xFF);
	            PC = 0x38;
	            System.out.println("RST 38H executed.");
	            break;
	            
	        	//default case (unknown opcode)
	        default:
	            System.out.println("Unknown opcode: 0x" + Integer.toHexString(opcode & 0xFF));
	    }
	}
	
	/**
	 * emulate a cpu cycle
	 */
	public void step() {
		byte opcode = fetch(); //fetch and decode,
		execute(opcode); //and execute!
		checkInterrupts();
	}
	
	//flag implementation
	public void setFlag(int bit, boolean condit) {
		if (condit) F |= (1 << bit);
		else F &= ~(1 << bit);
	}
 	
	/**
	 * flag positions
	 * 7 = Zero
	 * 6 = Subtract (N)
	 * 5 = Half carry
	 * 4 = Carry
	 * 3-0 = 0
	 * 
	 * 
	 * @param bit
	 * @return
	 */
	public boolean getFlag(int bit) {
		return (F & (1 << bit)) != 0;
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
 