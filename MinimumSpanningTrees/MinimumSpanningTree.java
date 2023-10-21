package MinimumSpanningTrees;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.TreeSet;

public class MinimumSpanningTree {
	
	private static int seed;
	private static int vertexCount;
	private static int minWeight;
	private static int maxWeight;
	private static int connectivity;
	private static String algorithmName;
	public static int[][] weightedMatrix;
	private static ArrayList<Vertex> allVertices;
	private static ArrayList<Vertex> boruvkaVertices;
	private static ArrayList<Edge> edgeResults;
	private static int bCount;
	private static int totalWeight;
	private static ArrayList<Edge> allEdges;

	public static void main(String[] args) {
		
		// Make a scanner for gathering input.
		Scanner input = new Scanner (System.in);
		
		// Get all inputs
		seed = input.nextInt();
		vertexCount = input.nextInt();
		minWeight = input.nextInt();
		maxWeight = input.nextInt();
		connectivity = input.nextInt();
		algorithmName = input.next();
		
		// Generate the connected, undirected, and weighted graph
		weightedMatrix = generateWeights(seed, vertexCount, minWeight, maxWeight, connectivity);
		
		// Get all vertices
		allVertices = new ArrayList<>();
		for (int i = 0; i < vertexCount; i++) {
			Vertex v = new Vertex(i);
			allVertices.add(v);
		}
		
		// Create a duplicate vertices list with no connections
		boruvkaVertices = new ArrayList<>();
		for (Vertex vertex : allVertices) {
			Vertex v = new Vertex(vertex.ID);
			boruvkaVertices.add(v);
		}
		
		// Get all edges
		allEdges = new ArrayList<>();
		for (int i = 0; i < weightedMatrix.length; i++) {
			for (int j = 0; j < weightedMatrix[0].length; j++) {
				if (weightedMatrix[i][j] > 0) {
					Edge e = new Edge(i, j);
					allVertices.get(i).AddConnection(allVertices.get(j));
					if (!allEdges.contains(e))
						allEdges.add(e);
				}
			}
		}
		edgeResults = new ArrayList<>();
		
		// Compute any one of the three spanning trees
		if (algorithmName.equals("Jarnik"))
			Jarnik(allVertices.get(input.nextInt()));
		else if (algorithmName.equals("Kruskal"))
			Kruskal();
		else if (algorithmName.equals("Boruvka"))
			Boruvka();
		
		totalWeight = 0;
        for (Edge e : edgeResults) {
        	totalWeight += weightedMatrix[e.i][e.j];
        }
        System.out.println(totalWeight);
		System.out.println(edgeResults.size());
		for (Edge e : edgeResults) {
			System.out.println(e.toString());
		}
		
		input.close();
	}
	
	
	private static ArrayList<Edge> Jarnik(Vertex startVertex) {
		PriorityQueue<Edge> queue = new PriorityQueue<>();
		startVertex.isMarked = true;
		for (Edge edge : startVertex.edgeConnections) {
			queue.add(edge);
		}
		while (DoesUnMarkExist()) {
			Edge e = queue.poll();
			Vertex v = allVertices.get(e.i);
			Vertex w = allVertices.get(e.j);
			if (!v.isMarked) {
				v.isMarked = true;
				edgeResults.add(e);
				for (Edge edge : v.edgeConnections) {
					queue.add(edge);
				}
			}
			else if (!w.isMarked) {
				w.isMarked = true;
				edgeResults.add(e);
				for (Edge edge : w.edgeConnections) {
					queue.add(edge);
				}
			}
		}
		
		return edgeResults;
	}
	
	
	private static ArrayList<Edge> Kruskal() {
		SortEdges(allEdges.size());
		
		ArrayList<TreeSet<Integer>> sets = new ArrayList<>();
		for (Vertex v : allVertices) {
			TreeSet<Integer> set = new TreeSet<>();
			set.add(v.ID);
			sets.add(set);
		}
		
		for (int i = 0; i < allEdges.size(); i++) {
			Edge e = allEdges.get(i);
			if (sets.get(e.i) != sets.get(e.j)) {
				sets.get(e.i).addAll(sets.get(e.j));
				edgeResults.add(e);
			}
		}
		
		return edgeResults;
	}
	
	
	private static ArrayList<Edge> Boruvka() {
		CountAndLabel();
		
		while (bCount > 0) {
			AddAllSafeEdges();
			CountAndLabel();
		}
		
		return edgeResults;
	}
	
	
	private static void AddAllSafeEdges() {
		ArrayList<Edge> safeEdges = new ArrayList<>();
		for (int i = 0; i < bCount + 1; i++) {
			safeEdges.add(null);
		}
		
		for (Edge e : allEdges) {
			int compU = boruvkaVertices.get(e.i).component;
			int compV = boruvkaVertices.get(e.j).component;
			
			if (compU != compV) {
				if (safeEdges.get(compU) == null || weightedMatrix[e.i][e.j] < weightedMatrix[safeEdges.get(compU).i][safeEdges.get(compU).j]) {
					// If vertex V contains a safe edge that is about to be replaced by a better edge, remove the edge
					if (safeEdges.get(compU) != null && boruvkaVertices.get(safeEdges.get(compU).i).neighbors.contains(boruvkaVertices.get(safeEdges.get(compU).j))) {
						boruvkaVertices.get(safeEdges.get(compU).i).neighbors.remove(boruvkaVertices.get(safeEdges.get(compU).j));
						boruvkaVertices.get(safeEdges.get(compU).j).neighbors.remove(boruvkaVertices.get(safeEdges.get(compU).i));
					}
					
					// Add the new safe edge
					safeEdges.set(compU, e);
					boruvkaVertices.get(safeEdges.get(compU).i).AddConnection(boruvkaVertices.get(safeEdges.get(compU).j));
				}
				if (safeEdges.get(compV) == null || weightedMatrix[e.i][e.j] < weightedMatrix[safeEdges.get(compV).i][safeEdges.get(compV).j]) {
					// If vertex V contains a safe edge that is about to be replaced by a better edge, remove the edge
					if (safeEdges.get(compV) != null && boruvkaVertices.get(safeEdges.get(compV).i).neighbors.contains(boruvkaVertices.get(safeEdges.get(compV).j))) {
						boruvkaVertices.get(safeEdges.get(compV).j).neighbors.remove(boruvkaVertices.get(safeEdges.get(compV).i));
						boruvkaVertices.get(safeEdges.get(compV).i).neighbors.remove(boruvkaVertices.get(safeEdges.get(compV).j));
					}
					
					// Add the new safe edge
					safeEdges.set(compV, e);
					boruvkaVertices.get(safeEdges.get(compV).j).AddConnection(boruvkaVertices.get(safeEdges.get(compV).i));
				}
			}
		}
		
		for (int i = 0; i < bCount; i++) {
			if (safeEdges.get(i) != null && !edgeResults.contains(safeEdges.get(i))) {
				edgeResults.add(safeEdges.get(i));
			}
		}
	}
	
	
	private static void CountAndLabel() {
		bCount = -1;
		UnmarkAll();
		ArrayList<Vertex> bag = new ArrayList<>();
		for (int i = 0; i < vertexCount; i++) {
			if (!boruvkaVertices.get(i).isMarked) {
				bCount++;
				
				bag.add(boruvkaVertices.get(i));
				while (!bag.isEmpty()) {
					Vertex v = bag.remove(0);
					if (!v.isMarked) {
						v.isMarked = true;
						v.component = bCount;
						for (Vertex w : v.neighbors)
							bag.add(w);
					}
				}
			}
		}
	}
	
	
	private static boolean DoesUnMarkExist() {
		for (Vertex v : allVertices) {
			if (!v.isMarked)
				return true;
		}
		return false;
	}
	
	
	private static void UnmarkAll() {
		for (Vertex v : boruvkaVertices)
			v.isMarked = false;
	}
	
	
	private static void SortEdges(int n) {
		if (n == 1)
			return;
		
		for (int i = 0; i < allEdges.size() - 1; i++) {
			if (allEdges.get(i).compareTo(allEdges.get(i + 1)) == 1) {
				Edge temp = allEdges.get(i);
				allEdges.set(i, allEdges.get(i + 1));
				allEdges.set(i + 1, temp);
			}
		}
		
		SortEdges(n - 1);
	}
	
	
	/**
	 * Generates a connected, undirected, weighted graph.  Note that
	 * the result is a symmetric adjacency matrix where each value
	 * represents an edge weight.  (An edge weight of 0 represents
	 * a non-edge.)
	 * 
	 * Note that the arrays are zero-based, so vertices are numbered
	 * [0..vertexCount).  
	 * 
	 * The connectivity parameter specifies how many times a random
	 * spanning tree should be added to the graph.  Note that a 
	 * value greater than 1 will probably result in a cycle, but it
	 * is not guaranteed (especially for tiny graphs)
	 * 
	 * For language independence, the random number generation is
	 * done using a linear feedback shift register with a cycle
	 * length of 2^31-1.  (Bits 27 and 30 are xor'd and fed back.)
	 * (Note:  2^31-1 is prime which is useful when generating
	 * pairs, triples, or other multi-valued sequences.  The
	 * pattern won't repeat until after 2^31-1 pairs, triples, etc.
	 * are generated.)
	 * 
	 * Finally, the runtime of this generation is O(v) in connectivity,
	 * or k*v*connectivity.
	 * 
	 * @param seed  any positive int
	 * @param vertexCount any int greater than 1  
	 * @param minWeight   any positive int
	 * @param maxWeight   any int greater than minWeight
	 * @param connectivity the overall connectedness of the graph, min 1
	 * @return the weighted adjacency matrix for the graph
	 */
	public static int[][] generateWeights (int seed, int vertexCount, int minWeight, int maxWeight, int connectivity)  // Non-zero seed, cap vertices at 100, weights at 10000 
	{
		int[][] weights = new int[vertexCount][vertexCount];
		
		for (int pass = 0; pass < connectivity; pass++)
		{
			List<Integer> connected = new ArrayList<Integer>();
			List<Integer> unused    = new ArrayList<Integer>();
			
			connected.add(0);
			for (int vertex = 1; vertex < vertexCount; vertex++)
				unused.add(vertex);
			
			while (unused.size() > 0)
			{
				seed = (((seed ^ (seed >> 3)) >> 12) & 0xffff) | ((seed & 0x7fff) << 16);
				int weight = seed % (maxWeight-minWeight+1) + minWeight;
				
				seed = (((seed ^ (seed >> 3)) >> 12) & 0xffff) | ((seed & 0x7fff) << 16);
				Integer fromVertex = connected.get(seed % connected.size());
				
				seed = (((seed ^ (seed >> 3)) >> 12) & 0xffff) | ((seed & 0x7fff) << 16);
				Integer toVertex   = unused.get(seed % unused.size());
				
				weights[fromVertex][toVertex] = weight;
				weights[toVertex][fromVertex] = weight;  // Undirected
				
				connected.add(toVertex);
				unused.remove(toVertex);  // Note -- overloaded, remove element Integer, not position int
			}			
		}
		
		return weights;
	}
}

