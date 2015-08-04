package esutdal.javanotes.cache.map;

import java.util.LinkedHashMap;
import java.util.Map;

import esutdal.javanotes.cache.VirtualMemCache;
import esutdal.javanotes.cache.core.CacheItem;


public class LRUMap<K, V> extends LinkedHashMap<K, CacheItem<V>>
{

	private final VirtualMemCache<K,V>.MapEntryModificationCallback callback;
	/**
	 * 
	 */
	private final int maxOnHeap;
	private final long ttlSecs;
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 8532010683283081429L;

	/**
	 * 
	 * @param maxOnHeap
	 * @param ttlSecs
	 */
	public LRUMap(int maxOnHeap, long ttlSecs, VirtualMemCache<K,V>.MapEntryModificationCallback callback)
	{
		super(16, 0.75f, true);
		this.maxOnHeap = maxOnHeap;
		this.ttlSecs = ttlSecs;
		this.callback = callback;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, CacheItem<V>> eldest)
	{
		if(size() > maxOnHeap)
		{
			return callback.moveEntry(eldest.getKey(), ttlSecs);
		}
		
		return false;
	}

}