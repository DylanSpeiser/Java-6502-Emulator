public class VIA {
	byte PORTA = 0x00;
	byte PORTB = 0x00;
	byte DDRA = 0x00;
	byte DDRB = 0x00;
	byte PCR = 0x00;
	byte IFR = 0x00;
	byte IER = 0x00;
	
	public byte read(short address) {
		switch (Short.toUnsignedInt(address)-Bus.VIA_ADDRESS) {
			case 0x000:
				IFR &= (byte)(0b01100111);
				PORTB = EaterEmulator.lcd.read((PORTA & 0x20) != 0);
				return PORTB;
			case 0x001:
				IFR &= (byte)(0b01111100);
				return 0;
			case 0x002:
				return 0;
			case 0x003:
				return 0;
			case 0x00C:
				return PCR;
			case 0x00D:
				return IFR;
			case 0x00E:
				return IER;
		}
		return 0;
	}

	public void write(short address, byte data) {
		switch (Short.toUnsignedInt(address)-Bus.VIA_ADDRESS) {
			case 0x000:
				PORTB = data;
				if ((PORTA&0x80)==0x80 && (PORTA&0x40)==0x00) {
					EaterEmulator.lcd.write((PORTA&0x20)==0x20, (byte)(PORTB&DDRB));
				}
				break;
			case 0x001:
				PORTA = data;
				if ((PORTA&0x80)==0x80 && (PORTA&0x40)==0x00) {
					EaterEmulator.lcd.write((PORTA&0x20)==0x20, (byte)(PORTB&DDRB));
				}
				break;
			case 0x002:
				DDRB = data;
				break;
			case 0x003:
				DDRA = data;
				break;
			case 0x00C:
				PCR = data;
				break;
			case 0x00D:
				IFR = data;
				break;
			case 0x00E:
				IER = data;
				break;
		}
	}
	
	public void CA1() {
		if ((IER &= (byte)(0b00000010)) == 0b00000010) {
			IFR |= (byte)(0b10000010);
			EaterEmulator.cpu.interruptRequested = true;
		}
	}
	
	public void CA2() {
		if ((IER &= (byte)(0b00000010)) == 0b00000001) {
			IFR |= (byte)(0b10000001);
			EaterEmulator.cpu.interruptRequested = true;
		}
	}
	
	public void CB1() {
		if ((IER &= (byte)(0b00000010)) == 0b00010000) {
			IFR |= (byte)(0b10010000);
			EaterEmulator.cpu.interruptRequested = true;
		}
	}
	
	public void CB2() {
		if ((IER &= (byte)(0b00000010)) == 0b00001000) {
			IFR |= (byte)(0b10001000);
			EaterEmulator.cpu.interruptRequested = true;
		}
	}
}