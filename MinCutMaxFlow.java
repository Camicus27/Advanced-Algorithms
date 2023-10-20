/**
 * @author Camron Wilson - u1230667
 * 4/30/2022
 */

package MinCutMaxFlow;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeSet;

public class MinCutMaxFlow
{
	// Input values
	private static int numberOfVertices;
	private static int sourceID;
	private static int targetID;
	
	// Stored representations of the graph
	private static int[][] flowCapGraph;
	private static int[][] flowGraph;
	private static int[][] resGraph;
	private static Node[] nodes;
	
	private static ArrayList<Integer> pathFlows;
	
	
	public static void main(String[] args)
	{
		// Make a scanner for gathering input.
		Scanner input = new Scanner (System.in);
		
		// Get initial vars from input
		numberOfVertices = input.nextInt();
		sourceID = input.nextInt();
		targetID = input.nextInt();
		
		// Initialize representations
		flowCapGraph = new int[numberOfVertices][numberOfVertices];
		flowGraph = new int[numberOfVertices][numberOfVertices];
		resGraph = new int[numberOfVertices][numberOfVertices];
		nodes = new Node[numberOfVertices];
		pathFlows = new ArrayList<>();
		
		// Fill graphs with input and create all nodes with respective IDs
		for (int i = 0; i < numberOfVertices; i++) // Row
		{ 
			for (int j = 0; j < numberOfVertices; j++) // Column
			{
				int flow = input.nextInt();
				flowCapGraph[i][j] = flow;
				resGraph[i][j] = flow;
			}
			
			nodes[i] = new Node(i);
		}
		input.close();
		
		
		MinimumCutMaximumFlowFinder();
		
		
		// Print all found information
		
		// Flow from each path
		int maxFlow = 0;
		String flows = "";
		for (int flow : pathFlows)
		{
			flows += flow + " ";
			maxFlow += flow;
		}
		System.out.println(flows.stripTrailing());
		
		// Max s-t flow
		System.out.println(maxFlow);
		
		// Total saturated edges
		int saturatedCount = 0;
		for (int i = 0; i < numberOfVertices; i++)
		{ 
			for (int j = 0; j < numberOfVertices; j++)
			{
				if (flowCapGraph[i][j] != 0 && flowCapGraph[i][j] == flowGraph[i][j])
					saturatedCount++;
			}
		}
		System.out.println(saturatedCount);
		
		// Elements of S
		TreeSet<Node> S = BFS(nodes[sourceID]);
		String elementsOfS = "";
		for (Node node : S)
			elementsOfS += node.id + " ";
		System.out.println(elementsOfS.stripTrailing());
		
		// Flow across cut
		System.out.println(maxFlow);
		
		// Total capacity T->S
		int totalCapacity = PossibleFlowFromTtoS(S, nodes[targetID]);
		System.out.println(totalCapacity);
	}
	
	/**
	 * While augmenting paths still exist...
	 *		Find an augmenting path
	 *		Calculate minimum flow (max possible flow)
	 *		Find the augmenting path again with max possible flow as a cap
	 *		Augment the flow of the path to the flowGraph
	 *		Update the residual graph only on edges in the path
	 */
	static void MinimumCutMaximumFlowFinder()
	{
		ArrayList<Node> path = new ArrayList<>();
		while (path != null)
		{
			path = FindAnAugmentingPath();
			if (path == null)
				break;
			int flow = FindMinimumFlow(path);
			
			ApplyFlow(path, flow);
			UpdateResGraph(path, flow);
		}
	}
	
	/**
	 * Modified Dijkstra's algorithm to find shortest paths to target
	 * 
	 * @return Current best augmenting path
	 */
	static ArrayList<Node> FindAnAugmentingPath()
	{
		// Setup
		ResetAllVertices();
		nodes[sourceID].distance = 0;
		
		PriorityQueue<Node> queue = new PriorityQueue<>();
		queue.add(nodes[sourceID]);
		
		// Relax every edge in the graph
		while (!queue.isEmpty())
		{
			Node u = queue.poll();
			
			// Set the distances of u -> v
			for (int i = 0; i < numberOfVertices; i++)
			{
				Node v = nodes[i];
				// If u -> v exists and is tense
				if (resGraph[u.id][i] != 0)
				{
					// New shortest path found
					if (u.distance + 1 < v.distance)
					{
						// Decrease Key
						if (queue.contains(v))
							queue.remove(v);
						
						// Relax the edge
						v.distance = u.distance + 1;
						v.potentialParents.clear();
						v.potentialParents.add(u);
						
						queue.add(v);
					}
					// Equal shortest path found
					else if (u.distance + 1 == v.distance)
					{
						// Decrease Key
						if (queue.contains(v))
							queue.remove(v);
						
						v.potentialParents.add(u);
						
						queue.add(v);
					}
				}
			}
		}
		
		// Find the best path based on tie breaks
		ArrayList<Node> path = new ArrayList<>();
		Stack<Node> stack = new Stack<>();
		stack.add(nodes[targetID]);
		path.add(nodes[targetID]);
		
		while (!stack.isEmpty())
		{
			Node n = stack.pop();
			if (n.potentialParents.isEmpty())
				break;
			
			if (n.potentialParents.size() == 1)
			{
				path.add(n.potentialParents.get(0));
				stack.push(n.potentialParents.get(0));
			}
			
			// First tie break - Multiple shortest paths
			else
			{
				int maxFlow = resGraph[n.potentialParents.get(0).id][n.id];
				int sameFlowCount = 0;
				int index = 0;
				for (int i = 0; i < n.potentialParents.size(); i++)
				{
					// Is flow from v->u  > current maximum
					if (GetFlow(n.potentialParents.get(i).id, n.id) > maxFlow)
					{
						maxFlow = resGraph[n.potentialParents.get(i).id][n.id];
						index = i;
					}
					else if (GetFlow(n.potentialParents.get(i).id, n.id) == maxFlow)
						sameFlowCount++;
				}
				
				// Final tie break - All flows are the same
				if (sameFlowCount == n.potentialParents.size())
				{
					int minID = 0;
					index = 0;
					for (int i = 0; i < n.potentialParents.size(); i++)
					{
						// Is ID < current minimum
						if (n.potentialParents.get(i).id < minID)
						{
							minID = n.potentialParents.get(i).id;
							index = i;
						}
					}
				}
				
				path.add(n.potentialParents.get(index));
				stack.push(n.potentialParents.get(index));
			}
		}
		
		// Verify there is a path from source to target
		if (path.contains(nodes[sourceID]))
			return path;
		else
			return null;
	}
	
