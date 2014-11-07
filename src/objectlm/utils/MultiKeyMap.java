package objectlm.utils;

import java.util.HashMap;

public class MultiKeyMap<K,F,V> extends HashMap<Integer, V> {

	
	/**
     * Gets the hash code for the specified multi-key.
     *
     * @param key1  the first key
     * @param key2  the second key
     * @return the hash code
     */
    int hash(final Object key1, final Object key2) {
        int h = 0;
        if (key1 != null) {
            h ^= key1.hashCode();
        }
        if (key2 != null) {
            h ^= key2.hashCode();
        }
        h += ~(h << 9);
        h ^=  h >>> 14;
        h +=  h << 4;
        h ^=  h >>> 10;
        return h;
    }
    
    /**
     * Extends Map to have the ability to use a multi key
     * to be accessed.
     * 
     * @param key1
     * @param key2
     * @param value
     * @return
     */
    public V put(K key1, F key2, V value) {
    	return super.put(hash(key1, key2), value);
    }
    
    /**
     * Extends Map to have the ability to use a multi key
     * to be accessed.
     * 
     * @param key1
     * @param key2
     * @param value
     * @return
     */
    public V get(K key1, F key2) {
    	return super.get(hash(key1, key2));
    }
    
    public V get_with_default(K key1, F key2, V default_value) {
    	V returned = this.get(key1, key2);
    	if (returned != null) {
    		return returned;
    	}
    	return default_value;
    }
    
    /**
     * Extends Map to have the ability to use a multi key
     * to remove elements.
     * 
     * @param key1
     * @param key2
     * @param value
     * @return
     */
    public V remove(K key1, F key2) {
    	return super.remove(hash(key1, key2));
    }
    
    /**
     * Check whether the pair key1, and key2 contain
     * something in this Map.
     * 
     * @param key1
     * @param key2
     * @param value
     * @return
     */
    public boolean containsKey(K key1, F key2) {
    	return super.containsKey(hash(key1,key2));
    }
	/**
	 * 
	 */
	private static final long serialVersionUID = -3399498810883223961L;
	
}
