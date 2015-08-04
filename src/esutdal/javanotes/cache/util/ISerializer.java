package esutdal.javanotes.cache.util;

/**
 * To be implemented for custom serialization in a thread safe manner.
 * @author esutdal
 *
 */
public interface ISerializer {

	/**
	 * 
	 * @param object
	 * @return
	 * @throws Exception
	 */
	public byte[] serialize(Object object) throws Exception;
	/**
	 * 
	 * @param serialized
	 * @return
	 * @throws Exception
	 */
	public Object deserialize(byte[] serialized) throws Exception;
}
