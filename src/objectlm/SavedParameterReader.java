package objectlm;

import java.io.BufferedReader;
import java.io.File;
//import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SavedParameterReader {
	
	public static InputStream getFileInputStream(String filename) throws IOException {
		InputStream in = new FileInputStream(filename);
		if (filename.endsWith(".gz")) {
			in = new GZIPInputStream(in);
		}
		return in;
	}
	
	public static void check_file_existence(String path, String file) throws Exception {
		File f = new File(path + file);
		if (!f.exists()) {
			throw new Exception("`" + file + "` file with saved parameters not found.");
		}
	}
	
	public static ArrayList<String> convert_string_to_list(String string) {
		ArrayList<String> list = new ArrayList<String>();
		
		// chop off the beginning and ending brackets:
		string = string.substring(
				string.indexOf("[") + 1,
				string.lastIndexOf("]"));
		
		// chop off the extra quotes
		for (String c : string.split(", ")) {
			list.add(
					c.substring(c.indexOf("'")+1,
					c.lastIndexOf("'")
					)
					);
		}
		
		return list;
	}
	
	public static ArrayList<ArrayList<String>> convert_map_to_output_labels(Map<String, String> map, int num_classes) {
		ArrayList<ArrayList<String>> output_labels = new ArrayList<ArrayList<String>>();
		
		for (int i = 0; i < num_classes; ++i) {
			output_labels.add(
					convert_string_to_list(
							map.get("softmax_labels_" + i)
							)
							);
		}
		
		// return the integers:
		return output_labels;
	}
	
	public static ArrayList<Integer> convert_string_to_output_classes(String output_classes_serialized) {
		ArrayList<Integer> output_classes = new ArrayList<Integer>();
		
		// chop off the beginning and ending brackets:
		output_classes_serialized = output_classes_serialized.substring(
				output_classes_serialized.indexOf("[") + 1,
				output_classes_serialized.indexOf("]"));
		
		// split along the commas:
		for (String c : output_classes_serialized.split(", ")) {
			output_classes.add(Integer.parseInt(c));
		}
		
		// return the integers:
		return output_classes;
	}
	
	public static Map<String, String> convert_file_to_map(String filename) throws IOException {
		InputStream in = getFileInputStream(filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		
		Map<String, String> read_map = new HashMap<String, String>();
		
		int position_of_space = -1;
		
		for (String next, line = reader.readLine(); line != null; line = next) {
			// continue moving cursor:
	        next = reader.readLine();
	        
	        position_of_space = line.indexOf(" ");
	        
	        // break up key/value by space character:
	        if (position_of_space != -1) {
				read_map.put(
						line.substring(0, position_of_space),
						line.substring(position_of_space+1)
						);
			} else {
				throw new IOException("Incorrect file format");
			}
	    }
		return read_map;
	}
	
	public static ArrayList<String> load_list_file(String filename) throws IOException {
		InputStream in = getFileInputStream(filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		
		ArrayList<String> list = new ArrayList<String>();
		
		for (String next, line = reader.readLine(); line != null; line = next) {
			// continue moving cursor:
	        next = reader.readLine();
	        list.add(line);
	    }
		return list;
	}
	
	public static SavedVocabulary load_vocabulary(String filename) throws IOException {
		return new SavedVocabulary(load_list_file(filename));
	}
	
	public static void main(String[] args) throws IOException {
		String base_path = "/Users/jonathanraiman/Documents/Master/research/deep_learning/restaurant_rsm/saves/yelplm_window_10_lm_20_objlm_20_4/",
				dict_path = base_path + "__dict__.txt",
				vocab_path = base_path + "__vocab__.gz";
		Map<String, String> map = convert_file_to_map(dict_path);
		
		for (String key : map.keySet()) {
			System.out.println( key + " : " + map.get(key));
		}
		
		SavedVocabulary vocab = load_vocabulary(vocab_path);
		
		System.out.println("first word in vocab is : " + vocab.index2word.get(0));
		System.out.println("      size of vocab is : " + vocab.index2word.size());
		
	}

}
