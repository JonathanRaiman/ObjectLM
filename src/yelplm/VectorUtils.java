package yelplm;

import org.ejml.simple.SimpleMatrix;

public class VectorUtils {
	public static SimpleMatrix concatenate(SimpleMatrix ... vectors) {
		int size = 0;
		for (SimpleMatrix vector : vectors) {
			size += vector.numRows();
		}

		SimpleMatrix result = new SimpleMatrix(size, 1);
		int index = 0;
		for (SimpleMatrix vector : vectors) {
			result.insertIntoThis(index, 0, vector);
			index += vector.numRows();
		}
		return result;
	}
	
	/**
	 * Applies softmax to all of the elements of the matrix.  The return
	 * matrix will have all of its elements sum to 1.  If your matrix is
	 * not already a vector, be sure this is what you actually want.
	 */
	public static SimpleMatrix softmax(SimpleMatrix input) {
		SimpleMatrix output = new SimpleMatrix(input);
		for (int i = 0; i < output.numRows(); ++i) {
			for (int j = 0; j < output.numCols(); ++j) {
				output.set(i, j, Math.exp(output.get(i, j)));
			}
		}
		double sum = output.elementSum();
		// will be safe, since exp should never return 0
		return output.scale(1.0 / sum);
	}
	
	public static double sigmoid(Float input) {
		return (1.0 / (1.0 + Math.exp(-input)));
	}
	
	public static double sigmoid(double input) {
		return 1.0 / (1.0 + Math.exp(-input));
	}
	
	/**
	 * Applies sigmoid to all of the elements of the matrix.*/
	public static SimpleMatrix element_wise_sigmoid(SimpleMatrix input) {
		for (int i = 0; i < input.numRows(); ++i) {
			for (int j = 0; j < input.numCols(); ++j) {
				input.set(i, j, sigmoid(input.get(i, j)));
			}
		}
		return input;
	}
	
}