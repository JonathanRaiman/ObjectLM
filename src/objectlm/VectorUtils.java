package objectlm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

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
	
	/*
	 * Retrieve the index of the largest value in a matrix.
	 */
	public static Integer argmax(SimpleMatrix input) {
		int greatest = -1;
		double max_observed = Double.NEGATIVE_INFINITY;
		if (input.numRows() > input.numCols()) {
			input = input.transpose();
		}
		for (int i = 0; i < input.numCols(); ++i) {
			if (input.get(0,i) > max_observed) {
				max_observed = input.get(0, i);
				greatest = i;
			}
		}
		return greatest;
	}
	
	public static Integer[] argsort(final float[] a, final boolean ascending) {
		Integer[] indexes = new Integer[a.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        Arrays.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(final Integer i1, final Integer i2) {
                return (ascending ? 1 : -1) * Float.compare(a[i1], a[i2]);
            }
        });
        return indexes;
    }
	
	public static ArrayList<Integer> argsort(final SimpleMatrix a, final boolean ascending) {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
        for (int i = 0; i < a.numRows(); i++) {
            indexes.add(i);
        }
        Collections.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(final Integer i1, final Integer i2) {
                return (ascending ? 1 : -1) * Double.compare(a.get(i1,0), a.get(i2,0));
            }
        });
        return indexes;
    }
	
	/* Sigmoid with Float inputs.
	 * Applies the nonlinearity:
	 *     f(x) = 1 / 1 + exp(-x)
	 */
	public static double sigmoid(Float input) {
		return (1.0 / (1.0 + Math.exp(-input)));
	}
	
	/* Sigmoid with Double inputs.
	 * Applies the nonlinearity:
	 *     f(x) = 1 / 1 + exp(-x)
	 */
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