package yelplm;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.ejml.simple.SimpleMatrix;
// This class handles the model parameters
// and gives access to search methods on the
// underlying vectors.
public class YelpLM {
	
	public SimpleMatrix projection_matrix;
	public SimpleMatrix bias_vector;
	public SimpleMatrix model_matrix;
	public SimpleMatrix object_matrix;
	
	public ArrayList<String> index2word;
	public Map<String, Integer> word2index;
	
	public ArrayList<String> index2object;
	public Map<String, Integer> object2index;;
	
	public Integer window;
	public Integer size;
	public Integer object_size;
	
	public ArrayList<Integer> output_classes;
	public Integer output_sigmoid_classes;
	public Integer prediction_size;
	
	public YelpLM(
			SimpleMatrix projection_matrix,
			SimpleMatrix bias_vector,
			SimpleMatrix model_matrix,
			SimpleMatrix object_matrix,
			ArrayList<String> index2word,
			Map<String, Integer> word2index,
			ArrayList<String> index2object,
			Map<String, Integer> object2index,
			int window,
			int size,
			int object_size,
			ArrayList<Integer> output_classes,
			int output_sigmoid_classes) {
		this.projection_matrix = projection_matrix;
		this.bias_vector   = bias_vector;
		this.model_matrix      = model_matrix;
		this.object_matrix     = object_matrix;
		
		this.index2word = index2word;
		this.word2index = word2index;
		
		this.index2object = index2object;
		this.object2index = object2index;
		
		this.window = window;
		this.size = size;
		this.object_size = object_size;
		
		this.output_classes = output_classes;
		this.output_sigmoid_classes = output_sigmoid_classes;
		this.prediction_size = output_sigmoid_classes;
		
		// sum up the size of the prediction of
		// softmax and sigmoid:
		for (int i : output_classes) {
			this.prediction_size += i;
		}
	}
	
	public SimpleMatrix get_word_vector(int index) {
		return this.model_matrix.extractVector(false, index);
	}
	
	public SimpleMatrix get_object_vector(int object_index) {
		return this.object_matrix.extractVector(false, object_index);
	}
	
	public SimpleMatrix observation_vector(ArrayList<Integer> indices, int object_index) {

		SimpleMatrix observation = new SimpleMatrix(window * size + object_size, 1);
		
		int index = 0;
		for (int i : indices) {
			observation.insertIntoThis(index, 0, get_word_vector(i));
			index += size;
		}
		observation.insertIntoThis(index, 0, get_object_vector(object_index));
		
		return observation;
	}
	
	// takes as input the linear projection and then applies the
	// sigmoid and softmax nonlinearities to obtain probabilities
	// for each class.
	private SimpleMatrix normalize_predictions(SimpleMatrix unnormalized) {
		SimpleMatrix prediction = new SimpleMatrix(prediction_size, 1);
		
		int index = 0;
		// for each softmax class extract a prediction
		for (int output_size : output_classes) {
			prediction.insertIntoThis(
					index,
					0,
					VectorUtils.softmax(
							unnormalized.extractMatrix(
									index,
									index + output_size,
									0,
									1)
									)
									);
			index += output_size;
		}
		
		// for the sigmoid classes sigmoid each:
		prediction.insertIntoThis(index, 0, VectorUtils.element_wise_sigmoid(unnormalized.extractMatrix(
									index,
									index + output_sigmoid_classes,
									0,
									1)
									)
									);
		return prediction;
	}
	
	public SimpleMatrix predict_proba(ArrayList<Integer> indices, int object_index) {
		
		SimpleMatrix observation = observation_vector(indices, object_index);
		
		SimpleMatrix unnormalized_predictions = projection_matrix.mult(observation).plus(bias_vector);
		
		return normalize_predictions(unnormalized_predictions);
		
	}
	
}
