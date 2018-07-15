package resources;

public class CloseCase1 {
	final void insert0 (int index, char[] value, int start, int length, char[] chars) {
		System.arraycopy(value, start, value, index, length);
		System.arraycopy(value, start, value, index, length);
	}
}
