package esutdal.javanotes.cache.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import esutdal.javanotes.cache.util.ISerializer;


/**
 * A hashed map which holds value off-heap. There is a cost of serialization associated
 * for any value fetching operation. Not thread safe.
 * <p>
 * 
 * @author esutdal
 *
 * @param <K>
 * @param <V>
 */
public class OffHeapMap<K, V> implements Map<K, V> {
	private final ISerializer serializer;
	private final HashMap<K, ItemPointer> map;
	/**
	 * 
	 * @param serializer
	 */
	public OffHeapMap(ISerializer serializer){
		this(32, serializer);
	}
	/**
	 * 
	 * @param initialCapacity
	 * @param serializer
	 */
	public OffHeapMap(int initialCapacity, ISerializer serializer) {
		this.serializer = serializer;
		map = new HashMap<K, ItemPointer>(initialCapacity);
		threads = Executors.newCachedThreadPool(new ThreadFactory() {
			private int n=0;
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, this.toString()+"-expiry-"+(n++));
				t.setDaemon(true);
				return t;
			}
		});
    }
	
	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	/**
	 * @deprecated This is costly. You probably won't want to use it!
	 */
	@Override
	public boolean containsValue(Object value) {
		Iterator<java.util.Map.Entry<K, ItemPointer>> iter = map.entrySet().iterator();
		if(value == null){
			while(iter.hasNext()){
				if(toObject(iter.next().getValue()) == null)
					return true;
			}
		}
		else{
			while(iter.hasNext()){
				if(value.equals(toObject(iter.next().getValue())))
					return true;
			}
		}
		return false;
	}

	@Override
	public V get(Object key) {
		if(map.containsKey(key))
		{
			ItemPointer ptr = map.get(key);
			if(ptr != null)
			{
				return toObject(ptr);
			}
		}
		return null;
		
	}

	
	@Override
	public V put(K key, V value) {
		V prev = null;
		ItemPointer p = toPointer(value);
		p = map.put(key, p);
		if(p != null){
			prev = toObject(p);
			p.dealloc();
		}
		return prev;
	}

	public void set(K key, V value) {
		ItemPointer p = toPointer(value);
		p = map.put(key, p);
		if(p != null){
			p.dealloc();
		}
	}
	
	@Override
	public V remove(Object key) {
		ItemPointer p = removeKey(key);
		V val = null;
		if (p != null) {
			val = toObject(p);
			p.dealloc();
		}
		return val;
	}
	private ItemPointer removeKey(Object key)
	{
		return map.remove(key);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if(m instanceof OffHeapMap){
			map.putAll(((OffHeapMap) m).map);
		}
		else{
			for(Entry<? extends K, ? extends V> entry : m.entrySet()){
				put(entry.getKey(), entry.getValue());
			}
		}
		
	}

	@Override
	public void clear() {
		for(Iterator<K> i = keySet().iterator();i.hasNext();){
			K key = i.next();
			ItemPointer p = map.get(key);
			if (p != null) {
				p.dealloc();
			}
			i.remove();
		}
		
	}
	public static interface ClearCriteria
	{
		public boolean apply(Object key, CacheItem<?> cachedItem);
	}
	private class ClearElementJob implements Callable<ClearElementJob>
	{
		private final K ptr;
		private ClearCriteria crit;
		public ClearElementJob(K ptr, ClearCriteria crit) {
			super();
			this.ptr = ptr;
			this.crit = crit;
		}
		@SuppressWarnings("unused")
		private boolean deallocated = false;
		@Override
		public ClearElementJob call() throws Exception {
			ItemPointer p = map.get(ptr);
			if (p != null) {
				try {
					CacheItem<V> item = unmarshall(p);
					if (crit.apply(ptr, item)) {
						setDeallocated(p.dealloc());
						removeKey(ptr);
						//System.out.println("OffHeapMap.ClearElementJob.call() -- expiring "+ptr);
					}
				} catch (Exception e) {
					throw e;
				}
			}
			return this;
		}
		public void setDeallocated(boolean deallocated) {
			this.deallocated = deallocated;
		}
		
	}
	private ExecutorService threads;
	/**
	 * Clears using a selection criteria. To be used from expiration timer
	 * @param crit
	 */
	public void clear(ClearCriteria crit) {
		//System.out.println("OffHeapMap.clear()");
		List<Future<ClearElementJob>> futures = new ArrayList<>(size());
		for(Iterator<K> i = keySet().iterator();i.hasNext();){
			K key = i.next();
			futures.add(threads.submit(new ClearElementJob(key, crit)));
			//System.out.println("OffHeapMap.clear() added");					
		}
		for(Future<ClearElementJob> f : futures)
		{
			try {
				f.get();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.err.println("OffHeapMap.clear() "+e.toString());
			}
		}
		
	}
	@Override
	public void finalize()
	{
		try {
			super.finalize();
		} catch (Throwable e) {
			
		}
		clear();
		threads.shutdown();
		//System.out.println("OffHeapMap.finalize()");
	}
	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<V> values() {
		ArrayList<V> coll = new ArrayList<V>();
		for(ItemPointer each : map.values()){
			if (each != null) {
				coll.add(toObject(each));
			}
		}
		return coll;
	}

	@SuppressWarnings("unchecked")
	private CacheItem<V> unmarshall(ItemPointer each) throws IOException, Exception {
		return (CacheItem<V>) CacheItem.deserialize(ByteBuffer.wrap(each.getBytes()), serializer);
	}
	private ItemPointer marshall(CacheItem<V> item) throws IOException, Exception {
		return new ItemPointer(item.serialize(serializer).array());
	}
	private ItemPointer toPointer(V value)
	{
		CacheItem<V> item = new CacheItem<>(value);
		try {
			return marshall(item);
		} catch (Exception e) {
			throw new RuntimeException("Unable to marshall into object pointer", e);
		}
	}
	private V toObject(ItemPointer each) {
		try {
			CacheItem<V> item = unmarshall(each);
			return item.getValue();
		} catch (Exception e) {
			throw new RuntimeException("Unable to unmarshall from object pointer", e);
		}
	}
	/**
	 * @deprecated This works, but keep note it's EXPENSIVE and can very well result in out of memory
	 */
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		Map<K, V> _map = new HashMap<>();
		for(Entry<K, ItemPointer> entry : map.entrySet())
		{
			K k = entry.getKey();
			V v = entry.getValue() != null ? toObject(entry.getValue()) : null;
			
			_map.put(k, v);
		}
		return _map.entrySet();
	}

}
