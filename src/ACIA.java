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
				return EaterEmulator.serial.getKey(); // get serial key
			case 0x01:
				STATUS = (byte) (EaterEmulator.serial.hasKey() ? 0x08 : 0x00);
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
				EaterEmulator.serial.receiveKey(data);

			case 0x01: // Programmed Reset
							// Clear bits 4 through 0 in the Command Register and bit 2 in the Status
							// Register
				//COMMAND &= 0b11100000;
				//STATUS &= 0b11111011; //i dont remember this but just doing what was written
				//EaterEmulator.serial.reset();
				//okay this is triggering in some random places and im kinda confused why so ill just leave it empty like how it was before
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
