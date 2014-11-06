package objectlm.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.ejml.simple.SimpleMatrix;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * A tree node class for representing a cluster.
 * 
 * Leaf nodes correspond to original observations, while non-leaf nodes
 * correspond to non-singleton clusters.
 * 
 * The to_tree function converts a matrix returned by the linkage
 * function into an easy-to-use tree representation.
 * 
 * HierarchicalCluster#to_tree : for converting a linkage matrix ``Z`` into a tree object.* 
 */
public class HierarchicalCluster {
	
	private static final int SINGLE_LINK = 0;
	private static final int AVERAGE_LINK = 1;
	public final HierarchicalCluster left;
	public final HierarchicalCluster right;
	public HierarchicalCluster parent;
	public final double dist;
	public final int count;
	public final int id;
	public final String label;
	public HierarchicalCluster[] _leaves;
	
	public HierarchicalCluster(int id,
			HierarchicalCluster left,
			HierarchicalCluster right,
			HierarchicalCluster parent,
			double dist,
			int count,
			String label) throws Exception {
		
		if ((left == null && right != null) ||
				(right == null && left != null)) {
			throw new Exception("Only full or proper binary trees are permitted. This node has one child.");
		}
		this.id = id;
		this.dist = dist;
		this.left = left;
		this.label = label;
		this.parent = parent;
		this.right = right;
		this.count = left == null ? count : left.count + right.count;
		if (left != null) {
			left.parent = this;
			right.parent = this;
		}
	}
	
	public HierarchicalCluster(int id, HierarchicalCluster left, HierarchicalCluster right, double dist) throws Exception {
		this(id, left, right, null, dist, 1, null);
	}
	
	public HierarchicalCluster(int id) throws Exception {
		this(id, null, null, null, 0.0, 1, null);
	}
	
	public HierarchicalCluster(int id, String label) throws Exception {
		this(id, null, null, null, 0.0, 1, label);
	}
	
