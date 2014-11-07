package objectlm.utils;

public class SymmetricMultiKeyMapNumeric<K extends Comparable<K>> extends MultiKeyMapNumeric<K,K> {
	
	/**
     * Gets the hash code for the specified multi-key.
     *
     * @param key1  the first key
     * @param key2  the second key
     * @return the hash code
     */
	int hash(final K key1, final K key2) {
    	// force increasing order:
    	if (key1.compareTo(key2) > 0) {
    		return hash(key2, key1);
    	}
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
	 * 
	 */
	private static final long serialVersionUID = -7965208992949200867L;

}
