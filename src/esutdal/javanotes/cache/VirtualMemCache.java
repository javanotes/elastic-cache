package esutdal.javanotes.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import esutdal.javanotes.cache.core.AbstractCache;
import esutdal.javanotes.cache.core.CacheItem;
import esutdal.javanotes.cache.core.OffHeapMap;
import esutdal.javanotes.cache.core.OffHeapMap.ClearCriteria;
import esutdal.javanotes.cache.map.FIFOMap;
import esutdal.javanotes.cache.map.LFUMap;
import esutdal.javanotes.cache.map.LIFOMap;
import esutdal.javanotes.cache.map.LRUMap;
import esutdal.javanotes.cache.util.CacheException;

/**
 * Implementation of a caching solution which overflows non-expired evicted items to an off-heap location. Addition of new items entails selection 
 * of another item for eviction, if max size of cache is reached. Evicted item (selected based on eviction strategy), if not expired, 
 * overflow to off-heap memory. An idle detection facility is also provided, which if enabled, would schedule a periodic eviction of ALL expired 
 * items and overflow of ALL idled items to off-heap area. Thus, this eviction does not look for any eviction strategy.
 * <p> This is the abstract class to be extended for loading cache entry. All other implementations from the 
 * base class {@link AbstractCache} is already provided, and need not be implemented. Under normal scenarios, 
 * implementing {@link #load(Object)} and {@link #loadAll()} (optionally) should suffice.
 * <p>Usage:
 * <pre>
 * 		 VirtualMemCache<K,V> cache = new CacheBuilder()
		.setCacheName("some_cache_name")
		.setMaxEntriesOnHeap(3)
		.setEvictionStrategy(EvictionStrategy.LIFO)
		.build(
			new VirtualMemCache<>() {

				
				protected V load(K key) throws CacheException {
					
					// V v = ... costly operations
					
					return V;
				}
	
				
				protected Map<K,V> loadAll() throws CacheException {
					// Needs to be implemented only if refresh(true) will be used
					return null;
				}
		});
 * </pre>
 * <p>
 * The various parameters for cache configuration is as follows:<p>
<p><li>
<b>class</b>
	The custom class implementing esutdal.javanotes.cache.VirtualMemCache, which will be used for item loading
	on cache miss. Required
<p><li>	
<b>name</b>
	A named identifier for loading the cache class. Required

<p><li>	
<b>maxEntriesOnHeap</b>
	The maximum number of items to be present in the cache. Adding anymore item will 
	select another item based on a given eviction strategy, for removal. If that item is not expired based on 'timeToLiveSecs',
	it is put into an off-heap location (overflow). Default 1024

<p><li>	
<b>timeToLiveSecs</b>
	The maximum number of seconds an element can exist in the cache regardless of use. 
	The element expires at this limit and will no longer be returned from the cache. 
	The default value is 0, which means no TTL eviction takes place (infinite lifetime).
<p><li>	
<b>timeToIdleSecs</b>
	The maximum number of seconds an element can exist in the cache without being accessed. 
	The element expires at this limit and will no longer be returned from the cache. 
	The default value is 0, which means no TTI eviction takes place (infinite lifetime).
<p><li>	
<b>useExpiryTimer</b>
	Whether to use an expiration checker thread to remove expired items [true/false]. Default false.
	Expiration does not follow any eviction strategy. If an item is expired based on 'timeToLiveSecs', 
	it is removed from cache; if an item is idled based on 'timeToIdleSecs', it is put into an off-heap location (overflow)
<p><li>	
<b>expiryTimerIntervalSecs</b>
	If expiration checker thread is used, the interval after which each run will happen. Default 3600.
<p><li>	
<b>evictionStrategy</b>
	Cache element eviction strategy: LRU,LFU,FIFO,LIFO. Default LRU
<p><li>	
<b>serializer</b>
	Since we are using off-heap memory storage, there is a cost associated with serialization/de-serialization. 
	For better performance, custom serialization scheme can be designed efficiently if we know the 
	target object structure. Need to implement com.offheap.cache.util.ISerializer. 
	Default using Kryo library (dependency)
 */
