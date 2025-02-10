package memory;

public class Memory {
	private byte[] memory = new byte[0x10000]; //64kb of memory
	
	public Memory() {
		//init as needed when using ROMs
	}
	
	public int read(int address) {
		//read bytes
		if(address >= 0 && address < memory.length) {
			return memory[address] & 0xFF;
		}  else {
			System.out.println("Invalid read access at address: " + Integer.toHexString(address));
			return 0;
		}
	}
	
	public void write(int address, int value) {
		if(address >= 0 && address < memory.length) {
			memory[address] = (byte) (value & 0xFF);
		} else {
			System.out.println("Invalid write access at address: " + Integer.toHexString(address) + " with write value: " + Integer.toHexString(value));
		}
	}
}