	public static Map<String, Object> hierarchical_cluster_map(int id) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("count", 1);
		return map;
	}
	
	private static Map<String, Object> hierarchical_cluster_map(int id,
			Map<String, Object> left, Map<String, Object> right, double dist, int count) throws Exception {
		if ((left == null && right != null) ||
				(right == null && left != null)) {
			throw new Exception("Only full or proper binary trees are permitted. This node has one child.");
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("left", left);
		map.put("right", right);
		map.put("dist", dist);
		map.put("count", left == null ? count : (Integer)left.get("count") + (Integer)right.get("count")); 
		return map;
	}
	
	private static Map<String, Object> hierarchical_cluster_map(int id,
			Map<String, Object> left, Map<String, Object> right, double dist) throws Exception {
		return hierarchical_cluster_map(id, left, right, dist, 1);
	}
	
	private static JSONObject hierarchical_cluster_json(int id,
			JSONObject left, JSONObject right, double dist) throws Exception {
		return hierarchical_cluster_json(id, left, right, dist, 1);
	}
	
	public static JSONObject hierarchical_cluster_json(String name) throws JSONException {
		JSONObject map = new JSONObject();
		map.put("count", 1);
		map.put("name", name);
		return map;
	}
	
	public static JSONObject hierarchical_cluster_json(int id) throws JSONException {
		JSONObject map = new JSONObject();
		map.put("count", 1);
		map.put("id", id);
		return map;
	}
	
	private static JSONObject hierarchical_cluster_json(int id,
			JSONObject left, JSONObject right, double dist, int count) throws Exception {
		if ((left == null && right != null) ||
				(right == null && left != null)) {
			throw new Exception("Only full or proper binary trees are permitted. This node has one child.");
		}
		JSONObject map = new JSONObject();
		map.put("id", id);
		JSONArray children = new JSONArray();
		children.put(left);
		children.put(right);
		map.put("children", children);
		map.put("dist", dist);
		map.put("count", left == null ? count : (Integer)left.get("count") + (Integer)right.get("count")); 
		return map;
	}
	
	
	public static HierarchicalCluster to_tree(double[][] Z) throws Exception {
		return to_tree(Z, null);
	}
	
	/**
	 * Converts a hierarchical clustering encoded in the matrix ``Z`` (by
	 * linkage) into an easy-to-use tree object.
	 * 
	 * The reference r to the root ClusterNode object is returned.
	 * 
	 * Each ClusterNode object has a left, right, dist, id, and count
	 * attribute. The left and right attributes point to ClusterNode objects
	 * that were combined to generate the cluster. If both are None then
	 * the ClusterNode object is a leaf node, its count must be 1, and its
	 * distance is meaningless but set to 0.
	 * 
	 * Note: This function is provided for the convenience of the library
	 * user. ClusterNodes are not used as input to any of the functions in this
	 * library.
	 * 
	 * @param Z double[][] The linkage matrix in proper form.
	 * @param names List<String> the names for each cluster
	 * @return {@link HierarchicalCluster}HierarchicalCluster root node
	 * @throws Exception
	 */
	public static HierarchicalCluster to_tree(double[][] Z, List<String> names) throws Exception {
		int n = Z.length + 1, i;
		// Create a list full of None's to store the node objects
		HierarchicalCluster[] d = new HierarchicalCluster[n*2 - 1];
		HierarchicalCluster nd = null;
		// Create the nodes corresponding to the n original objects.
		if (names == null) {
			for (i = 0; i < n; ++i) {
				d[i] = new HierarchicalCluster(i);
			}
		} else {
			for (i = 0; i < n; ++i) {
				d[i] = new HierarchicalCluster(i, names.get(i));
			}
		}
		int fi, fj;
		for (i = 0; i < n -1;++i) {
			fi = (int)Z[i][0];
			fj = (int)Z[i][1];
			if (fi > i + n) {
				throw new Exception("Corrupt matrix Z. Index to derivative cluster is used before it is formed. See row  "+ fi + ", column 0");
			}
			if (fj > i + n) {
				throw new Exception("Corrupt matrix Z. Index to derivative cluster is used before it is formed. See row  "+ fj + ", column 1");
			}
			nd = new HierarchicalCluster(i + n, d[fi], d[fj], Z[i][2]);
			//                           ^ id  ^ left ^ right ^ dist
			if (Z[i][3] != nd.count) {
				throw new Exception("Corrupt matrix Z. The count Z["+i+"][3] is incorrect");
			}
			d[n + i] = nd;
		}
		// nd is now the root
		nd._leaves = d;
		return nd;
	}
	

	private static Map<String, Object> to_map_tree(double[][] Z) throws Exception {
		// TODO Auto-generated method stub
		int n = Z.length + 1, i;
		// Create a list full of None's to store the node objects
		List<Map<String, Object>> d = new ArrayList<Map<String, Object>>(n*2 - 1);
		Map<String, Object> nd = null;
		// Create the nodes corresponding to the n original objects.
		for (i = 0; i < n; ++i) {
			d.add(hierarchical_cluster_map(i));
		}
		int fi, fj;
		for (i = 0; i < n -1;++i) {
			fi = (int)Z[i][0];
			fj = (int)Z[i][1];
			if (fi > i + n) {
				throw new Exception("Corrupt matrix Z. Index to derivative cluster is used before it is formed. See row  "+ fi + ", column 0");
			}
			if (fj > i + n) {
				throw new Exception("Corrupt matrix Z. Index to derivative cluster is used before it is formed. See row  "+ fj + ", column 1");
			}
			nd = hierarchical_cluster_map(i + n, d.get(fi), d.get(fj), Z[i][2]);
			//                        ^ id  ^ left ^ right ^ dist
			if (Z[i][3] != (Integer)nd.get("count")) {
				throw new Exception("Corrupt matrix Z. The count Z["+i+"][3] is incorrect");
			}
			d.add(nd);
		}
		return nd;
	}
	
	public static JSONObject to_json_tree(double[][] Z) throws Exception {
		return to_json_tree(Z, null);
	}
	
	public static JSONObject to_json_tree(double[][] Z, List<String> names) throws Exception {
		// TODO Auto-generated method stub
		int n = Z.length + 1, i;
		// Create a list full of None's to store the node objects
		List<JSONObject> d = new ArrayList<JSONObject>(n*2 - 1);
		JSONObject nd = null;
		// Create the nodes corresponding to the n original objects.
		if (names != null) {
			for (i = 0; i < n; ++i) {
				d.add(hierarchical_cluster_json(names.get(i)));
			}
		} else {
			for (i = 0; i < n; ++i) {
				d.add(hierarchical_cluster_json(i));
			}
		}
		
		int fi, fj;
		for (i = 0; i < n -1;++i) {
			fi = (int)Z[i][0];
			fj = (int)Z[i][1];
			if (fi > i + n) {
				throw new Exception("Corrupt matrix Z. Index to derivative cluster is used before it is formed. See row  "+ fi + ", column 0");
			}
			if (fj > i + n) {
				throw new Exception("Corrupt matrix Z. Index to derivative cluster is used before it is formed. See row  "+ fj + ", column 1");
			}
			nd = hierarchical_cluster_json(i + n, d.get(fi), d.get(fj), Z[i][2]);
			//                        ^ id  ^ left ^ right ^ dist
			if (Z[i][3] != (Integer)nd.get("count")) {
				throw new Exception("Corrupt matrix Z. The count Z["+i+"][3] is incorrect");
			}
			d.add(nd);
		}
		return nd;
	}
	
	
	/**
	 *  Calculate the size of each cluster. The result is the fourth column of
	 *  the linkage matrix.
	 *  
	 *  @param Z : double[][]
	 *       The linkage matrix. The fourth column can be empty.
	 *  @param cs : double[][]
	 *       The array to store the sizes.
	 *  @param cs_sublices : int
	 *   	 The subarray index in Z where the sizes should be stored.
	 *  @param n : int
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
	
	/**
	 *    Generate a linkage matrix from its pointer representation.
	 *
	 *    @param Z : double[][]
	 *        An array to store the linkage matrix.
	 *    @param Lambda : double[]
	 *        The :math:`\\Lambda` array of the pointer representation.
	 *    @param Pi : int[]
	 *        The :math:`\\Pi` array of the pointer representation.
	 *    @param n : int
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
	/**
	 * The SLINK algorithm. Single linkage in O(n^2) time complexity converted
	 * from scipy.cluster._hierarchy.pyx (cython code);
	 * 
	 * <h2>References:</h2>
	 * 
	 * R. Sibson, "SLINK: An optimally efficient algorithm for the single-link
	 * cluster method", The Computer Journal 1973 16: 30-34.
	 * 
	 * @param dists : double[]
	 *     A condensed matrix stores the pairwise distances of the observations.
	 * @param n : int
	 *     The number of observations.
	 *     
	 * @return Z : double[][]
	 *     A (n - 1) x 4 matrix to store the result (i.e. the linkage matrix).
	 * 
	 */
	public static double[][] slink(double[] pdists, int n) {
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
				M[j] = pdists[VectorUtils.condensed_index(n, i, j)];
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
	
	public static double[][] average_link(double[] pdists, int n) {
		int i, j, k, x = 0, y = 0, i_start, nx, ny, ni, id_x, id_y, id_i;
		double current_min;
		// inter-cluster dists
		double[] D = pdists.clone();
		double[][] Z = new double[n-1][4];
		// map the indices to node ids
		int[] id_map = new int[n];
		
		for (i = 0; i < n; ++i) {
			id_map[i] = i;
		}
		    
		for (k = 0; k < n-1 ; ++k) {
		    // find two closest clusters x, y (x < y)
		    current_min = Double.MAX_VALUE;
		    for (i = 0; i< n-1;++i) {
		    	if (id_map[i] == -1) {
		    		continue;
		    	}
		    	i_start = VectorUtils.condensed_index(n, i, i + 1);
		    	for (j = 0; j <n - i - 1;++j) {
		            if (D[i_start + j] < current_min) {
		                current_min = D[i_start + j];
		                x = i;
		                y = i + j + 1;
		            }
		    	}
		    }
	
		    id_x = id_map[x];
		    id_y = id_map[y];
	
		    // get the original numbers of points in clusters x and y
		    nx = (id_x < n) ? 1 : (int)Z[id_x - n][3];
		    ny = (id_y < n) ? 1 : (int)Z[id_y - n][3];
	
		    // record the new node
		    if (id_x < id_y) {
		        Z[k] = new double[] {id_x, id_y, current_min, nx + ny};
		    } else {
		        Z[k] = new double[] {id_y, id_x, current_min, nx + ny};
		    }
		    id_map[x] = -1;  // cluster x will be dropped
		    id_map[y] = n + k;  // cluster y will be replaced with the new cluster
	
		    // update the distance matrix
		    for (i = 0; i < n; ++i) {
		        id_i = id_map[i];
		        if ((id_i == -1) || (id_i == n + k)) {
		            continue;
		        }
	
		        ni = (id_i < n) ? 1 : (int)Z[id_i - n][3];
		        D[VectorUtils.condensed_index(n, i, y)] = average_cluster_dist(
		            D[VectorUtils.condensed_index(n, i, x)],
		            D[VectorUtils.condensed_index(n, i, y)],
		            current_min, nx, ny, ni);
		        if (i < x) {
		            D[VectorUtils.condensed_index(n, i, x)] = Double.MAX_VALUE;
		        }
		    }
		}
		return Z;
	}
	/**
	 * A `linkage_distance_update` function calculates the distance from cluster i
	 * to the new cluster xy after merging cluster x and cluster y
	 * 
	 * @param d_xi : double
	 *     Distance from cluster x to cluster i
	 * @param d_yi : double
	 *     Distance from cluster y to cluster i
	 * @param d_xy : double
	 *     Distance from cluster x to cluster y
	 * @param size_x : int
	 *     Size of cluster x
	 * @param size_y : int
	 *     Size of cluster y
	 * @param size_i : int
	 *     Size of cluster i
	 * 
	 * @return d_xyi : double
	 *     Distance from the new cluster xy to cluster i
	 */
	private static double average_cluster_dist(double d_xi, double d_yi, double d_xy,
            int size_x, int size_y, int size_i) {
		return (size_x * d_xi + size_y * d_yi) / (size_x + size_y);
	}

	/**
	 * 
	 * @param x : SimpleMatrix
	 * @return tree : HierarchicalCluster
	 * @throws Exception
	 */
	public static HierarchicalCluster hierarchy(SimpleMatrix x, int linkage_method) throws Exception {
		double [] pdists = VectorUtils.compute_pdist(x);
		double[][] Z;
		switch (linkage_method) {
			case SINGLE_LINK:
					Z = HierarchicalCluster.slink(pdists, x.numRows());
					break;
			case AVERAGE_LINK:
				Z = HierarchicalCluster.average_link(pdists, x.numRows());
				break;
			default:
				Z = HierarchicalCluster.average_link(pdists, x.numRows());
				break;
		}
		return to_tree(Z);
	}
	
	
	/**
	 * 
	 * @param x : SimpleMatrix
	 * @return tree : Map<String, Object>
	 * @throws Exception
	 */
	public static Map<String, Object> hierarchy_map(SimpleMatrix x, int linkage_method) throws Exception {
		double [] pdists = VectorUtils.compute_pdist(x);
		double[][] Z;
		switch (linkage_method) {
			case SINGLE_LINK:
					Z = HierarchicalCluster.slink(pdists, x.numRows());
					break;
			case AVERAGE_LINK:
				Z = HierarchicalCluster.average_link(pdists, x.numRows());
				break;
			default:
				Z = HierarchicalCluster.average_link(pdists, x.numRows());
				break;
		}
		return to_map_tree(Z);
	}
	
	/**
	 * Loads a saved serialized tree matrix array from a path.
	 * 
	 * @param path where the serialized object is kept.
	 * @return the matrix array.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static double[][] load_tree_matrix(String path) throws IOException, ClassNotFoundException {
		FileInputStream hierarchical_map_file = new FileInputStream(path);
		@SuppressWarnings("resource")
		ObjectInputStream reader = new ObjectInputStream(hierarchical_map_file);
		double[][] Z = (double[][]) reader.readObject();
		return Z;
	}
	
	/**
	 * Saves the tree matrix to a file.
	 *  
	 * @param Z the tree matrix obtained using average_link or slink
	 * @param path where to serialize the array.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void save_tree_matrix(double[][] Z, String path) throws FileNotFoundException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("hierarchy_map.ser"));
		out.writeObject(Z);
		out.close();
	}
	
	/**
	 * 
	 * @param x : SimpleMatrix
	 * @return tree : Map<String, Object>
	 * @throws Exception
	 */
	public static JSONObject hierarchy_json(SimpleMatrix x, int linkage_method) throws Exception {
		double [] pdists = VectorUtils.compute_pdist(x);
		double[][] Z;
		switch (linkage_method) {
			case SINGLE_LINK:
					Z = HierarchicalCluster.slink(pdists, x.numRows());
					break;
			case AVERAGE_LINK:
				Z = HierarchicalCluster.average_link(pdists, x.numRows());
				break;
			default:
				Z = HierarchicalCluster.average_link(pdists, x.numRows());
				break;
		}
		return to_json_tree(Z);
	}
}
