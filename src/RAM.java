public class RAM {
	private byte[] array;
	
	public RAM() {
		array = new byte[0x8000];
		for (int i = 0; i<0x8000; i++) {
			array[i] = (byte)0x00;
		}
	}
	
	public RAM(byte[] theArray) {
		array = theArray;
	}

	public byte[] getRAMArray() {
		return array;
	}

	public void setRAMArray(byte[] array) {
		this.array = array;
	}
	
	public byte read(short address) {
		return array[Short.toUnsignedInt(address)];
	}
	
	public void write(short address, byte data) {
		array[Short.toUnsignedInt(address)] = data;
	}
	
	public String toString(int bytesPerLine, boolean addresses) {
		StringBuilder sb = new StringBuilder();
		
		if (addresses)
			sb.append("0000: ");
		
		for (int i = 1; i <= array.length; i++) {
			if ((i%bytesPerLine != 0) || (i == 0)) {
				sb.append(ROMLoader.byteToHexString(array[i-1])+" ");
			} else {
				String zeroes = "0000";
				sb.append(ROMLoader.byteToHexString(array[i-1])+"\n");
				if (addresses)
					sb.append(zeroes.substring(0, Math.max(0, 4-Integer.toHexString(i).length()))+Integer.toHexString(i)+": ");
			}
		}
		
		return sb.toString();
	}
}
