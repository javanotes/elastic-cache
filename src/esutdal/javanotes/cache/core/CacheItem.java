package esutdal.javanotes.cache.core;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import esutdal.javanotes.cache.util.ISerializer;

/**
 * Wrapper for a cached item
 * @author esutdal
 *
 * @param <V>
 */
public final class CacheItem<V>
{
	public int getNumOfHits() {
		return numOfHits;
	}

	private long lastAccessed;
	private final long created;
	private int numOfHits = 0;
	private V value;
	public CacheItem(){this(null);}		
	public CacheItem(V value)
	{
		this(value, System.currentTimeMillis());
		
	}
	private CacheItem(V value, long createdTime)
	{
		this.value = value;
		created = createdTime;
		
	}
	/**
	 * Deserializes given a serializer
	 * @param buff
	 * @param valSerializer - value serializer
	 * @return
	 * @throws Exception
	 */
	public final static CacheItem<?> deserialize(ByteBuffer buff, ISerializer valSerializer) throws Exception
	{
		long c = buff.getLong();
		long l = buff.getLong();
		int u = buff.getInt();
		byte[] valBytes = new byte[buff.remaining()];
		buff.get(valBytes);
		Object val = valSerializer.deserialize(valBytes);
		CacheItem<?> item = new CacheItem<Object>(val, c);
		item.lastAccessed = l;
		item.numOfHits = u;
		return item;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (created ^ (created >>> 32));
		result = prime * result + super.hashCode();
		result = prime * result + value.hashCode();
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		CacheItem other = (CacheItem) obj;
		if (created != other.created)
			return false;
		if (!value.equals(other.value))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "CacheItem [lastAccessed=" + lastAccessed + ", created="
				+ created + ", value=" + value + "]";
	}
	public boolean isIdled(long ttiSecs)
	{
		return ttiSecs > 0 && (System.currentTimeMillis() - lastAccessed) > TimeUnit.SECONDS.toMillis(ttiSecs);
	}
	public boolean isExpired(long ttlSecs)
	{
		return ttlSecs > 0 && (System.currentTimeMillis() - created) > TimeUnit.SECONDS.toMillis(ttlSecs);
	}
	/**
	 * Serializes using a given serializer
	 * @param valSerializer - value serializer
	 * @return
	 * @throws Exception
	 */
	public final ByteBuffer serialize(ISerializer valSerializer) throws Exception
	{
		byte[] valBytes = valSerializer.serialize(value);
		ByteBuffer buff = ByteBuffer.allocate(valBytes.length + 20);
		buff.putLong(created);
		buff.putLong(lastAccessed);
		buff.putInt(numOfHits);
		buff.put(valBytes);
		
		return buff;
	}
	
	V getValue()
	{
		lastAccessed = System.currentTimeMillis();
		numOfHits++;
		return value;
	}
	
}
