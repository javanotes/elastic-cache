package esutdal.javanotes.cache;

public enum EvictionStrategy{
	/**
	 * Least recently used
	 */
	LRU,
	/**
	 * Least frequently used
	 */
	LFU,
	/**
	 * Last in (stack order)
	 */
	LIFO,
	/**
	 * First in (queue order)
	 */
	FIFO
	}