package esutdal.javanotes.cache.core;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import esutdal.javanotes.cache.EvictionStrategy;
import esutdal.javanotes.cache.util.CacheException;
import esutdal.javanotes.cache.util.ISerializer;
/**
 * Base class for common functionalities
 * @author esutdal
 *
 * @param <K>
 * @param <V>
 */
public abstract class AbstractCache<K,V>
{
	private final ConcurrentMap<K, Object> keyLock = new ConcurrentHashMap<>();
	private final Object dummy = new Object();
	protected final void unlockKey(K key)
	{
		if(keyLock.remove(key, dummy))
		{
			synchronized (dummy) {
				dummy.notifyAll();
				//System.out.println("AbstractCache.unlockKey() "+key);
			}
		}
	}
	protected final void lockKey(K key)
	{
		//System.out.println("AbstractCache.lockKey() "+key);
		while(keyLock.putIfAbsent(key, dummy) != null)
		{
			synchronized (dummy) {
				if(keyLock.putIfAbsent(key, dummy) != null)
				{
					try {
						dummy.wait();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				else{
					//System.out.println("AbstractCache.lockKey() return --------------- "+key);
					return;
				}
			}
		}
		//System.out.println("AbstractCache.lockKey() return --------------- "+key);
	}
	/**
	 * Refresh the cache. It can simply invoke {@link #clear()}. If reload set to true, reload the cache with all elements
	 * @param reload
	 * @throws CacheException
	 */
	public abstract void refresh(boolean reload) throws CacheException;
	/**
	 * Get value from cache (if hit), else load and get (on miss)
	 * @param key
	 * @return
	 */
	public abstract V get(K key);
	/**
	 * Returns an unmodifiable view of the underlying map. Used for testing purpose
	 * @return
	 */
	public Map<K, CacheItem<V>> getHeapMap() {
		return Collections.unmodifiableMap(heapMap);
	}
	public abstract List<V> getAll();
	protected Map<K, CacheItem<V>> heapMap;
	
	/**
	 * Invoked by expiration thread if an entry is idled but not expired.
	 * @param entry
	 */
	protected void handleIdleOnRemoval(Entry<K, CacheItem<V>> entry){}
	
	protected final ReentrantLock	lock	= new ReentrantLock();
	final protected Condition c_onRefresh = lock.newCondition();
	protected final V getFromCache(K key)
	{
		CacheItem<V> item = heapMap.get(key);
		return item != null ? item.getValue() : null;
	}
		
	/**
	 * Method to override for lazy loading. Thread safe
	 * 
	 * @param key
	 * @return
	 * @throws CacheLoadException
	 */
	protected abstract V load(K key) throws CacheException;

	/**
	 * Method to override for loading when the cache initializes or refreshes. Thread safe
	 * 
	 * @return
	 * @throws CacheLoadException
	 */
	protected abstract Map<K, V> loadAll() throws CacheException;
	/**
	 * Any initialization code. Will be invoked once after the cache class is loaded
	 * @throws CacheException
	 */
	public abstract void init() throws CacheException;
	private ScheduledExecutorService	thread;
	/**
	 * This should not be needed
	 */
	protected void startExpiryTimer()
	{
		thread = Executors
				.newSingleThreadScheduledExecutor(new ThreadFactory()
				{

					@Override
					public Thread newThread(Runnable r)
					{
						Thread t = new Thread(r, cacheName + "-expiry-task");
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
						//log.debug("Running expiry timer ");
						for (Iterator<Entry<K, CacheItem<V>>> iter = heapMap
								.entrySet().iterator(); iter.hasNext();)
						{
							Entry<K, CacheItem<V>> key = iter.next();
							CacheItem<V> item = key.getValue();
							if (item.isExpired(timeToLiveSecs)
									|| item.isIdled(timeToIdleSecs))
							{
								lockKey(key.getKey());
								try
								{
									if (item.isExpired(timeToLiveSecs))
									{
										iter.remove();
									}
									else if(item.isIdled(timeToIdleSecs))
									{
										handleIdleOnRemoval(key);
										iter.remove();
										
									}
								}
								catch (Exception e)
								{
									System.out
											.println("AbstractCache.startExpiryTimer().new Runnable() {...}.run()");
									e.printStackTrace();
								}
								finally
								{
									unlockKey(key.getKey());
								}
							}
						}
						
					}
				}, expiryTimerIntervalSecs, expiryTimerIntervalSecs,
				TimeUnit.SECONDS);
	}
	
	/**
	 * The maximum number of seconds an element can exist in the cache
	 * regardless of use. The element expires at this limit and will no longer
	 * be returned from the cache. The default value is 0, which means no TTL
	 * eviction takes place (infinite lifetime).
	 */
	long				timeToLiveSecs			= 0;

	/**
	 * The maximum number of seconds an element can exist in the cache without
	 * being accessed. The element expires at this limit and will no longer be
	 * returned from the cache. The default value is 0, which means no TTI
	 * eviction takes place (infinite lifetime).
	 */
	long				timeToIdleSecs			= 0;
	String			cacheName;

	/**
	 * The maximum number of items to be present in the cache. Adding anymore
	 * item will remove the LRU item.
	 */
	int					maxEntriesOnHeap		= 1000;

	/**
	 * Whether to use an expiry checker thread to remove expired items
	 */
	boolean				useExpiryTimer			= false;

	/**
	 * If expiry checker thread is used, the interval after which each run will
	 * happen
	 */
	int					expiryTimerIntervalSecs	= 3600;
	
	/**
	 * The strategy to be used to evict entries from the cache
	 */
	EvictionStrategy evictionStrategy = EvictionStrategy.LRU;
	/**
	 * default using Kryo library
	 */
	private ISerializer serializer;
	
	@Override
	public int hashCode()
	{
		return cacheName.hashCode();
	}
	
	public long getTimeToLiveSecs() {
		return timeToLiveSecs;
	}
	public void setTimeToLiveSecs(long timeToLiveSecs) {
		this.timeToLiveSecs = timeToLiveSecs;
	}
	public long getTimeToIdleSecs() {
		return timeToIdleSecs;
	}
	public void setTimeToIdleSecs(long timeToIdleSecs) {
		this.timeToIdleSecs = timeToIdleSecs;
	}
	public String getCacheName() {
		return cacheName;
	}
	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}
	public int getMaxEntriesOnHeap() {
		return maxEntriesOnHeap;
	}
	public void setMaxEntriesOnHeap(int maxEntriesOnHeap) {
		this.maxEntriesOnHeap = maxEntriesOnHeap;
	}
	public boolean isUseExpiryTimer() {
		return useExpiryTimer;
	}
	public void setUseExpiryTimer(boolean useExpiryTimer) {
		this.useExpiryTimer = useExpiryTimer;
	}
	public int getExpiryTimerIntervalSecs() {
		return expiryTimerIntervalSecs;
	}
	public void setExpiryTimerIntervalSecs(int expiryTimerIntervalSecs) {
		this.expiryTimerIntervalSecs = expiryTimerIntervalSecs;
	}
	public EvictionStrategy getEvictionStrategy() {
		return evictionStrategy;
	}
	public void setEvictionStrategy(EvictionStrategy evictionStrategy) {
		this.evictionStrategy = evictionStrategy;
	}
	@Override
	public boolean equals(Object cache)
	{
		if (cache == null)
			return false;
		if (cache == this)
			return true;
		if (!(cache instanceof AbstractCache<?, ?>))
			return false;

		AbstractCache<?,?> rhs = (AbstractCache<?,?>) cache;
		return cacheName.equals(rhs.cacheName);
		
	}
	/**
	 * Implement clearing of the cache
	 */
	public void clear(){}
	public ISerializer getSerializer() {
		return serializer;
	}
	public void setSerializer(ISerializer serializer) {
		this.serializer = serializer;
	}
}
