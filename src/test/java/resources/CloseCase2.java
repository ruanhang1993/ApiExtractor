package resources;

public class CloseCase2 {
	final void insert0 (int index, char[] value, int start, int length, char[] chars) {
		System.arraycopy(value, start, chars, index, length);
		System.arraycopy(value, index, value, start, length);
	}
}
