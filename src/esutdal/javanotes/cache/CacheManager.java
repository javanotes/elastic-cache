package esutdal.javanotes.cache;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.xml.sax.SAXException;

import esutdal.javanotes.cache.core.AbstractCache;
import esutdal.javanotes.cache.util.CacheConfig;
import esutdal.javanotes.cache.util.CacheException;

/**
 * Class to be used for declarative usage of virtual memcache
 * @author esutdal
 *
 */
public class CacheManager
{
	private static final Map<String, VirtualMemCache<?,?>> cacheList = new HashMap<>();
	//private static final Logger log = Logger.getLogger(CacheManager.class);
	
	private static final Map<String,Map<String, String>> config = new WeakHashMap<>();
	
	public static void clear()
	{
		while(!mutex.compareAndSet(false, true));
		try {
			for(AbstractCache<?,?> cache : cacheList.values())
			{
				cache.clear();
			}
		} finally {
			mutex.compareAndSet(true, false);
		}
		
	}
	private static void loadConfig() throws ParseException, SAXException, IOException
	{
		List<Map<String, String>> l;
		try
		{
			String cfgFile = System.getProperty("cache.config.xml");
			if(cfgFile == null || cfgFile.trim().equals(""))
				cfgFile = "/cache-config.xml";
			
			l = CacheConfig.read(cfgFile);
			for(Map<String, String> m : l)
			{
				config.put(m.get("name"), m);
			}
		}
		catch (ParseException e)
		{
			throw e;
		}
		
	}
	
	static
	{
		try {
			loadConfig();
		} catch (ParseException e) {
			throw new RuntimeException("Invalid config file ", e);
		} catch (SAXException e) {
			throw new RuntimeException("Invalid config file ", e);
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	/**
	 * Gets a value from the named cache with the given key
	 * @param <K>
	 * @param <V>
	 * @param cacheName
	 * @param key
	 * @return
	 * @throws CacheException 
	 */
	public static <K, V> V getFromCache(String cacheName, K key) throws CacheException
	{
		AbstractCache<K, V> cache;
		V value = null;
		try 
		{
			cache = getCache(cacheName);
			value = cache != null ? cache.get(key) : null;
			
		} catch (CacheException e) {
			throw e;
		}
		return value;
		
	}
	
		
	static final AtomicBoolean mutex = new AtomicBoolean();
	@SuppressWarnings("unchecked")
	private static <K, V> AbstractCache<K,V> getCache(String cacheName) throws CacheException
	{
		while(!cacheList.containsKey(cacheName))
		{
			while(!mutex.compareAndSet(false, true));
			try 
			{
				if (!cacheList.containsKey(cacheName))
				{
					try
					{
						VirtualMemCache<?, ?> cache = (VirtualMemCache<?, ?>) createCache(cacheName);
						putCache(cacheName, cache);
					}
					catch (CacheException e)
					{
						throw e;
					}
				}
			} finally {
				mutex.compareAndSet(true, false);
			}
			
		}
		
		return (AbstractCache<K, V>) cacheList.get(cacheName);
	}
	static void putCache(String cacheName, VirtualMemCache<?, ?> cache)
	{
		VirtualMemCache<?, ?> cache1 = cacheList.put(cacheName, cache);
		if(cache1 != null)
			cache1.clear();
	}
	/**
	 * This method is already invoked from within a critical section. Do not synchronize any further
	 * @param cacheClass
	 * @return 
	 */
	private static AbstractCache<?, ?> createCache(String name) throws CacheException
	{
			Map<String, String> map = config.remove(name);
			if(map != null)
			{
				AbstractCache<?, ?> cache;
				try 
				{
					cache = CacheBuilder.build(map, name);
					
				} catch (InstantiationException e) 
				{
					throw new CacheException(e);
				}
				
				cache.init();
				return cache;
			}
			else
			{
				throw new CacheException("Cache ["+name+"] not found");
			}
	}
	
	
}
