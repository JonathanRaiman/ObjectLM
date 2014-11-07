package objectlm.utils;

public class MultiKeyMapNumeric<K,F> extends MultiKeyMap<K,F, Double> {
	
	public void increment(K key1, F key2, double value) {
		int key = hash(key1, key2);
		super.put(key, this.get(key) + value);
	}
	
	public void increment_equals(K key1, F key2, double value, double default_val) {
		int key = hash(key1, key2);
		if (this.get(key) != null) {
			super.put(key, this.get(key) + value);
		} else {
			super.put(key, value + default_val);
		}
	}
	
	public void increment_equals(K key1, F key2, double value) {
		int key = hash(key1, key2);
		if (this.get(key) != null) {
			super.put(key, this.get(key) + value);
		} else {
			super.put(key, value );
		}
	}
	
	public void decrement(K key1, F key2, double value) {
		int key = hash(key1, key2);
		super.put(key, this.get(key) - value);
	}
	
	public void multiply(K key1, F key2, double value) {
		int key = hash(key1, key2);
		super.put(key, this.get(key) * value);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
}