public abstract class VirtualMemCache<K, V> extends AbstractCache<K, V>
{
	public static interface EvictionTieCriteria<V> extends Comparator<V>
	{
		
	}
	private EvictionTieCriteria<V> evictTieCriteria;
	//private static final Logger	log						= Logger.getLogger(CacheImpl.class);
	
	//private ElasticHashMap<K, CacheItem<V>> offheapMap;
	private OffHeapMap<K, CacheItem<V>> offheapMap;
	/**
	 * Returns an unmodifiable view of the off-heap map. Used for testing purpose
	 * @return
	 */
	public Map<K, CacheItem<V>> getOffheapMap() {
		return Collections.unmodifiableMap(offheapMap);
	}
	/**
	 * 
	 * @author esutdal
	 *
	 */
	public final class MapEntryModificationCallback
	{
		/**
		 * removes from heap map
		 * @param key
		 * @param ttlSecs
		 */
		public void removeItem(K key, long ttlSecs)
		{
			if (key != null) {
				lockKey(key);
				try {
					CacheItem<V> item = heapMap.get(key);
					if (item != null) {
						heapMap.remove(key);
					}
					moveEntry(key, item, ttlSecs);
				} finally {
					unlockKey(key);
				}
			}
		}
		public boolean moveEntry(K key, long ttlSecs) {
			if (key != null) {
				lockKey(key);
				try {
					CacheItem<V> item = heapMap.get(key);
					return moveEntry(key, item, ttlSecs);
				} finally {
					unlockKey(key);
				}
			}
			return false;
		}
		/**
		 * puts to offheap map
		 * @param key
		 * @param item
		 * @param ttlSecs 
		 */
		private boolean moveEntry(K key, CacheItem<V> item, long ttlSecs) {
			if(item != null && !item.isExpired(ttlSecs))
			{
				setToOffHeap(key, item);
				return true;
			}
			return false;
		}
		
		private void setToOffHeap(K key, CacheItem<V> item)
		{
			offheapMap.set(key, item);
		}
	}
	private MapEntryModificationCallback callback;
	private ScheduledExecutorService thread;
	@Override
	public void init() throws CacheException
	{
		//offheapMap = new ElasticHashMap<>(getSerializer());
		offheapMap = new OffHeapMap<>(getSerializer());
		if(evictTieCriteria == null)
		{
			evictTieCriteria = new EvictionTieCriteria<V>() {
				
				@Override
				public int compare(V o1, V o2) {
					return 0;
				}
			};
		}
		callback = new MapEntryModificationCallback();
		switch(getEvictionStrategy())
		{
			case FIFO:
				heapMap = new FIFOMap<K, V>(getMaxEntriesOnHeap(), getTimeToLiveSecs(), callback);
				break;
			case LFU:
				heapMap = new LFUMap<K, V>(getMaxEntriesOnHeap(), getTimeToLiveSecs(), callback);
				break;
			case LIFO:
				heapMap = new LIFOMap<K, V>(getMaxEntriesOnHeap(), getTimeToLiveSecs(), callback);
				break;
			case LRU:
				heapMap = new LRUMap<K, V>(getMaxEntriesOnHeap(), getTimeToLiveSecs(), callback);
				break;
		}
				
		clear();
	
		if (isUseExpiryTimer())
		{
			startExpiryTimer();
			startOffHeapExpiryTimer();
			//System.out.println("VirtualMemCache.init() isUseExpiryTimer");
		}
	}
	
