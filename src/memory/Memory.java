package memory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Memory {
	private byte[] memory = new byte[0x10000]; //64kb of memory
	
	public void loadROM(String filePath) {
		try(FileInputStream fis = new FileInputStream(new File(filePath))) {
			int bytesRead = fis.read(memory, 0x0000, Math.min(fis.available(), 0x8000));
			System.out.println("Loaded " + bytesRead + " bytes into available memory.");
		} catch (IOException e) {
			System.err.println("Error loading ROM: " + e.getMessage());
		}
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
