package esutdal.javanotes.cache;

import java.util.Map;

import esutdal.javanotes.cache.core.AbstractCache;
import esutdal.javanotes.cache.util.CacheException;
import esutdal.javanotes.cache.util.DefaultSerializer;
import esutdal.javanotes.cache.util.ISerializer;
import esutdal.javanotes.cache.util.Instantiator;
import esutdal.javanotes.cache.util.KryoSerializer;

public class CacheBuilder
{
	
	/**
	 * The maximum number of seconds an element can exist in the cache
	 * regardless of use. The element expires at this limit and will no longer
	 * be returned from the cache. The default value is 0, which means no TTL
	 * eviction takes place (infinite lifetime).
	 */
	private long				timeToLiveSecs			= 0;

	/**
	 * The maximum number of seconds an element can exist in the cache without
	 * being accessed. The element expires at this limit and will no longer be
	 * returned from the cache. The default value is 0, which means no TTI
	 * eviction takes place (infinite lifetime).
	 */
	private long				timeToIdleSecs			= 0;
	private String			cacheName;
	/**
	 * 
	 */
	private EvictionStrategy evictionStrategy;
	private ISerializer serializer;

	/**
	 * The maximum number of items to be present in the cache. Adding anymore
	 * item will remove the LRU item.
	 */
	private int					maxEntriesOnHeap		= 1000;

	/**
	 * Whether to use an expiry checker thread to remove expired items
	 */
	private boolean				useExpiryTimer			= false;

	/**
	 * If expiry checker thread is used, the interval after which each run will
	 * happen
	 */
	private int					expiryTimerIntervalSecs	= 3600;

	public CacheBuilder setTimeToLiveSecs(long timeToLiveSecs) {
		this.timeToLiveSecs = timeToLiveSecs;
		return this;
	}

	
	public CacheBuilder setTimeToIdleSecs(long timeToIdleSecs) {
		this.timeToIdleSecs = timeToIdleSecs;
		return this;
	}

	
	public CacheBuilder setCacheName(String cacheName) {
		this.cacheName = cacheName;
		return this;
	}

	public CacheBuilder setMaxEntriesOnHeap(int maxEntriesOnHeap) {
		this.maxEntriesOnHeap = maxEntriesOnHeap;
		return this;
	}

	public CacheBuilder setUseExpiryTimer(boolean useExpiryTimer) {
		this.useExpiryTimer = useExpiryTimer;
		return this;
	}

	public CacheBuilder setExpiryTimerIntervalSecs(int expiryTimerIntervalSecs) {
		this.expiryTimerIntervalSecs = expiryTimerIntervalSecs;
		return this;
	}

	public CacheBuilder setEvictionStrategy(EvictionStrategy evictStrategy){
		this.evictionStrategy = evictStrategy;
		return this;
	}
	public CacheBuilder setSerializer(ISerializer serializer){
		this.serializer = serializer;
		return this;
	}
	/**
	 * Programmatic cache building. <b>Note:</b> Will override any other cache with the same name
	 * loaded declaratively (using {@link CacheManager}) in the same jvm.
	 * @param cache
	 * @return
	 * @throws CacheException
	 */
	public <V, K> VirtualMemCache<K, V> build(VirtualMemCache<K, V> cache) throws CacheException
	{
		cache.setTimeToLiveSecs(timeToLiveSecs);
		cache.setCacheName(cacheName);
		cache.setExpiryTimerIntervalSecs(expiryTimerIntervalSecs);
		cache.setMaxEntriesOnHeap(maxEntriesOnHeap);
		cache.setTimeToIdleSecs(timeToIdleSecs);
		cache.setTimeToLiveSecs(timeToLiveSecs);
		cache.setUseExpiryTimer(useExpiryTimer);
		cache.setEvictionStrategy(evictionStrategy);
		cache.setSerializer(serializer);
		
		setDefaults(cache);
		try 
		{
			cache.init();
			CacheManager.putCache(cache.getCacheName(), cache);
			
		} catch (CacheException e) {
			throw e;
		}
		return cache;
	}
	
	private static void setDefaults(AbstractCache<?, ?> cache)
	{
		if(cache.getSerializer() == null)
		{
			try 
			{
				Class.forName("com.esotericsoftware.kryo.Kryo");
				cache.setSerializer(new KryoSerializer());
			} catch (ClassNotFoundException e) {
				cache.setSerializer(new DefaultSerializer());
			}	
			
		}
		if(cache.getEvictionStrategy() == null)
		{
			cache.setEvictionStrategy(EvictionStrategy.LRU);
		}
		if(cache.getMaxEntriesOnHeap() == 0)
		{
			cache.setMaxEntriesOnHeap(1024);
		}
	}
	static AbstractCache<?, ?> build(Map<String, String> map, String name) throws InstantiationException
	{
		String clazz = map.remove("class");
		//log.info("Creating cache named \"" + name + "\" with class "+clazz);
		AbstractCache<?, ?> cache;
		
			cache = Instantiator.newInstance(clazz);
			cache.setCacheName(name);
			if(map.containsKey("timeToLiveSecs"))
			{
				cache.setTimeToLiveSecs(Integer.valueOf(map.get("timeToLiveSecs")));
			}
			if(map.containsKey("timeToIdleSecs"))
			{
				cache.setTimeToIdleSecs(Integer.valueOf(map.get("timeToIdleSecs")));
			}
			if(map.containsKey("maxEntriesOnHeap"))
			{
				cache.setMaxEntriesOnHeap((Integer.valueOf(map.get("maxEntriesOnHeap"))));
			}
			if(map.containsKey("useExpiryTimer"))
			{
				cache.setUseExpiryTimer(Boolean.valueOf(map.get("useExpiryTimer")));
			}
			if(map.containsKey("expiryTimerIntervalSecs"))
			{
				cache.setExpiryTimerIntervalSecs(Integer.valueOf(map.get("expiryTimerIntervalSecs")));
			}
			if(map.containsKey("evictionStrategy"))
			{
				cache.setEvictionStrategy(EvictionStrategy.valueOf(map.get("evictionStrategy")));
			}
			if(map.containsKey("serializer"))
			{
				cache.setSerializer((ISerializer) Instantiator.newInstance(map.get("serializer")));
			}
			
			setDefaults(cache);
			
			return cache;
		
	}
}