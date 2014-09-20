Yelp Language Model
===================

(Java implementation of the Python / Cython version in the [rsm](https://git.mers.csail.mit.edu/jraiman/rsm/tree/master#custom-language-model) repo)

A language model for documents and words. This simple language model takes a supervised input dataset with pairs of labels in multinomial or unimodal classes, and then trains the documents and windows of words within those documents to predict those labels. Both documents and words are trained through backprop. Words are shared among all documents, while document vectors live in their own embedding.

Eucledian distances between documents are observed to possess fuzzy search properties over all the labels.