import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class CollectionUtils {
	public static Byte[] toBoxedArray(byte[] array) {
		return IntStream.range(0, array.length).mapToObj(i -> array[i]).toArray(Byte[]::new);
	}

	public static <T, R> List<R> mapArray(T[] array, Function<T, R> mapper) {
		return Arrays.stream(array).map(mapper).toList();
	}

	public static <T> List<IndexedValue<T>> withIndex(List<T> list) {
		return IntStream.range(0, list.size()).mapToObj(i -> new IndexedValue<>(i, list.get(i))).toList();
	}

	public record IndexedValue<T>(int index, T value) {
	}
}
