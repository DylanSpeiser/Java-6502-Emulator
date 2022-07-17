package com.hadden.emu;
public class VIA {
	byte PORTA = 0x00;
	byte PORTB = 0x00;
	byte DDRA = 0x00;
	byte DDRB = 0x00;
	byte PCR = 0x00;
	byte IFR = 0x00;
	byte IER = 0x00;
	
	public byte read(short address) {
		switch (Short.toUnsignedInt(address)) {
			case 0x6000:
				IFR &= (byte)(0b01100111);
				return 0;
			case 0x6001:
				IFR &= (byte)(0b01111100);
				return 0;
			case 0x6002:
				return 0;
			case 0x6003:
				return 0;
			case 0x600C:
				return PCR;
			case 0x600D:
				return IFR;
			case 0x600E:
				return IER;
		}
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
					//SystemEmulator.lcd.write((PORTA&0x20)==0x20, (byte)(PORTB&DDRB));
				}
				break;
			case 0x6002:
				DDRB = data;
				break;
			case 0x6003:
				DDRA = data;
				break;
			case 0x600C:
				PCR = data;
				break;
			case 0x600D:
				IFR = data;
				break;
			case 0x600E:
				IER = data;
				break;
		}
	}
	
	public void CA1() {
		if ((IER &= (byte)(0b00000010)) == 0b00000010) {
			IFR |= (byte)(0b10000010);
			SystemEmulator.cpu.interruptRequested = true;
		}
	}
	
	public void CA2() {
		if ((IER &= (byte)(0b00000010)) == 0b00000001) {
			IFR |= (byte)(0b10000001);
			SystemEmulator.cpu.interruptRequested = true;
		}
	}
	
	public void CB1() {
		if ((IER &= (byte)(0b00000010)) == 0b00010000) {
			IFR |= (byte)(0b10010000);
			SystemEmulator.cpu.interruptRequested = true;
		}
	}
	
	public void CB2() {
		if ((IER &= (byte)(0b00000010)) == 0b00001000) {
			IFR |= (byte)(0b10001000);
			SystemEmulator.cpu.interruptRequested = true;
		}
	}
}