	private void startOffHeapExpiryTimer() {
		thread = Executors
				.newSingleThreadScheduledExecutor(new ThreadFactory()
				{

					@Override
					public Thread newThread(Runnable r)
					{
						Thread t = new Thread(r, getCacheName() + "-offheap-expiry-task");
						t.setDaemon(true);
						return t;
					}
				});

		thread.scheduleWithFixedDelay(
				new Runnable()
				{

					@Override
					public void run()
					{
						offheapMap.clear(new ClearCriteria() {

							@SuppressWarnings("unchecked")
							@Override
							public boolean apply(Object key,
									CacheItem<?> cachedItem) {
								lockKey((K) key);
								try
								{
									
									return cachedItem.isExpired(getTimeToLiveSecs());
									
								}
								finally
								{
									unlockKey((K) key);
								}
							}

							
						});
												
					}
				}, getExpiryTimerIntervalSecs(), getExpiryTimerIntervalSecs(),
				TimeUnit.SECONDS);
		//System.out.println("VirtualMemCache.startOffHeapExpiryTimer()"+thread);
	}
	@Override
	public void clear()
	{
		try {
			refresh(false);
		} catch (CacheException e) {
			// won't be thrown
		}
	}
	private boolean onRefresh = false;	
	@Override
	public void refresh(boolean reload) throws CacheException
	{
		lock.lock();
		onRefresh = true;
		try
		{
			
			heapMap.clear();
			offheapMap.clear();
			if (reload) {
				Map<K, V> entries = loadAll();
				if (entries != null) {
					//log.debug("entries.size() "+entries.size());
					for (Entry<K, V> entry : entries.entrySet()) {
						addToCache(entry.getKey(), entry.getValue());
					}
					//log.debug("cache.size() "+cache.size());
				}
			}
		}
		finally
		{
			onRefresh = false;
			c_onRefresh.signalAll();
			lock.unlock();
			
		}
	}

	@Override
	protected void handleIdleOnRemoval(Entry<K, CacheItem<V>> entry)
	{
		//already inside a monitor. DO NOT synchronize
		callback.setToOffHeap(entry.getKey(), entry.getValue());
	}
	private void waitOnRefresh()
	{
		if(onRefresh)
		{
			lock.lock();
			try
			{
				if (onRefresh)
				{
					c_onRefresh.await();
				}
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			finally
			{
				lock.unlock();
			}
		}
	}
	/**
	 * @deprecated Not implemented
	 * @return returns an empty list
	 */
	public List<V> getAll()
	{
		waitOnRefresh();		
		Collection<CacheItem<V>> items = heapMap.values();
		//log.debug(cacheName +": Cached items "+items.size());
		if(items != null && !items.isEmpty())
		{
			/*List<V> values = Relationals.collect(items, new Mapper<V, CacheItem<V>>()
			{
				@Override
				public V apply(CacheItem<V> value)
				{
					return value != null ? value.getValue() : null;
				}
			});
			//log.debug(cacheName +": Collected items "+values.size());
			return values;*/
		}
		return Collections.emptyList();
	}
	
	/**
	 * Gets the value associated with key. Will return null on exception.
	 * Override {@link #onLoadException(Object, CacheException)} to catch exceptions.
	 * 
	 */
	public V get(K key)
	{
		waitOnRefresh();
		
		lockKey(key);
		try
		{
			if (!heapMap.containsKey(key))
			{
				if(offheapMap.containsKey(key))
				{
					CacheItem<V> item = offheapMap.remove(key);
					if(item.isIdled(getTimeToIdleSecs()) || item.isExpired(getTimeToLiveSecs()))
					{
						loadForKey(key);
					}
					else
					{
						addToCache(key, item);
					}
				}
				else
				{
					loadForKey(key);
				}
			}
			else
			{
				CacheItem<V> item = heapMap.get(key);
				if(item.isIdled(getTimeToIdleSecs()) || item.isExpired(getTimeToLiveSecs()))
				{
					loadForKey(key);
				}
			}
			return getFromCache(key);
		} catch (CacheException e) {
			//this is handled
		}
		finally
		{
			unlockKey(key);
		}
		return null;	
	}
	/**
	 * To be overridden if needed to inspect exception
	 * @param key
	 * @param t
	 */
	protected void onLoadException(K key, CacheException t)
	{
		System.err.println("VirtualMemCache.onLoadException() Key: "+key);
		t.printStackTrace();
	}
	private V loadForKey(K key) throws CacheException
	{
		V value = null;
		try
		{
			value = load(key);
			if (value != null)
			{
				addToCache(key, value);
			}
		}
		catch (CacheException e)
		{
			onLoadException(key, e);
			throw e;
		}
		return value;
	}
	
	private void addToCache(K key, V value)
	{
		addToCache(key, new CacheItem<V>(value));

	}
	private void addToCache(K key, CacheItem<V> entry)
	{
		heapMap.put(key, entry);

	}
	public EvictionTieCriteria<V> getEvictTieCriteria() {
		return evictTieCriteria;
	}
	public void setEvictTieCriteria(EvictionTieCriteria<V> evictTieCriteria) {
		this.evictTieCriteria = evictTieCriteria;
	}
}
