package esutdal.javanotes.cache.util;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public class UnsafeWrapper {

	private static final Unsafe UNSAFE;
	static
	{
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			UNSAFE = (Unsafe) f.get(null);
		} catch (Exception e) {
			throw new ExceptionInInitializerError("sun.misc.Unsafe not instantiated ["+e.toString()+"]");
		}

	}
	public static Unsafe getUnsafe() {
		return UNSAFE;
	}
	
	public static byte[] getByteArray(int noOfBytes, long _address)
    {
        byte[] values = new byte[noOfBytes];
 
        UNSAFE.copyMemory(null, _address,
                          values, Unsafe.ARRAY_BYTE_BASE_OFFSET,
                          noOfBytes);
        
        return values;
    }
	public static void putByteArray(final byte[] values, long _address)
    {
        UNSAFE.copyMemory(values, Unsafe.ARRAY_BYTE_BASE_OFFSET,
                          null, _address,
                          values.length);
       
    }
}
