import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ROM {
	private byte[] array;
	public String ROMString = "";

	public ROM() {
		array = new byte[0x8000];
		for (int i = 0; i < 0x8000; i++) {
			array[i] = (byte) 0x00;
		}
		ROMString = this.toStringWithOffset(8, 0x8000, true);
	}

	public ROM(byte[] theArray) {
		array = theArray;
		ROMString = this.toStringWithOffset(8, 0x8000, true);
	}

	public byte[] getROMArray() {
		return array;
	}

	public void setROMArray(byte[] array) {
		this.array = array;
		ROMString = this.toStringWithOffset(8, 0x8000, true);
	}

	public byte read(short address) {
		return array[Short.toUnsignedInt(address)];
	}

	public String toString(int bytesPerLine, boolean addresses) {
		return toStringWithOffset(bytesPerLine, 0, addresses);
	}

	public String toStringWithOffset(int bytesPerLine, int addressOffset, boolean addresses) {
		List<byte[]> chunks = new ArrayList<>();
		int chunkAmount = (int) Math.ceil(array.length / (double) bytesPerLine);

		for (int i = 0; i < chunkAmount; i++) {
			int end = Math.min(array.length, (i + 1) * bytesPerLine);
			byte[] chunk = Arrays.copyOfRange(array, i * bytesPerLine, end);
			chunks.add(chunk);
		}

		var lines = CollectionUtils.withIndex(chunks).stream().map(indexedValue -> {
					var localSb = new StringBuilder();
					Byte[] chunk = CollectionUtils.toBoxedArray(indexedValue.value());

					if (addresses)
						localSb.append(String.format("%04X: ", (indexedValue.index() * bytesPerLine) + addressOffset));

					localSb.append(String.join(" ", CollectionUtils.mapArray(chunk, (b -> String.format("%02X", b)))));
					return localSb.toString();
				}
		).toList();

		return String.join(System.lineSeparator(), lines);
	}
}
