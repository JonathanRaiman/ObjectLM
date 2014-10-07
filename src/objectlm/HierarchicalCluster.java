package objectlm;

public class HierarchicalCluster {

	/*
	 * Calculate the condensed index of element (i, j) in an n x n condensed
	 * matrix.
	 */
	private static int condensed_index (int n, int i, int j) {
		if (i < j) {
			return n * i - (i * (i + 1) / 2) + (j - i - 1);
		} else if (i > j) {
			return n * j - (j * (j + 1) / 2) + (i - j - 1);
		}
		return 0;
	}
	
	/*
	 *  Calculate the size of each cluster. The result is the fourth column of
	 *  the linkage matrix.
	 *  
	 *   Parameters
	 *   ----------
	 *   Z : double[][]
	 *       The linkage matrix. The fourth column can be empty.
	 *   cs : double[][]
	 *       The array to store the sizes.
	 *   cs_sublics: int
	 *   	 The subarray index in Z where the sizes should be stored.
	 *   n : int
	 *       The number of observations.
	 */
	private static void calculate_cluster_sizes(double[][] Z, double[][] cs, int cs_subslice, int n) {
		    int i, child_l, child_r;
		    for (i = 0; i < n - 1; ++i) {
		        child_l = (int)Z[i][0];
		        child_r = (int)Z[i][1];
		        
		        if (child_l >= n) {
		        	cs[i][cs_subslice] += cs[child_l - n][cs_subslice];
		        } else {
		        	cs[i][cs_subslice] += 1;
		        }
		        
		        if (child_r >= n) {
		            cs[i][cs_subslice] += cs[child_r - n][cs_subslice];
		        } else {
		            cs[i][cs_subslice] += 1;
		        }
		    }
		}
	
	/*
	 *    Generate a linkage matrix from its pointer representation.
	 *
	 *    Parameters
	 *    ----------
	 *    Z : ndarray
	 *        An array to store the linkage matrix.
	 *    Lambda : ndarray
	 *        The :math:`\\Lambda` array of the pointer representation.
	 *    Pi : ndarray
	 *        The :math:`\\Pi` array of the pointer representation.
	 *    n : int
	 *        The number of observations.
	 */
	private static void from_pointer_representation(double[][] Z, double[] Lambda, int[] Pi, int n) {
		int i, current_leaf, pi;
		int[] sorted_idx = VectorUtils.argsort(Lambda);
	    int[] node_ids = new int[n];
	    
	    for (i = 0 ; i < n; ++i) {
	    	node_ids[i] = i;
	    }
	    
	    for (i = 0; i < n-1; ++i) {
	    	current_leaf = sorted_idx[i];
	    	pi = Pi[current_leaf];
	    	if (node_ids[current_leaf] < node_ids[pi]) {
	    		Z[i][0] = node_ids[current_leaf];
	    		Z[i][1] = node_ids[pi];
	    	} else {
	    		Z[i][0] = node_ids[pi];
	    		Z[i][1] = node_ids[current_leaf];
	    	}
	    	Z[i][2] = Lambda[current_leaf];
	    	node_ids[pi] = n + i;
	    }
	    for (i = 0; i < Z.length; ++i) {
	    	Z[i][3] = 0;
	    }
	    calculate_cluster_sizes(Z, Z, 3, n);
	}
	/*
	 * The SLINK algorithm. Single linkage in O(n^2) time complexity converted
	 * from scipy.cluster._hierarchy.pyx (cython code);
	 * 
	 * Parameters
	 * ----------
	 * dists : double[]
	 *     A condensed matrix stores the pairwise distances of the observations.
	 * n : int
	 *     The number of observations.
	 * 
	 * References
	 * ----------
	 * R. Sibson, "SLINK: An optimally efficient algorithm for the single-link
	 * cluster method", The Computer Journal 1973 16: 30-34.
	 * 
	 * Outputs
	 * -------
	 * 
	 * Z : double[][]
	 *     A (n - 1) x 4 matrix to store the result (i.e. the linkage matrix).
	 * 
	 */
	public static double[][] cluster(double[] pdists, int n) {
		double[] M = new double[n];
		double[] Lambda = new double[n];
		double[][] Z = new double[n-1][4];
		int[] Pi = new int[n];

		Pi[0] = 0;
		Lambda[0] = Double.MAX_VALUE;
		for (int i = 1; i< n; ++i) {
			Pi[i] = i;
			Lambda[i] = Double.MAX_VALUE;

			for (int j = 0; j < i; ++j) {
				M[j] = pdists[condensed_index(n, i, j)];
			}
			for (int j = 0; j < i; ++j) {
				if (Lambda[j] >= M[j]) {
					M[Pi[j]] = Math.min(M[Pi[j]], Lambda[j]);
					Lambda[j] = M[j];
					Pi[j] = i;
				} else {
					M[Pi[j]] = Math.min(M[Pi[j]], M[j]);
				}
			}

			for (int j = 0; j < i; ++j) {
				if (Lambda[j] >= Lambda[Pi[j]]) {
					Pi[j] = i;
				}
			}
		}
		from_pointer_representation(Z, Lambda, Pi, n);
		return Z;
	}
}
