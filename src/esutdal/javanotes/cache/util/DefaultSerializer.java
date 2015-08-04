package esutdal.javanotes.cache.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DefaultSerializer implements ISerializer {

	
	@Override
	public byte[] serialize(Object object) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try(ObjectOutputStream objOut = new ObjectOutputStream(out))
		{
			objOut.writeObject(object);
			out.flush();
			return out.toByteArray();
		}
	}

	@Override
	public Object deserialize(byte[] serialized) throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(serialized);
		try(ObjectInputStream objIn = new ObjectInputStream(in))
		{
			return objIn.readObject();
		}
	}

}
