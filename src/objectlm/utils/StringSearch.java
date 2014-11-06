package objectlm.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import objectlm.ObjectLM;

import org.apache.commons.lang3.StringUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import java.util.Scanner;

public class StringSearch {
	
	// how far should 2 strings be matched:
	static final int MAXDISTANCE = 4;
	public static final String DB_NAME = "yelp";
	public static final String COLLECTION_NAME = "restaurants";
	
	public static final int JARO_WINKLER = 0;
	public static final int LEVENSHTEIN = 1;
	
	public static int search_score_levenshtein(String query, List<String> keys) {
		int best_score = Integer.MAX_VALUE, new_score;
		for (String key : keys) {
			new_score = StringUtils.getLevenshteinDistance(query, key, MAXDISTANCE);
			if (new_score != -1) {
				best_score = Math.min(best_score, new_score);
			}
		}
		return best_score;
	}
	
	public static double search_score_jaro_winkler(String query, List<String> keys) {
		double score = 1.0;
		for (String key : keys) {
			score = Math.min(score, 1.0 - StringUtils.getJaroWinklerDistance(query, key));
		}
		return score;
	}
	
	/*
	 * Search using LevenshteinDistance a set of list of strings.
	 */
	public static int search_with_text(String query, List<Tuple<List<String>, Integer>> texts ) {
		int score = Integer.MAX_VALUE,
			new_score,
			best_result = -1;
		for (Tuple<List<String>, Integer> candidate : texts) {
			new_score = search_score_levenshtein(query, candidate.x);
			if (new_score != -1) {
				if (new_score < score) {
					score = new_score;
					best_result = candidate.y;
				}
			}
			// end early if distance is 0.
			if (score == 0) {
				return best_result;
			}
		}
		return best_result;
	}
	
	public static void present_restaurant(DBObject document, Double score, Integer key) {
		String name = (String) document.get("given_name");
		if ( name == null || name.length() == 0) {
			name = (String) document.get("_id");
		}
		@SuppressWarnings("unchecked")
		List<String> categories = (List<String>) document.get("categories");
		Double rating = (Double) document.get("rating");
		String price = (String) document.get("price");
		
		if (score != null) {
			DecimalFormat df = new DecimalFormat("#.##");
			name += " (" + df.format(score) + ")";
		}
		
		if (key != null) {
			name = "("+key+") " + name;
		}
		
		System.out.println(name);
		System.out.println(price);
		System.out.println(StringUtils.repeat('*', rating.intValue()));
		String category_string = "Categories: " + StringUtils.join(categories, ", ");
		System.out.println(category_string);
		System.out.println(StringUtils.repeat('-', category_string.length()));
	}
	
	public static void present_restaurant(DBObject document) {
		present_restaurant(document, null, null);
	}
	
	public static void present_restaurant(DBObject document, Double score) {
		present_restaurant(document, score, null);
	}
	
	public static List<String> convert_restaurant_DBObject_to_list(DBObject document) {
		// get the name:
		String name = get_restaurant_name(document);
		
		ArrayList<String> keywords = new ArrayList<String>();
		
		// categories:
		@SuppressWarnings("unchecked")
		List<String> categories = (List<String>) document.get("categories");
		// lowercase all strings:
		for (String category : categories) {
			keywords.add(category.toLowerCase());
		}
		for (String subword : name.split("[ -]")) {
			keywords.add(subword.toLowerCase());
		}
		keywords.add(name.toLowerCase());
		return keywords;
	}
	
	public static DBObject text_search(String query, int search_method) throws Exception {
		DBObject best_object = null;
		// Connect to Mongo:
		final MongoClient DB_Client = new MongoClient( "localhost" , 27017 );
		final DB DB = DB_Client.getDB( DB_NAME );
		final DBCollection documents = DB.getCollection( COLLECTION_NAME );
		
		if (search_method != JARO_WINKLER && search_method != LEVENSHTEIN) {
			search_method = JARO_WINKLER;
		}
		double new_score = Double.MAX_VALUE, score = Double.MAX_VALUE;
		
		DBCursor cursor = documents.find();
		for (DBObject document : cursor) {
			List<String> keywords = convert_restaurant_DBObject_to_list(document);

			new_score = (search_method == JARO_WINKLER) ?
					search_score_jaro_winkler(query, keywords) :
						((search_method == LEVENSHTEIN) ? 
								search_score_levenshtein(query, keywords) : 
									0.0);

					if (new_score < score) {
						score = new_score;
						best_object = document;
					}

					if (score == 0.0) {
						break;
					}
		}
		return best_object;
	}
	
