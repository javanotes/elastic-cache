package cache.test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import esutdal.javanotes.cache.CacheBuilder;
import esutdal.javanotes.cache.CacheManager;
import esutdal.javanotes.cache.VirtualMemCache;
import esutdal.javanotes.cache.util.CacheException;

/**
 * Test suite for cache functional testing
 * @author esutdal
 *
 */
public class PerfTest {

	private static final int iterations = 100000;
	private static final int nThreads = 2;


	static void declarative() throws CacheException
	{
		ExecutorService threads = Executors.newFixedThreadPool(nThreads);
		final AtomicInteger ai = new AtomicInteger();
		final Random r = new Random();
		for (int i = 0; i < nThreads; i++) {
			threads.submit(new Runnable() {
				
				@Override
				public void run() {
					while(ai.incrementAndGet() <= iterations)
					{
						try {
							CacheManager.getFromCache("somecache", r.nextInt(1000)+"");
						} catch (CacheException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
			});
		}
		threads.shutdown();
		System.out.println("PerfTest.declarative() running iterations..");
		try {
			threads.awaitTermination(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CacheManager.clear();
		System.out.println("declarative passed ..");
	}
	
	static void programmatic() throws CacheException
	{
		
		final VirtualMemCache<String, ShallowObject> cache = new CacheBuilder()
		.setCacheName("somecache")
		.setMaxEntriesOnHeap(200)
		.build(new SomeCache());
		
		ExecutorService threads = Executors.newFixedThreadPool(nThreads);
		final AtomicInteger ai = new AtomicInteger();
		final Random r = new Random();
		for (int i = 0; i < nThreads; i++) {
			threads.submit(new Runnable() {
				
				@Override
				public void run() {
					while(ai.incrementAndGet() <= iterations)
					{
						cache.get(r.nextInt(1000)+"");
					}
					
				}
			});
		}
		threads.shutdown();
		System.out.println("PerfTest.programmatic() running iterations ..");
		try {
			threads.awaitTermination(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cache.clear();
		System.out.println("programmatic passed ..");
	}
	static Set<Integer> set = new HashSet<>();
	
	
	/**
	 * @param args
	 * @throws CacheException 
	 */
	public static void main(String[] args) throws CacheException {
		programmatic();
		declarative();
		
	}

}
