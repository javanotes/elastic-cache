package cache.test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import esutdal.javanotes.cache.CacheBuilder;
import esutdal.javanotes.cache.CacheManager;
import esutdal.javanotes.cache.EvictionStrategy;
import esutdal.javanotes.cache.VirtualMemCache;
import esutdal.javanotes.cache.util.CacheException;

/**
 * Test suite for cache functional testing
 * @author esutdal
 *
 */
public class FuncTest {

	@SuppressWarnings("unused")
	static void declarative() throws CacheException
	{
		
		ShallowObject s = CacheManager.getFromCache("somecache", "key1");
		//System.out.println(s);
		//System.out.println(s.hashCode());
		
		ShallowObject s1 = CacheManager.getFromCache("somecache", "key1");
		//System.out.println(s1);
		if(!s.equals(s1) || s.hashCode() != s1.hashCode()){
			System.err.println("declarative Failed: not same object");
			return;
		}
		
		ShallowObject s_ = CacheManager.getFromCache("somecache", "key1");
		
		s = CacheManager.getFromCache("somecache", "key2");
		//System.out.println(s);
		//System.out.println(s.hashCode());
		
		s1 = CacheManager.getFromCache("somecache", "key2");
		//System.out.println(s1);
		//System.out.println(s1.hashCode());
		if(!s.equals(s1) || s.hashCode() != s1.hashCode()){
			System.err.println("declarative Failed: not same object");
			return;
		}
		
		ShallowObject s3 = CacheManager.getFromCache("somecache", "key3");
		//System.out.println(s3);
		//System.out.println(s3.hashCode());
		
		s = CacheManager.getFromCache("somecache", "key1");
		//System.out.println(s);
		//System.out.println(s.hashCode());
		if(!s.equals(s_) || s.hashCode() == s_.hashCode()){
			System.err.println("declarative Failed: not equal but different object");
			return;
		}
		CacheManager.clear();
		System.out.println("declarative passed ..");
	}
	
	@SuppressWarnings("unused")
	static void programmatic() throws CacheException
	{
		
		VirtualMemCache<String, ShallowObject> cache = new CacheBuilder()
		.setCacheName("somecache")
		.setMaxEntriesOnHeap(2)
		.build(new SomeCache());
		
		ShallowObject s = cache.get("key1");
		//System.out.println(s);
		//System.out.println(s.hashCode());
		
		ShallowObject s1 = cache.get("key1");
		//System.out.println(s1);
		if(!s.equals(s1) || s.hashCode() != s1.hashCode()){
			System.err.println("programmatic Failed: not same object");
			return;
		}
		
		ShallowObject s_ = cache.get("key1");
		
		s = cache.get("key2");
		//System.out.println(s);
		//System.out.println(s.hashCode());
		
		s1 = cache.get("key2");
		//System.out.println(s1);
		//System.out.println(s1.hashCode());
		if(!s.equals(s1) || s.hashCode() != s1.hashCode()){
			System.err.println("programmatic Failed: not same object");
			return;
		}
		
		ShallowObject s3 = cache.get("key3");
		//System.out.println(s3);
		//System.out.println(s3.hashCode());
		
		s = cache.get("key1");
		//System.out.println(s);
		//System.out.println(s.hashCode());
		if(!s.equals(s_) || s.hashCode() == s_.hashCode()){
			System.err.println("programmatic Failed: not equal but different object");
			return;
		}
		
		cache.clear();
		System.out.println("programmatic passed ..");
	}
	static Set<Integer> set = new HashSet<>();
	
