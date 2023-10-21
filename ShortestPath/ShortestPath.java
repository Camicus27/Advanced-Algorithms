package ShortestPath;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class ShortestPath {

	private static int numberOfRoads;			// [3..10,000]
	private static int numberOfIntersections;	// [3..50,000]
	private static int start;
	private static int end;
	
	private static Node[] roads;
	
	public static void main(String[] args) {
		// Make a scanner for gathering input.
		Scanner input = new Scanner (System.in);

		// Get the road segments and intersections
		numberOfRoads = input.nextInt();
		numberOfIntersections = input.nextInt();
		// Get the start road and end road
		start = input.nextInt();
		end = input.nextInt();
		
		// Build the empty map of roads
		roads = new Node[numberOfRoads];
		for (int i = 0; i < numberOfRoads; i++) {
			Node segment = new Node(i, i);
			roads[i] = segment;
		}
		
		// Add the intersections
		for (int i = 0; i < numberOfIntersections; i++) {
			// Get the connection
			int u = input.nextInt();
			String action = input.next();
			long weight;
			if (action.equals("left")) {
				weight = 50001l * 50001l;  // [2,500,100,001 - 125,005,000,050,000]
			}
			else if (action.equals("straight")) {
				weight = 50001l;  // [50,001 - 2,500,050,000]
			}
			else {
				weight = 1l;  // [1 - 50,000]
			}
			int v = input.nextInt();
			
			// Create the connection
			roads[u].AddConnection(roads[v], weight);
		}
		input.close();
		
		
		FindSSSP(roads[start]);
		
		
		// Generate output
		int choiceCount = 0;
		Stack<String> choices = new Stack<>();
		Stack<Node> stack = new Stack<>();
		stack.add(roads[end]);
		
		while (!stack.isEmpty()) {
			Node n = stack.pop();
			if (n.parent != null) {
				choiceCount++;
				long weight = n.parent.GetEdgeWeight(n);
				if (weight == 1l)
					choices.add("right");
				else if (weight == 50001l)
					choices.add("straight");
				else
					choices.add("left");
				
				stack.push(n.parent);
			}
		}
		
		// Print output
		System.out.println(choiceCount);
		while (!choices.isEmpty())
			System.out.println(choices.pop());
	}

	/**
	 * Find the minimal path from the source to the end
	 * 
	 * @param source - Starting node
	 */
	static void FindSSSP(Node source) {
		// Setup priority queue
		source.distance = 0l;
		PriorityQueue<Node> queue = new PriorityQueue<>();
		queue.add(source);
		
		// Relax every edge in the graph
		while (!queue.isEmpty()) {
			
			Node u = queue.poll();
			
			// Relax all edges u -> v
			for (Node v : u.GetAllChildren()) {
				// If u -> v is tense
				if (u.distance + u.GetEdgeWeight(v) < v.distance) {
					// Decrease Key
					if (queue.contains(v))
						queue.remove(v);
					
					// Relax u -> v
					v.distance = u.distance + u.GetEdgeWeight(v);
					v.parent = u;
					
					queue.add(v);
				}
			}
		}
	}
	
	/**
	 * Representation of a node
	 *	In this case, represents a road segment
	 */
	static class Node implements Comparable<Node> {
		public int id;
		public int creationStamp;
		public long distance;
		public Node parent;
		// Mapping of children with their connection weights
		public HashMap<Node, Long> children;
		
		public Node(int ID, int stamp) {
			creationStamp = stamp;
			id = ID;
			children = new HashMap<>();
			distance = Long.MAX_VALUE; 
			parent = null;
		}
		
		/**
		 * Add a weighted, directed connection.
		 * 
		 * Also filters by priority: If a connection to the child already
		 * exists but the new weight is smaller than the currently stored weight,
		 * the larger weight is replaced by the smaller one.
		 * 
		 * @param child - Node to point to
		 * @param weight - Weight of the edge
		 */
		public void AddConnection(Node child, long weight) {
			if (children.containsKey(child)) {
				if (children.get(child) > weight)
					children.replace(child, weight);
			}
			else
				children.put(child, weight);
		}
		
		/**
		 * @return The set of all child nodes.
		 */
		public Set<Node> GetAllChildren() {
			return children.keySet();
		}
		
		/**
		 * Get the weight of the edge to a given child.
		 * 
		 * @param child - Node to find the edge to
		 * @return Weight of edge from this node to given child
		 */
		public long GetEdgeWeight(Node child) {
			return children.get(child);
		}
		
		@Override
		public int compareTo(Node o) {
			if (distance < o.distance)
				return -1;
			else if (distance > o.distance)
				return 1;
			else {
				if (creationStamp < o.creationStamp)
					return -1;
				else if (creationStamp > o.creationStamp)
					return 1;
				else
					return 0;
			}
		}
		
		@Override
	    public boolean equals(Object o) {
			return compareTo((Node)o) == 0;
		}
	}
}