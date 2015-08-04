package cache.test;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ShallowObject implements Serializable {

	

	@Override
	public String toString() {
		return "ShallowObject [sourceId=" + /*sourceId + ", special=" + special
				+ ", orderCode=" + orderCode + ", priority=" + priority
				+ ", prices=" + Arrays.toString(prices) + ", quantities="
				+ Arrays.toString(quantities) + ", suspectMatrix="
				+ suspectMatrix + */", name=" + name + "]";
	}

	private static final long serialVersionUID = 10275539472837495L;

	private long sourceId;
	private boolean special;
	private int orderCode;
	private int priority;
	private double[] prices;
	private long[] quantities;

	//private final Map<String, Map<String, Integer>> suspectMatrix = new HashMap<>();
	private String name;

	public ShallowObject() {
		
		/*Map<String, Integer> map = new HashMap<>();
		map.put("o", 0);
		map.put("n", 1);
		map.put("e", 2);
		suspectMatrix.put("one", map);
		
		map = new HashMap<>();
		map.put("f", 0);
		map.put("o", 1);
		map.put("u", 2);
		map.put("r", 3);
		suspectMatrix.put("four", map);*/
	}

	public ShallowObject(final long sourceId, final boolean special,
			final int orderCode, final int priority, final double[] prices,
			final long[] quantities) {
		this.sourceId = sourceId;
		this.special = special;
		this.orderCode = orderCode;
		this.priority = priority;
		this.prices = prices;
		this.quantities = quantities;
	}

	public ShallowObject(final long sourceId, final boolean special,
			final int orderCode, final int priority, final double[] prices,
			final long[] quantities,String name) {
		this.sourceId = sourceId;
		this.special = special;
		this.orderCode = orderCode;
		this.priority = priority;
		this.prices = prices;
		this.quantities = quantities;
		this.name = name;
	}

	public void write(final ByteBuffer byteBuffer) {
		byteBuffer.putLong(sourceId);
		byteBuffer.put((byte) (special ? 1 : 0));
		byteBuffer.putInt(orderCode);
		byteBuffer.putInt(priority);

		byteBuffer.putInt(prices.length);
		for (final double price : prices) {
			byteBuffer.putDouble(price);
		}

		byteBuffer.putInt(quantities.length);
		for (final long quantity : quantities) {
			byteBuffer.putLong(quantity);
		}
	}

	public static ShallowObject read(final ByteBuffer byteBuffer) {
		final long sourceId = byteBuffer.getLong();
		final boolean special = 0 != byteBuffer.get();
		final int orderCode = byteBuffer.getInt();
		final int priority = byteBuffer.getInt();

		final int pricesSize = byteBuffer.getInt();
		final double[] prices = new double[pricesSize];
		for (int i = 0; i < pricesSize; i++) {
			prices[i] = byteBuffer.getDouble();
		}

		final int quantitiesSize = byteBuffer.getInt();
		final long[] quantities = new long[quantitiesSize];
		for (int i = 0; i < quantitiesSize; i++) {
			quantities[i] = byteBuffer.getLong();
		}

		return new ShallowObject(sourceId, special, orderCode, priority,
				prices, quantities);
	}
	
	public String getName() {
		return name;
	}
	@Override
	public int hashCode() {
		/*final int prime = 31;
		long result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + orderCode;
		result = prime * result + Arrays.hashCode(prices);
		result = prime * result + priority;
		result = prime * result + Arrays.hashCode(quantities);
		result = prime * result + (int) (sourceId ^ (sourceId >>> 32));
		result = prime * result + (special ? 1231 : 1237);
		result = prime * result + created;
		return Long.;*/
		/*if(created == null)
			created = seq.incrementAndGet();
		return created;*/
		//return created == null ? created = seq.incrementAndGet() : created;
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ShallowObject other = (ShallowObject) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (orderCode != other.orderCode)
			return false;
		if (!Arrays.equals(prices, other.prices))
			return false;
		if (priority != other.priority)
			return false;
		if (!Arrays.equals(quantities, other.quantities))
			return false;
		if (sourceId != other.sourceId)
			return false;
		if (special != other.special)
			return false;
		return true;
	}

	public void setName(String name) {
		this.name = name;
	}

}