	public static String get_restaurant_name(DBObject document) {
		String name = (String) document.get("given_name");
		if (name == null || name.length() == 0) {
			name = (String) document.get("_id");
		}
		return name;
	}
	
	public static void main (String[] args) throws Exception {
		final MongoClient DB_Client = new MongoClient( "localhost" , 27017 );
		final DB DB = DB_Client.getDB( DB_NAME );
		final DBCollection documents = DB.getCollection( COLLECTION_NAME );
		
		String query,
			base_instructions = "Type a keyword to search (e.g. \"burger\"), 'q' to exit",
			instructions = "Type a keyword, 'q' to exit",
			semantic_instructions = ", 's' to search for ";
		
		DBObject best_object = null;
		List<Triple<Double, String, Integer>> semantic_results = null;
		Scanner sc = new Scanner(System.in);
		
		
		int search_method = JARO_WINKLER; // LEVENSHTEIN
		int topn = 5;
		
		ObjectLM model = null;
		
		// get data from standard in:
		while (true) {
			if (best_object == null) {
				System.out.println(base_instructions + ":");
			} else if (best_object != null && semantic_results != null) {
				System.out.println(instructions + semantic_instructions + "\"" + get_restaurant_name(best_object) + "\"'s semantic neighbors, \nor 'c' to choose search another restaurant's neighbors :");
			} else {
				System.out.println(instructions + semantic_instructions + "\"" + get_restaurant_name(best_object) + "\"'s semantic neighbors :");
			}
			
			query = sc.nextLine();
			query = StringUtils.trim(query).toLowerCase();
			if (query.equals("q")) {
				break;
			} else if (best_object != null && semantic_results != null && query.equals("c")) {
				System.out.println("Enter the number corresponding to the restaurant you want to focus on, or 'q' to cancel:");
				int i = 1;
				for (Triple<Double, String, Integer> result : semantic_results) {
					DBObject found = documents.findOne(new BasicDBObject("_id", result.y));
					System.out.println("(" + i + ") " + get_restaurant_name(found));
					i += 1;
				}
				
				// enter a valid number to switch focus.
				while (true) {
					query = sc.nextLine();
					query = StringUtils.trim(query).toLowerCase();
					if (query.equals("q")) {
						System.out.println("Keeping \"" + get_restaurant_name(best_object) + "\"");
						break;
					} else {
						try {
							int new_restaurant = Integer.parseInt(query);
							if (new_restaurant > 0 && new_restaurant <= semantic_results.size()) {
								Triple<Double, String, Integer> result = semantic_results.get(new_restaurant - 1);
								best_object = documents.findOne(new BasicDBObject("_id", result.y));
								System.out.println("Switched focus to \"" + get_restaurant_name(best_object) + "\"");
								break;
							} else {
								System.out.println("Not a valid number, must be between 1 and " + semantic_results.size());
							}
						} catch (NumberFormatException e) {
							System.out.println("Not a valid number");
						}
					}
				}
				
			} else if (best_object != null && query.equals("s")) {
				System.out.println("Getting neighbors:");
				
				// if model is not present, load it:
				if (model == null) {
					System.out.println("Loading Object Language Model from disk.");
					model = ObjectLM.load_saved_python_model("/Users/jonathanraiman/Documents/Master/research/deep_learning/restaurant_rsm/saves/objectlm_window_10_lm_20_objlm_20_4/");
					System.out.println("Done");
				}
				
				// find elements with near semantic meaning:
				semantic_results = model.most_similar_object((String)best_object.get("_id"), topn);
				int i = 1;
				for (Triple<Double, String, Integer> result : semantic_results) {
					DBObject found = documents.findOne(new BasicDBObject("_id", result.y));
					present_restaurant(found, result.x, i);
					i += 1;
				}
			} else {
				// reset the semantic_results:
				semantic_results = null;
				best_object = text_search(query, search_method);
				if (best_object != null) {
					present_restaurant(best_object);
				} else {
					System.out.println("No results found :(");
				}
			}
		}
		sc.close();
		System.out.println("Bye");
	}
}
