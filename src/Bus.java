public class Bus {
	public static int VIA_ADDRESS = 0x7ff0;
		public static int ACIA_ADDRESS = 0x5000;

	public static byte read(short address) {
		if (Short.toUnsignedInt(address) >= 0x8000) {
			return EaterEmulator.rom.read((short)(address-0x8000));
		} else if (Short.toUnsignedInt(address) <= VIA_ADDRESS+16 && Short.toUnsignedInt(address) >= VIA_ADDRESS) {
			return EaterEmulator.via.read(address);
		} else if (Short.toUnsignedInt(address) <= ACIA_ADDRESS+3 && Short.toUnsignedInt(address) >= ACIA_ADDRESS) {
			if (EaterEmulator.verbose) System.out.println("Read from address "+Integer.toHexString(Short.toUnsignedInt(address)));
			return EaterEmulator.acia.read(address);
		} else {
			return EaterEmulator.ram.read(address);
		}
	}
	
	public static void write(short address, byte data) {
		if (Short.toUnsignedInt(address) >= 0x8000) {
			System.err.println("Can't write to ROM! ("+Integer.toHexString(Short.toUnsignedInt(address)).toUpperCase()+")");
		} else if (Short.toUnsignedInt(address) <= VIA_ADDRESS+16 && Short.toUnsignedInt(address) >= VIA_ADDRESS) {
			EaterEmulator.via.write(address, data);
		} else if (Short.toUnsignedInt(address) <= ACIA_ADDRESS+3 && Short.toUnsignedInt(address) >= ACIA_ADDRESS) {
			EaterEmulator.acia.write(address, data);
			if (EaterEmulator.verbose) System.out.println("Wrote "+ROMLoader.byteToHexString(data)+" at "+Integer.toHexString(Short.toUnsignedInt(address)));
		} else {
			EaterEmulator.ram.write(address, data);
		}
		
	}
}
