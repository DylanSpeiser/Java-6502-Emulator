public class VIA {
	byte PORTA = 0x00;
	byte PORTB = 0x00;
	byte DDRA = 0x00;
	byte DDRB = 0x00;
	
	public byte read(short address) {
		return 0;
	}

	public void write(short address, byte data) {
		switch (Short.toUnsignedInt(address)) {
			case 0x6000:
				PORTB = data;
				break;
			case 0x6001:
				PORTA = data;
				if ((data&0x80)==0x80) {
					EaterEmulator.lcd.write((PORTA&0x20)==0x20, (byte)(PORTB&DDRB));
				}
				break;
			case 0x6002:
				DDRB = data;
				break;
			case 0x6003:
				DDRA = data;
				break;
		}
	}
}