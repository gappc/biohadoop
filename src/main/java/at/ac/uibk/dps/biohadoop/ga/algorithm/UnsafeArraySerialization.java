package at.ac.uibk.dps.biohadoop.ga.algorithm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import sun.misc.Unsafe;

public class UnsafeArraySerialization {
	// getting Unsafe by reflection
	private static Unsafe unsafe;

	private byte[] buffer;

	private static long byteArrayOffset;
	private static long intArrayOffset;
	private static long longArrayOffset;
	// other offsets

	private static final int SIZE_OF_LONG = 8;
	private static final int SIZE_OF_INT = 4;
	private static final int SIZE_OF_DOUBLE = 8;
	// other sizes

	private long pos = 0;
	
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		UnsafeArraySerialization ser = new UnsafeArraySerialization(1000);
		int size = 100;
		int[] input = new int[size];
		for (int i = 0; i < size; i++) {
			input[i] = i + 1000;
		}
		
		ser.putIntArray(input);
		
		byte[] intermediate = ser.getBuffer();
		
		UnsafeArraySerialization ser2 = new UnsafeArraySerialization(1000);
		ser2.putBuffer(intermediate);
		
		int[] output = ser2.getIntArray();
		
		for (int i : output) {
			System.out.println(i);
		}
	}
	
	static {
		Constructor<Unsafe> unsafeConstructor;
		try {
			unsafeConstructor = Unsafe.class
					.getDeclaredConstructor();
			unsafeConstructor.setAccessible(true);
			unsafe = unsafeConstructor.newInstance();

			byteArrayOffset = unsafe.arrayBaseOffset(byte[].class);
			intArrayOffset = unsafe.arrayBaseOffset(int[].class);
			longArrayOffset = unsafe.arrayBaseOffset(long[].class);
		} catch (NoSuchMethodException | SecurityException
				| InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public UnsafeArraySerialization(int bufferSize) {
		this.buffer = new byte[bufferSize];
	}

	public final void putBuffer(byte[] buffer) {
		this.buffer = buffer;
	}
	
	public final byte[] getBuffer() {
		return buffer;
	}

	public final void putInt(int value) {
		unsafe.putInt(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_INT;
	}

	public final int getInt() {
		int result = unsafe.getInt(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_INT;
		return result;
	}

	public final void putLong(long value) {
		unsafe.putLong(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_LONG;
	}

	public final long getLong() {
		long result = unsafe.getLong(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_LONG;
		return result;
	}
	
	public final void putDouble(double value) {
		unsafe.putDouble(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_DOUBLE;
	}

	public final double getDouble() {
		double result = unsafe.getDouble(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_DOUBLE;
		return result;
	}

	public final void putLongArray(final long[] values) {
		putInt(values.length);
		long bytesToCopy = values.length << 3;
		unsafe.copyMemory(values, longArrayOffset, buffer, byteArrayOffset
				+ pos, bytesToCopy);
		pos += bytesToCopy;
	}

	public final long[] getLongArray() {
		int arraySize = getInt();
		long[] values = new long[arraySize];
		long bytesToCopy = values.length << 3;
		unsafe.copyMemory(buffer, byteArrayOffset + pos, values,
				longArrayOffset, bytesToCopy);
		pos += bytesToCopy;
		return values;
	}

	public final void putIntArray(final int[] values) {
		putInt(values.length);
		long bytesToCopy = values.length << 2;
		unsafe.copyMemory(values, longArrayOffset, buffer, byteArrayOffset
				+ pos, bytesToCopy);
		pos += bytesToCopy;
	}

	public final int[] getIntArray() {
		int arraySize = getInt();
		int[] values = new int[arraySize];
		long bytesToCopy = values.length << 2;
		unsafe.copyMemory(buffer, byteArrayOffset + pos, values,
				intArrayOffset, bytesToCopy);
		pos += bytesToCopy;
		return values;
	}
}
