package esutdal.javanotes.cache.core;

import java.io.IOException;

import esutdal.javanotes.cache.util.UnsafeWrapper;

/**
 * Reference to an off-heap object storage. Read only access. Can be shared across multiple threads.
 * Would store a long (off-heap memory location), and int (byte array size) on the java heap.
 * 
 * So a pointer to any object would take a constant (on-heap) storage of 12 bytes.
 * 
 * @author esutdal
 *
 * @param <T> instance type
 */
class ItemPointer {
	
	private final int noOfBytes;
			
	private void putByteArray(final byte[] values)
    {
       UnsafeWrapper.putByteArray(values, _address);
       
    }
 
    
	private byte[] getByteArray()
    {
        return UnsafeWrapper.getByteArray(noOfBytes, _address);
    }
	
		
	/**
	 * Get a read committed instance
	 * @param <T>
	 * @return
	 * @throws IOException 
	 */
	public byte[] getBytes() throws IOException{
				
		try {
			return getByteArray();
			
		} catch (Exception e) {
			throw new IOException(e);
		}
		
	}
		
	/**
	 * Deallocate the object. This is outside JVM garbage collection
	 * @return
	 */
	boolean dealloc(){
		UnsafeWrapper.getUnsafe().freeMemory(_address);
		return true;
	}
		
	private final long _address;
	/**
	 * Allocate an object to off-heap. Remember to dealloc() it since garbage collector won't see it
	 * @param <T>
	 * @param object
	 * @return
	 * @throws IOException 
	 */
	<T> ItemPointer(byte[] bytes) throws IOException{
		try 
		{
			
			noOfBytes = bytes.length;
			_address = UnsafeWrapper.getUnsafe().allocateMemory(noOfBytes);
			putByteArray(bytes);
			
		} catch (Exception e) {
			throw new IOException(e);
		}
		
	}
	
}
