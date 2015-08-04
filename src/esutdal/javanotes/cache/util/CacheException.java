package esutdal.javanotes.cache.util;

public class CacheException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9003255767687051414L;
	
	public CacheException() {
        super();
    }
    
    public CacheException(String message) {
        super(message);
    }

   
    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }

   
    public CacheException(Throwable cause) {
        super(cause);
    }


}