	/**
	 * Finds the minimum flow in the residual graph along the given path
	 * 
	 * @param path - Path to find the minimum flow of
	 * @return Minimum flow
	 */
	private static int FindMinimumFlow(ArrayList<Node> path)
	{
		int minFlow = Integer.MAX_VALUE;
		for (int i = 1; i < path.size(); i++)
		{
			if (resGraph[path.get(i).id][path.get(i-1).id] < minFlow)
				minFlow = resGraph[path.get(i).id][path.get(i-1).id];
		}
		pathFlows.add(minFlow);
		
		return minFlow;
	}
	
	/**
	 * Applies the given flow to the graph along the given path
	 * 
	 * @param path - Where to apply the flow
	 * @param flow - Flow to apply
	 */
	private static void ApplyFlow(ArrayList<Node> path, int flow)
	{
		for (int i = path.size()-1; i > 0; i--)
		{
			flowGraph[path.get(i).id][path.get(i-1).id] += flow;
		}
	}
	
	/**
	 * Applies the given flow to the residual graph along the given path
	 * 
	 * @param path - Where to apply the flow
	 * @param flow - Flow to apply
	 */
	private static void UpdateResGraph(ArrayList<Node> path, int flow)
	{
		for (int i = path.size()-1; i > 0; i--)
		{
			resGraph[path.get(i).id][path.get(i-1).id] -= flow;
			resGraph[path.get(i-1).id][path.get(i).id] += flow;
		}
	}
	
	/**
	 * @return Flow from u -> u
	 */
	private static int GetFlow(int u, int v)
	{
		return resGraph[u][v];
	}
	
	/**
	 * Reset all the vertices to a default state
	 */
	private static void ResetAllVertices()
	{
		for (Node node : nodes)
		{
			node.distance = Integer.MAX_VALUE;
			node.potentialParents.clear();
			node.isMarked = false;
		}
	}
	
	/**
	 * Performs a breadth-first search to find the reachability of a node
	 * 
	 * @param source - Source to search from
	 * @return PriorityQueue of reachable nodes
	 */
	private static TreeSet<Node> BFS(Node source)
	{
		ResetAllVertices();
		TreeSet<Node> set = new TreeSet<>();
		Queue<Node> queue = new LinkedList<>();
		source.isMarked = true;
		queue.add(source);
		set.add(source);
		
		while (!queue.isEmpty())
		{
			Node u = queue.poll();
			
			for (int i = 0; i < numberOfVertices; i++)
			{
				// If edge exists and hasn't been visited
				if (resGraph[u.id][i] != 0 && !nodes[i].isMarked)
				{
					nodes[i].isMarked = true;
					set.add(nodes[i]);
					queue.add(nodes[i]);
				}
			}
		}
		
		return set;
	}
	
	/**
	 * Finds the maximum flow capacity from set T to set S
	 * 
	 * @param S - Set of source flow
	 * @param target - Target node
	 * @return Max flow from T to S
	 */
	private static int PossibleFlowFromTtoS(TreeSet<Node> S, Node target)
	{
		int flow = 0;
		
		for (int i = 0; i < numberOfVertices; i++) // Row
		{
			// If node is in set T
			if (!S.contains(nodes[i]))
			{
				for (int j = 0; j < numberOfVertices; j++) // Column
				{
					// If edge exists and leads from T to S
					if (resGraph[nodes[i].id][j] != 0 && S.contains(nodes[j]))
						flow += flowCapGraph[nodes[i].id][j];
				}
			}
		}
		
		return flow;
	}
	
	/**
	 * Representation of a node
	 */
	static class Node implements Comparable<Node>
	{
		public int id;
		public int distance;
		public ArrayList<Node> potentialParents;
		
		public boolean isMarked;
		
		public Node(int ID)
		{
			id = ID;
			distance = Integer.MAX_VALUE;
			potentialParents = new ArrayList<>();
			isMarked = false;
		}
		
		@Override
		public int compareTo(Node o)
		{
			if (distance < o.distance)
				return -1;
			else if (distance > o.distance)
				return 1;
			else {
				if (id < o.id)
					return -1;
				else if (id > o.id)
					return 1;
				else
					return 0;
			}
		}
		
		@Override
	    public boolean equals(Object o)
		{
			return compareTo((Node)o) == 0;
		}
	}

}
