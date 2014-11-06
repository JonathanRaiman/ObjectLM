package objectlm.utils;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import org.ejml.data.MatrixIterator;
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
	 * Here we follow the steps shown in [1, 2] to get the inverse covariance. 
	 * 
	 * [1] ``The discovery of structural forms", Kemp C., Tenenbaum J. B., (2008)
	 * [2] ``Semi-Supervised Learning Using Gaussian Fields and Harmonic Functions",
	 *     Zhu, Ghahramani and Lafferty (2003)
	 * 
	 * @param pdists: succinct adjacency matrix.
	 * @param n: number of points compared in adjacency matrix.
	 * @param sigma : the sigma value used
	 * @return SimpleMatrix : the inverse covariance matrix for all points.
	 */
	public static SimpleMatrix inv_covariance(double[] pdists, int n, double sigma) {
		SimpleMatrix D = new SimpleMatrix(n, n);
		double row_sum = 0.0,
				pdist = 0.0,
				sigma_squared_inv = 1.0 / (sigma * sigma);
		for (int i = 0; i < n;++i) {
			row_sum = 0.0;
			for (int j = 0; j < n; ++j) {
				pdist = pdists[condensed_index(n, i, j)];
				D.set(i, j, - pdist);
				row_sum += pdist;
			}
			D.set(i, i, D.get(i,i) + row_sum + sigma_squared_inv);
		}
		return D;
	}
	
	public static int[] argsort(final float[] a) {
        return argsort(a, true);
    }

    public static int[] argsort(final float[] a, final boolean ascending) {
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
        return asArray(indexes);
    }
    

	public static int[] argsort(final double[] a) {
        return argsort(a, true);
    }
    
    public static int[] argsort(final double[] a, final boolean ascending) {
        Integer[] indexes = new Integer[a.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        Arrays.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(final Integer i1, final Integer i2) {
                return (ascending ? 1 : -1) * Double.compare(a[i1], a[i2]);
            }
        });
        return asArray(indexes);
    }

    public static <T extends Number> int[] asArray(final T... a) {
        int[] b = new int[a.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = a[i].intValue();
        }
        return b;
    }
	
	
	public static double[] compute_pdist(SimpleMatrix x) {
		
		// final size is the sum of first n-1 first numbers:
		long n = x.numRows();
		long end_size = ((n-1) * n) / 2;
		double[] pdists = new double[(int)end_size];
		
		int k = 0;
		for (int i = 0; i < n; ++i) {
			for (int j = i + 1; j < n; ++j) {
				// Cosine distance:
				pdists[k] = 1.0 - Math.abs(x.extractVector(true, i).dot(x.extractVector(true, j)));
				k += 1;
			}
		}
		
		return pdists;
	}
	
	public static SimpleMatrix normalize(SimpleMatrix x) {
		return x.divide(x.normF());
	}
	
	public static SimpleMatrix sum(SimpleMatrix x, Integer axis) throws IllegalArgumentException{
		if (axis == null) {
			SimpleMatrix summed = new SimpleMatrix(1, 1);
			summed.set(0, 0, x.elementSum());
			return summed;
		} else {
			if (axis < 0) {
				return sum(x, 2 + axis);
			}
			int major_dimension;
			boolean flipped;
			SimpleMatrix summed;
				
			if (axis == 0) {
				major_dimension = x.numCols();
				flipped = false;
				summed = new SimpleMatrix(1, major_dimension);
			} else if (axis == 1) {
				major_dimension = x.numRows();
				flipped = true;
				summed = new SimpleMatrix(major_dimension, 1);
			} else {
				throw new IllegalArgumentException("Can only sum along axis 0, 1 or -1, -2");
			}
			
			// get the appropriate axis, and then do a sum along that axis
			for (int i = 0; i< major_dimension; i++) {
				summed.set(i, x.extractVector(flipped, i).elementSum());
			}
			return summed;
		}
	}
	
	
	/**
	 * Returns a vector with random Gaussian values, mean 0, std 1
	 */
	public static SimpleMatrix randomGaussian(int numRows, int numCols, Random rand, double mean, double std) {
		SimpleMatrix result = new SimpleMatrix(numRows, numCols);
		for (int i = 0; i < numRows; ++i) {
			for (int j = 0; j < numCols; ++j) {
				result.set(i, j, mean + std * rand.nextGaussian());
			}
		}
		return result;
	}
	
	/**
	 * Returns a vector with random Gaussian values, mean 0, std 1
	 */
	public static SimpleMatrix randomGaussian(int numRows, int numCols, Random rand) {
		return randomGaussian(numRows, numCols, rand, 0., 1.);
	}
	
	/**
	 * Returns a vector with random Gaussian values, mean 0, std 1
	 */
	public static SimpleMatrix randomGaussian(int numRows, int numCols) {
		Random rand = new Random();
		return randomGaussian(numRows, numCols, rand, 0., 1.);
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
	
	public static List<Integer> argsort(final SimpleMatrix a, final boolean ascending) {
		List<Integer> indexes = new ArrayList<Integer>();
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
	
	public static SimpleMatrix sqrt(SimpleMatrix x) {
		return sqrt(x, false);
	}
	
	public static SimpleMatrix sqrt(SimpleMatrix x, boolean inplace) {
		if (!inplace) {
			x = x.copy();
		}
		MatrixIterator iter = x.iterator(true, 0, 0, x.numRows()-1, x.numCols()-1);
		Double current_val;
		while (iter.hasNext()) {
			current_val = iter.next();
			iter.set(Math.sqrt(current_val));
		}
		return x;
	}
	public static SimpleMatrix element_divide(SimpleMatrix x, SimpleMatrix divisor) throws Exception {
		return element_divide(x, divisor, false);
	}
	
	public static SimpleMatrix element_divide(SimpleMatrix x, SimpleMatrix divisor, boolean inplace) throws IllegalArgumentException {
		if (!inplace) {
			x = x.copy();
		}
		if (!divisor.isVector()) {
			throw new IllegalArgumentException("divisor must be a vector");
		}
		if (divisor.numCols() > divisor.numRows()) {
			for (int i = 0 ; i < x.numCols();i++) {
				x.insertIntoThis(0, i, x.extractVector(false, i).divide(divisor.get(i)));
			}
		} else {
			for (int i = 0 ; i < x.numRows();i++) {
				x.insertIntoThis(i, 0, x.extractVector(true, i).divide(divisor.get(i)));
			}
		}
		
		return x;
	}
	
	/**
	 * Calculate the condensed index of element (i, j) in an n x n condensed
	 * matrix.
	 */
	public static int condensed_index (int n, int i, int j) {
		if (i < j) {
			return n * i - (i * (i + 1) / 2) + (j - i - 1);
		} else if (i > j) {
			return n * j - (j * (j + 1) / 2) + (i - j - 1);
		}
		return 0;
	}
	
	public static void main (String[] args) throws Exception {
		
		SimpleMatrix mat1 = new SimpleMatrix(new double[][]{{0.1, 0.2, 0.3}, {1.0, 2.0, 3.0}});
		System.out.println(sum(mat1, null));
		System.out.println(sum(mat1, 0));
		System.out.println(sum(mat1, 1));
		System.out.println(sum(mat1, -1));
		System.out.println(sum(mat1, -2));
		
		SimpleMatrix mat_divisor = new SimpleMatrix(new double[][]{{1.0, 2.0, 3.0}});
		
		System.out.println(element_divide(mat1, mat_divisor));
		
		System.out.println(element_divide(mat1, mat_divisor.extractMatrix(0, 1, 0, 2).transpose()));

		System.out.print(sqrt(mat1));
	}
	
}