package esutdal.javanotes.cache.util;

import java.lang.reflect.Constructor;

public class Instantiator {
	
	private Instantiator(){}
	public static <T> T newInstance(Class<T> aClass, Class<?> consType, Object consParam) throws InstantiationException
	{
		try 
		{
			Constructor<T> cons = aClass.getConstructor(consType);
			cons.setAccessible(true);
			return cons.newInstance(consParam);
		} catch (Exception e) {
			throw new InstantiationException(e.getMessage());
		}
		
	}
	/**
	 * Uses native allocation for fast instantiation. WARNING! All initialization codes will be bypassed.
	 * @param aClass
	 * @return
	 * @throws InstantiationException
	 */
	public static <T> T newInstanceUnsafe(Class<T> aClass) throws InstantiationException
	{
		try 
		{
			Object obj = UnsafeWrapper.getUnsafe().allocateInstance(aClass);
			return aClass.cast(obj);
			
		} catch (Exception e) {
			throw new InstantiationException(e.getMessage());
		}
		
	}
	/**
	 * Uses native allocation for fast instantiation. WARNING! All initialization codes will be bypassed.
	 * @param className
	 * @return
	 * @throws InstantiationException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstanceUnsafe(String className) throws InstantiationException
	{
		Class<T> aClass = null;
		try 
		{
			aClass = (Class<T>) Class.forName(className);
			return newInstanceUnsafe(aClass);
			
		} catch (ClassNotFoundException e) {
			throw new InstantiationException(e.getMessage());
		} catch (Exception e) {
			throw new InstantiationException(e.getMessage());
		}
		
	}
	/**
	 * 
	 * @param className
	 * @return
	 * @throws InstantiationException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(String className) throws InstantiationException
	{
		Class<T> aClass = null;
		try 
		{
			aClass = (Class<T>) Class.forName(className);
			return aClass.newInstance();
		} catch (ClassNotFoundException e) {
			throw new InstantiationException(e.toString());
		} catch (Exception e) {
			throw new InstantiationException(e.toString());
		}
		
	}
	public static <T> T newInstance(Constructor<T> cons, Object...args) throws InstantiationException {
		try 
		{
			cons.setAccessible(true);
			return cons.newInstance(args);
		} catch (Exception e) {
			throw new InstantiationException(e.getMessage());
		}
	}
}