	static void testLIFO() throws CacheException
	{


		final VirtualMemCache<Integer, ShallowObject> cache = new CacheBuilder()
		.setCacheName("anothercache")
		.setMaxEntriesOnHeap(3)
		.setEvictionStrategy(EvictionStrategy.LIFO)
		.build(
			new VirtualMemCache<Integer, ShallowObject>() {

				@Override
				protected ShallowObject load(Integer key) throws CacheException {
					/*if(!set.add(key))
					{
						System.out.println("Reload!!"+key);
					}*/
					ShallowObject obj = new ShallowObject(
				            1010L, true, key, 99,
				            new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0},
				            new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},Integer.toHexString(key));
					
					return obj;
				}
	
				@Override
				protected Map<Integer, ShallowObject> loadAll() throws CacheException {
					// TODO Auto-generated method stub
					return null;
				}
		});
		
		
		
		try {
			ShallowObject o = cache.get(1);
			
			o = cache.get(2);
			long h2 = o.hashCode();
			o = cache.get(3);
			long h3 = o.hashCode();
			
			//LI is 3. should be removed
			o = cache.get(4);
			
			//2 should be from cache
			o = cache.get(2);
			if(h2 != o.hashCode()){
				System.err.println("Failed LIFO: Expected cache hit");
				return;
			}
			
			//should be a new instance
			o = cache.get(3);
			if(h3 == o.hashCode()){
				System.err.println("Failed LIFO: Expected cache miss");
				return;
			}
		} finally {
			cache.clear();
		}
		
		System.out.println("testLIFO passed ..");
	
	
	}
	
	static void testFIFO() throws CacheException
	{

		final VirtualMemCache<Integer, ShallowObject> cache = new CacheBuilder()
		.setCacheName("anothercache")
		.setMaxEntriesOnHeap(3)
		.setEvictionStrategy(EvictionStrategy.FIFO)
		.build(
			new VirtualMemCache<Integer, ShallowObject>() {

				@Override
				protected ShallowObject load(Integer key) throws CacheException {
					/*if(!set.add(key))
					{
						System.out.println("Reload!!"+key);
					}*/
					ShallowObject obj = new ShallowObject(
				            1010L, true, key, 99,
				            new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0},
				            new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},Integer.toHexString(key));
					
					return obj;
				}
	
				@Override
				protected Map<Integer, ShallowObject> loadAll() throws CacheException {
					// TODO Auto-generated method stub
					return null;
				}
		});
		
		
		
		try {
			ShallowObject o = cache.get(3);
			long h3 = o.hashCode();
			o = cache.get(2);
			long h2 = o.hashCode();
			o = cache.get(1);
			
			
			//FI is 3. should be removed
			o = cache.get(4);
			
			//2 should be from cache
			o = cache.get(2);
			if(h2 != o.hashCode()){
				System.err.println("Failed FIFO: Expected cache hit");
				return;
			}
			
			//should be a new instance
			o = cache.get(3);
			if(h3 == o.hashCode()){
				System.err.println("Failed FIFO: Expected cache miss");
				return;
			}
		} finally {
			cache.clear();
		}
		
		System.out.println("testFIFO passed ..");
	
	}
	
	static void testLRU() throws CacheException
	{
		final VirtualMemCache<Integer, ShallowObject> cache = new CacheBuilder()
		.setCacheName("anothercache")
		.setMaxEntriesOnHeap(3)
		.setEvictionStrategy(EvictionStrategy.LRU)
		.build(
			new VirtualMemCache<Integer, ShallowObject>() {

				@Override
				protected ShallowObject load(Integer key) throws CacheException {
					/*if(!set.add(key))
					{
						System.out.println("Reload!!"+key);
					}*/
					ShallowObject obj = new ShallowObject(
				            1010L, true, key, 99,
				            new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0},
				            new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},Integer.toHexString(key));
					
					return obj;
				}
	
				@Override
				protected Map<Integer, ShallowObject> loadAll() throws CacheException {
					// TODO Auto-generated method stub
					return null;
				}
		});
		
		
		
		try {
			ShallowObject o = cache.get(1);
			o = cache.get(2);
			long h2 = o.hashCode();
			o = cache.get(3);
			long h3 = o.hashCode();
			o = cache.get(1);
			o = cache.get(2);
			//LRU is 3. should be removed
			o = cache.get(4);
			
			//2 should be from cache
			o = cache.get(2);
			if(h2 != o.hashCode()){
				System.err.println("Failed LRU: Expected cache hit");
				return;
			}
			
			//should be a new instance
			o = cache.get(3);
			if(h3 == o.hashCode()){
				System.err.println("Failed LRU: Expected cache miss");
				return;
			}
		} finally {
			cache.clear();
		}
		
		System.out.println("testLRU passed ..");
	}
	
	static void testLFU() throws CacheException
	{
		final VirtualMemCache<Integer, ShallowObject> cache = new CacheBuilder()
		.setCacheName("anothercache")
		.setMaxEntriesOnHeap(3)
		.setEvictionStrategy(EvictionStrategy.LFU)
		.build(
			new VirtualMemCache<Integer, ShallowObject>() {

				@Override
				protected ShallowObject load(Integer key) throws CacheException {
					/*if(!set.add(key))
					{
						System.out.println("Reload!!"+key);
					}*/
					//System.out.println("CacheTest.testLFU().new VirtualMemCache() {...}.load() "+key);
					ShallowObject obj = new ShallowObject(
				            1010L, true, key, 99,
				            new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0},
				            new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},Integer.toHexString(key));
					
					return obj;
				}
	
				@Override
				protected Map<Integer, ShallowObject> loadAll() throws CacheException {
					// TODO Auto-generated method stub
					return null;
				}
		});
		
		try {
			ShallowObject o = cache.get(1);
			o = cache.get(2);
			long h2 = o.hashCode();
			o = cache.get(3);
			long h3 = o.hashCode();
			o = cache.get(1);
			o = cache.get(2);
			o = cache.get(1);
			o = cache.get(1);
			o = cache.get(2);
			o = cache.get(2);
			o = cache.get(3);
			
			//LFU is 3. should be removed
			o = cache.get(4);
			
			//2 should be from cache
			o = cache.get(2);
			if(h2 != o.hashCode()){
				System.err.println("Failed LFU: Expected cache hit");
				return;
			}
			
			//should be a new instance
			o = cache.get(3);
			if(h3 == o.hashCode()){
				System.err.println("Failed LFU: Expected cache miss");
				return;
			}
		} finally {
			cache.clear();
		}
		System.out.println("testLFU passed ..");
	}
	
	static void expiryTimer() throws CacheException
	{
		
		VirtualMemCache<String, ShallowObject> cache = new CacheBuilder()
		.setCacheName("somecache")
		.setMaxEntriesOnHeap(2)
		.setUseExpiryTimer(true)
		.setExpiryTimerIntervalSecs(2)
		.setTimeToLiveSecs(1)
		.build(new SomeCache());
		
		ShallowObject s = cache.get("key1");
		ShallowObject s1 = cache.get("key1");//should be same
		if(!s.equals(s1) || s.hashCode() != s1.hashCode()){
			System.err.println("expiryTimer Failed: not same object");
			return;
		}
		cache.get("key2");
		cache.get("key3");//now key1 should overflow to offheap
		boolean key1Present = cache.getOffheapMap().containsKey("key1");
		if(!key1Present){
			System.err.println("expiryTimer Failed: not overflown to off-heap");
			return;
		}
			
		System.out.println("FuncTest.expiryTimer() running");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			
		}
		boolean keyPresent = cache.getHeapMap().containsKey("key2");
		if(keyPresent){
			System.err.println("expiryTimer Failed: not expired from on-heap");
			return;
		}
		keyPresent = cache.getHeapMap().containsKey("key3");
		if(keyPresent){
			System.err.println("expiryTimer Failed: not expired from on-heap");
			return;
		}
		key1Present = cache.getOffheapMap().containsKey("key1");//should have been removed from offheap by now
		if(key1Present){
			System.err.println("expiryTimer Failed: not expired from off-heap");
			return;
		}
		s1 = cache.get("key1");//
		//System.out.println(s1);
		if(!s.equals(s1) || s.hashCode() == s1.hashCode()){
			System.err.println("expiryTimer Failed: not equal but different object");
			return;
		}
		
		ShallowObject s_ = cache.get("key1");
		
		s = cache.get("key2");
		//System.out.println(s);
		//System.out.println(s.hashCode());
		
		s1 = cache.get("key2");
		//System.out.println(s1);
		//System.out.println(s1.hashCode());
		if(!s.equals(s1) || s.hashCode() != s1.hashCode()){
			System.err.println("expiryTimer Failed: not same object");
			return;
		}
		
		cache.get("key3");
		
		s = cache.get("key1");
		//System.out.println(s);
		//System.out.println(s.hashCode());
		if(!s.equals(s_) || s.hashCode() == s_.hashCode()){
			System.err.println("expiryTimer Failed: not equal but different object");
			return;
		}
		
		cache.clear();
		System.out.println("expiryTimer passed ..");
	}
	/**
	 * @param args
	 * @throws CacheException 
	 */
	public static void main(String[] args) throws CacheException {
		programmatic();
		declarative();
		expiryTimer();
		testLRU();
		testLFU();
		testFIFO();
		testLIFO();
	}

}
