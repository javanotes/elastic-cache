package esutdal.javanotes.cache.map;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import esutdal.javanotes.cache.VirtualMemCache;
import esutdal.javanotes.cache.core.CacheItem;

public class LIFOMap<K,V> extends LinkedHashMap<K, CacheItem<V>> {

	private final VirtualMemCache<K,V>.MapEntryModificationCallback callback;

	private final int maxOnHeap;
	private final long ttlSecs;
	public LIFOMap(int maxOnHeap, long ttlSecs, VirtualMemCache<K,V>.MapEntryModificationCallback callback)
	{
		super(16, 0.75f);
		this.maxOnHeap = maxOnHeap;
		this.ttlSecs = ttlSecs;
		this.callback = callback;
	}
	private void markForRemoval()
	{
		if (size() == maxOnHeap) {
			Iterator<java.util.Map.Entry<K, CacheItem<V>>> iter;
			java.util.Map.Entry<K, CacheItem<V>> lastEntry = null;
			for (iter = entrySet().iterator(); iter.hasNext();) {
				lastEntry = iter.next();
			}
			if (lastEntry != null) {
				callback.removeItem(lastEntry.getKey(), ttlSecs);
			}
		}
			
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -8373893384940183648L;

	@Override
	public CacheItem<V> put(K key, CacheItem<V> value) {
		markForRemoval();
		CacheItem<V> item = super.put(key, value);
		return item;
		
	}

	@Override
	public void putAll(Map<? extends K, ? extends CacheItem<V>> m) {
		markForRemoval();
		super.putAll(m);
		

	}


}
