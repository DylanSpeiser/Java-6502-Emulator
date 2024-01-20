import java.util.Scanner;

public class ACIA {
	private byte DATA = 0;
	private byte COMMAND = 0;
	private byte STATUS =0;
	private byte CONTROL = 0;

	private Scanner s;

	public ACIA() {
		s = new Scanner(System.in);

	}

	public byte getDATA() {
		return DATA;
	}

	public byte getCOMMAND() {
		return COMMAND;
	}

	public byte getSTATUS() {
		return STATUS;
	}

	public byte getCONTROL() {
		return CONTROL;
	}

	public byte read(short address) {

		switch (Short.toUnsignedInt(address) - Bus.ACIA_ADDRESS) {
			case 0x00:
				try {
						DATA = (byte) System.in.read();
						if (DATA == 0xa) DATA = 0xd; //convert \n to \r
					
				} catch (Exception e) {
					System.err.println("Error reading from System.in");
				}
				return DATA; // Read Receiver Data Register
			case 0x01:
			try	 {
								if (System.in.available() >= 1) {  // TODO: Remove this later when we go to a windowed interface
					STATUS = 1 << 3;
				} else {
					STATUS = 0 << 3;
				}
			} catch (Exception e) {
				System.err.println("Error reading from System.in");
			}
				return STATUS; // Read Status Register
			case 0x02:
				return COMMAND; // Read Command Register

			case 0x03:
				return CONTROL; // Read Control Register
			default:
				System.err.printf("Attempted to read from invalid ACIA register: %04x\n", address);
				return 0;

		}
	}

	public void write(short address, byte data) {
		switch (Short.toUnsignedInt(address) - Bus.ACIA_ADDRESS) {
			case 0x00:
				System.out.printf("%c", data); // Write Transmitter Data Register
				if (data == 0xd) System.out.printf("\n"); // convert \r to \n

			case 0x01: // Programmed Reset
							// Clear bits 4 through 0 in the Command Register and bit 2 in the Status
							// Register
			case 0x02:
				COMMAND = data;
				break; // Write Command Register
			case 0x03:
				CONTROL = data;
				break; // Write Control Register
			default:
				System.err.printf("Attempted to write to invalid ACIA register: %04x\n", address);

		}
	}

}