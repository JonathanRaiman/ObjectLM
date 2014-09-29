Yelp Language Model
===================

This is a Java implementation of the Python / Cython version found [here](https://github.com/JonathanRaiman/pythonobjectlm).

A language model for documents and words. This simple language model takes a supervised input dataset with pairs of labels in multinomial or unimodal classes, and then trains the documents and windows of words within those documents to predict those labels. Both documents and words are trained through backprop. Words are shared among all documents, while document vectors live in their own embedding.

Eucledian distances between documents are observed to possess fuzzy search properties over all the labels.

### Load Language Model ###

To load a saved copy of the model from Python you can use the following function:

	String base_path = "saves/current_model";
	ObjectLM model = load_saved_python_model(base_path);

And the model will then be parametrized as the Python one. In this directory a `__dict__.txt` file includes the parameters, a `__vocab__.gz` file holds the vocabulary mapping, and a `parameters.mat` Matlab matrix file holds the saved parameters, and `__objects__.gz` holds the mapping between some unique string id for objects and their vector.


### Example usage


Load the 4th epoch of the model with window size 10
language model size 20, and object language model size 20:

	String base_path = "/Users/jonathanraiman/Documents/Master/research/deep_learning/restaurant_rsm/saves/objectlm_window_10_lm_20_objlm_20_4/";
	ObjectLM model = load_saved_python_model(base_path);
		
Test the model's true predictive task, getting labels for restaurants + their text:
		
	String search_word = "he";
	String search_id = "The Lemongrass";
	// Some sentences that can be read:
	//String sentence = "On January 24th , Apple Computer will introduce Macintosh . And you 'll see why 1984 won't be like `` 1984 '' .";
	String sentence = "Great wine expensive food and tasty soup with rolls and parsley .";
		
	// the index of the document:
	int object_index = 0;
	
	// Prepare as input:
	ArrayList<Integer> indices = model.convert_to_indices(sentence);
	
	// Project:
	ArrayList<Integer> labels = model.predict(indices.subList(0, model.window), object_index);
	
	// Restitute:
	int num_output_classes = model.output_classes.size();
	System.out.println("Predictions for:");
	System.out.println(" Word window: \"" + sentence + '"');
	System.out.println("Restaurant id: \"" + model.index2object.get(object_index) + '"');
	System.out.println();
	
	System.out.println("Predicted Price & Rating:");
	System.out.println("=========================");
	for (int i = 0; i < num_output_classes; ++i ) {
		System.out.println(model.output_labels.get(i).get(labels.get(i)));
	}
	System.out.println();
	System.out.println("Predicted Categories:");
	System.out.println("=====================");
	
	for (int i = 0; i < model.output_sigmoid_classes; ++i) {
		if (labels.get(num_output_classes + i) == 1) {
			System.out.println(model.output_sigmoid_labels.get(i));
		}
	}

The model is better though at obtaining embeddings for objects, and thus using those for **search**:
		
#### Search for words:


		System.out.println("Neighbors for Word:");
		System.out.println("==========");
		System.out.println('"' + search_word + '"');
		for (Triple<Double, String, Integer> result : model.most_similar_word(search_word, 10)) {
			System.out.println(result.y + " : " + result.x);
		}
		
		
#### Search for Objects:


		System.out.println("Neighbors for Object:");
		System.out.println("==========");
		System.out.println('"' + search_id + '"');
		for (Triple<Double, String, Integer> result : model.most_similar_object(search_id, 10)) {
			System.out.println(result.y + " : " + result.x);
		}


#### Search using labels:

If you backprop the label through the projection matrix you can get a representative in the object space. The eucledian distance to other objects will then be shortest with those with the same label output (by linearity), so looking a expensive, or high rating places can be done as follows:
		
		System.out.println("Objects near the output label 5, or $$$$:");
		System.out.println("==========");
		for (Triple<Double, String, Integer> result : model.search_object_using_output_labels(4, 10)) {
			System.out.println(result.y + " : " + result.x);
		}
		
		// Search for Objects:
		System.out.println("Objects near the output label 10, or 5 stars:");
		System.out.println("==========");
		for (Triple<Double, String, Integer> result : model.search_object_using_output_labels(9, 10)) {
			System.out.println(result.y + " : " + result.x);
		}