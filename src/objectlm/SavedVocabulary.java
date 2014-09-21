package objectlm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SavedVocabulary {
	public ArrayList<String> index2word;
	public Map<String, Integer> word2index;
	
	/*
	 * Create a hashmap from an arraylist and
	 * thus form a mapping from words to indices, and
	 * vice-versa.
	 */
	public SavedVocabulary(ArrayList<String> words) {
		this.index2word = words;
		this.create_word2index();
	}
	
	/*
	 * Creates the HashMap for the word2index using
	 * the index2word ArrayList.
	 */
	public void create_word2index() {
		this.word2index = new HashMap<String, Integer>();
		int index = 0;
		for (String word : this.index2word) {
			word2index.put(word, index);
			index += 1;
		}
	}
}
