package esutdal.javanotes.cache.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


/**
 * Serializer extension using Kryo library
 * 
 * @author esutdal
 * 
 * @param <T>
 */
public class KryoSerializer implements ISerializer {

	
	private final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {

		
		@Override
		protected Kryo initialValue() {
			Kryo kryo = new Kryo();
			return kryo;
		}
	};

	public KryoSerializer() {
		
	}


	@Override
	public byte[] serialize(Object object) throws Exception {
		Output out = new Output(512);
		Kryo kryo = kryoThreadLocal.get();
		kryo.writeClassAndObject(out, object);
		return out.toBytes();
	}

	@Override
	public Object deserialize(byte[] serialized) throws Exception {
		Input in = new Input(serialized);
		Kryo kryo = kryoThreadLocal.get();
		return kryo.readClassAndObject(in);

	}

}
