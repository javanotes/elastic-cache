package cache.test;

import java.util.Map;

import esutdal.javanotes.cache.VirtualMemCache;
import esutdal.javanotes.cache.util.CacheException;


public class SomeCache extends VirtualMemCache<String, ShallowObject> {

	@Override
	protected ShallowObject load(String key) throws CacheException {
		//System.out.println("Miss->"+key);
		ShallowObject so =
		        new ShallowObject(
		            1010L, true, 777, 99,
		            new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0},
		            new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
		
		so.setName(key);
		
		return so;
	}

	@Override
	protected Map<String, ShallowObject> loadAll() throws CacheException {
		// TODO Auto-generated method stub
		return null;
	}

}
