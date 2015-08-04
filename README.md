# elastic-map-final
Implementation of a caching solution which overflows non-expired evicted items to an off-heap location.
Addition of new items entails selection of another item for eviction, if max size of cache is reached. Evicted item (selected based on eviction strategy), if not expired, overflow to off-heap memory. An idle detection facility is also provided, which if enabled, would schedule a periodic eviction of ALL (heap/off-heap) expired items and overflow of ALL idled items to off-heap area. 

The various parameters for cache configuration are as follows:<p>
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
