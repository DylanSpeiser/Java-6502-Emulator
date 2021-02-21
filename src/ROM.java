public class ROM {
	private byte[] array;
	public String ROMString = "";
	
	public ROM() {
		array = new byte[0x8000];
		for (int i = 0; i<0x8000; i++) {
			array[i] = (byte)0x00;
		}
		ROMString = this.toStringWithOffset(8,0x8000,true);
	}
	
	public ROM(byte[] theArray) {
		array = theArray;
		ROMString = this.toStringWithOffset(8,0x8000,true);
	}

	public byte[] getROMArray() {
		return array;
	}

	public void setROMArray(byte[] array) {
		this.array = array;
		ROMString = this.toStringWithOffset(8,0x8000,true);
	}
	
	public byte read(short address) {
		return array[Short.toUnsignedInt(address)];
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
					sb.append(zeroes.substring(0, 4-Integer.toHexString(i).length())+Integer.toHexString(i)+": ");
			}
		}
		
		return sb.toString();
	}
	
	public String toStringWithOffset(int bytesPerLine, int addressOffset, boolean addresses) {
		StringBuilder sb = new StringBuilder();
		String zeroes = "0000";
		
		if (addresses)
			sb.append(zeroes.substring(0, 4-Integer.toHexString(0+addressOffset).length())+Integer.toHexString(0+addressOffset)+": ");
		
		for (int i = 1; i <= array.length; i++) {
			if ((i%bytesPerLine != 0) || (i == 0)) {
				sb.append(ROMLoader.byteToHexString(array[i-1])+" ");
			} else {
				sb.append(ROMLoader.byteToHexString(array[i-1])+"\n");
				if (addresses)
					sb.append(Integer.toHexString(i+addressOffset)+": ");
			}
		}
		
		return sb.toString();
	}
}
