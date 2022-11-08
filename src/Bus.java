public class Bus {
	public static int VIA_ADDRESS = 0x7ff0;

	public static byte read(short address) {
		if (Short.toUnsignedInt(address) >= 0x8000) {
			return EaterEmulator.rom.read((short)(address-0x8000));
		} else if (Short.toUnsignedInt(address) <= VIA_ADDRESS+16 && Short.toUnsignedInt(address) >= VIA_ADDRESS) {
			return EaterEmulator.via.read(address);
		} else {
			return EaterEmulator.ram.read(address);
		}
	}
	
	public static void write(short address, byte data) {
		if (Short.toUnsignedInt(address) >= 0x8000) {
			System.err.println("Can't write to ROM! ("+Integer.toHexString(Short.toUnsignedInt(address)).toUpperCase()+")");
		} else if (Short.toUnsignedInt(address) <= VIA_ADDRESS+16 && Short.toUnsignedInt(address) >= VIA_ADDRESS) {
			EaterEmulator.via.write(address, data);
		} else {
			EaterEmulator.ram.write(address, data);
		}
		//System.out.println("Wrote "+data+" at "+address);
	}
}
