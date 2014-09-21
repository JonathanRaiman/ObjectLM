Yelp Language Model
===================

(Java implementation of the Python / Cython version in the [rsm](https://git.mers.csail.mit.edu/jraiman/rsm/tree/master#custom-language-model) repo)

A language model for documents and words. This simple language model takes a supervised input dataset with pairs of labels in multinomial or unimodal classes, and then trains the documents and windows of words within those documents to predict those labels. Both documents and words are trained through backprop. Words are shared among all documents, while document vectors live in their own embedding.

Eucledian distances between documents are observed to possess fuzzy search properties over all the labels.

### Load Language Model ###

To load a saved copy of the model from Python you can use the following function:

	String base_path = "saves/current_model";
	ObjectLM model = load_saved_python_model(base_path);

And the model will then be parametrized as the Python one. In this directory a `__dict__.txt` file
includes the parameters, a `__vocab__.gz` file holds the vocabulary mapping, and a `parameters.mat`
Matlab matrix file holds the saved parameters.