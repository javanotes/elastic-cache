package esutdal.javanotes.cache.map;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import esutdal.javanotes.cache.VirtualMemCache;
import esutdal.javanotes.cache.core.CacheItem;

public class LFUMap<K, V> extends LinkedHashMap<K, CacheItem<V>> {

	public LFUMap(int maxOnHeap, long ttlSecs, VirtualMemCache<K,V>.MapEntryModificationCallback callback)
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
			java.util.Map.Entry<K, CacheItem<V>> lfuEntry = null;
			
			int minUsed = Integer.MAX_VALUE;
			for (iter = entrySet().iterator(); iter.hasNext();) {
				java.util.Map.Entry<K, CacheItem<V>> next = iter.next();
				if(next.getValue().getNumOfHits() < minUsed)
				{
					minUsed = next.getValue().getNumOfHits();
					lfuEntry = next;
				}
			}
			if (lfuEntry != null) {
				callback.removeItem(lfuEntry.getKey(), ttlSecs);
			}
		}
			
	}
	private final VirtualMemCache<K,V>.MapEntryModificationCallback callback;
	/**
	 * 
	 */
	private final int maxOnHeap;
	private final long ttlSecs;
	/**
	 * 
	 */
	private static final long serialVersionUID = -5662909825327977124L;
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