class Edge implements Comparable<Edge> {
	public int i, j;
	
	public Edge(int i, int j) {
		this.i = i;
		this.j = j;
	}

	@Override
	public int compareTo(Edge o) {
		if (MinimumSpanningTree.weightedMatrix[i][j] < MinimumSpanningTree.weightedMatrix[o.i][o.j])
			return -1;
		if (MinimumSpanningTree.weightedMatrix[i][j] > MinimumSpanningTree.weightedMatrix[o.i][o.j])
			return 1;
		if (min(i, j) < min(o.i, o.j))
			return -1;
		if (min(i, j) > min(o.i, o.j))
			return 1;
		if (max(i, j) < max(o.i, o.j))
			return -1;
		if (max(i, j) > max(o.i, o.j))
			return 1;
		return 0;
	}
	
	@Override
    public boolean equals(Object o) {
		return compareTo((Edge)o) == 0;
	}
	
	@Override
	public String toString() {
		if (i < j)
			return i + " " + j;
		else
			return j + " " + i;
	}
	
	private int min(int i, int j) {
		if (i < j)
			return i;
		return j;
	}
	
	private int max(int i, int j) {
		if (i < j)
			return j;
		return i;
	}
}


class Vertex {
	public int ID;
	
	public int component;
	
	public ArrayList<Edge> edgeConnections;
	public HashSet<Vertex> neighbors;
	
	public boolean isMarked;
	
	// Default construction
	public Vertex(int id) {
		ID = id;
		edgeConnections = new ArrayList<>();
		neighbors = new HashSet<>();
		
		isMarked = false;
	}
	
	/**
	 * Add an undirected connection
	 * 
	 * @param neighbor - Name of vertex to connect to
	 */
	public void AddConnection(Vertex neighbor) {
		Edge e = new Edge(ID, neighbor.ID);
		
		if (!edgeConnections.contains(e))
			edgeConnections.add(e);
		if (!neighbor.edgeConnections.contains(e))
			neighbor.edgeConnections.add(e);
		
		neighbors.add(neighbor);
		neighbor.neighbors.add(this);
	}
}
