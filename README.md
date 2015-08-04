# elastic-map-final
Implementation of a caching solution which overflows non-expired evicted items to an off-heap location.
Addition of new items entails selection of another item for eviction, if max size of cache is reached. Evicted item (selected based on eviction strategy), if not expired, overflow to off-heap memory. An idle detection facility is also provided, which if enabled, would schedule a periodic eviction of ALL (heap/off-heap) expired items and overflow of ALL idled items to off-heap area. 
