import java.util.Scanner;

public class ACIA {
	private byte dataRegister = 0;
	private byte commandRegister = 0;
	private byte statusRegister =0;
	private byte controlRegister = 0;

	private Scanner s;

	public ACIA() {
		s = new Scanner(System.in);

	}

	/**
	 * @return theF dataRegister
	 */
	public byte getDataRegister() {
		return dataRegister;
	}

	/**
	 * @return the commandRegister
	 */
	public byte getCommandRegister() {
		return commandRegister;
	}

	/**
	 * @return the statusRegister
	 */
	public byte getStatusRegister() {
		return statusRegister;
	}

	/**
	 * @return the controlRegister
	 */
	public byte getControlRegister() {
		return controlRegister;
	}

	public byte read(short address) {

		switch (Short.toUnsignedInt(address) - Bus.ACIA_ADDRESS) {
			case 0x00:
				try {
						dataRegister = (byte) System.in.read();
						if (dataRegister == 0xa) dataRegister = 0xd; //convert \n to \r
					
				} catch (Exception e) {
					System.err.println("Error reading from System.in");
				}
				return dataRegister; // Read Receiver Data Register
			case 0x01:
			try	 {
								if (System.in.available() >= 1) {  // TODO: Remove this later when we go to a windowed interface
					statusRegister = 1 << 3;
				} else {
					statusRegister = 0 << 3;
				}
			} catch (Exception e) {
				System.err.println("Error reading from System.in");
			}
				return statusRegister; // Read Status Register
			case 0x02:
				return commandRegister; // Read Command Register

			case 0x03:
				return controlRegister; // Read Control Register
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
				commandRegister = data;
				break; // Write Command Register
			case 0x03:
				controlRegister = data;
				break; // Write Control Register
			default:
				System.err.printf("Attempted to write to invalid ACIA register: %04x\n", address);

		}
	}

}