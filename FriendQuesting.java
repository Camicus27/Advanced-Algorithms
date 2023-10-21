package FriendQuesting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class FriendQuesting {
	
	private static int player1QuestCount;
	private static int player2QuestCount;
	private static int coopQuestCount;
	
	private static HashMap<String, Node> graph;
	private static String[] sortedGraph;

	public static void main(String[] args) {
		
		// Make a scanner for gathering input.
		Scanner input = new Scanner (System.in);
		
		graph = new HashMap<String, Node>();
		
		// Get player 1 quests
		player1QuestCount = input.nextInt();
		for (int i = 0; i < player1QuestCount; i++) {
			
			// Get the next node
			String nodeName = input.next() + "-1";
			Node node;
			if (graph.containsKey(nodeName))
				node = graph.get(nodeName);
			else
				node = new Node(nodeName);
			
			// Get the node it connects to
			String nodeToConnectToName = input.next() + "-1";
			Node nodeToConnectTo;
			if (graph.containsKey(nodeToConnectToName))
				nodeToConnectTo = graph.get(nodeToConnectToName);
			else
				nodeToConnectTo = new Node(nodeToConnectToName);
			
			// Connect them
			node.AddConnection(nodeToConnectTo);
			
			// Add them to the graph
			graph.put(nodeName, node);
			graph.put(nodeToConnectToName, nodeToConnectTo);
		}
		
		// Get player 2 quests
		player2QuestCount = input.nextInt();
		for (int i = 0; i < player2QuestCount; i++) {
			
			// Get the next node
			String nodeName = input.next() + "-2";
			Node node;
			if (graph.containsKey(nodeName))
				node = graph.get(nodeName);
			else
				node = new Node(nodeName);
			
			// Get the node it connects to
			String nodeToConnectToName = input.next() + "-2";
			Node nodeToConnectTo;
			if (graph.containsKey(nodeToConnectToName))
				nodeToConnectTo = graph.get(nodeToConnectToName);
			else
				nodeToConnectTo = new Node(nodeToConnectToName);
			
			// Connect them
			node.AddConnection(nodeToConnectTo);
			
			// Add them to the graph
			graph.put(nodeName, node);
			graph.put(nodeToConnectToName, nodeToConnectTo);
		}
		
		// Get coop quests
		coopQuestCount = input.nextInt();
		for (int i = 0; i < coopQuestCount; i++) {
			
			// Get the next node
			String nodeName = input.next();
			Node coopNode = new Node(nodeName);
			
			// Find the nodes from each quest line in the graph
			Node node1 = graph.remove(nodeName + "-1");
			Node node2 = graph.remove(nodeName + "-2");
			
			// Create a new node with all the connections
			for (Node child : node1.To) {
				coopNode.AddConnection(child);
			}
			for (Node child : node2.To) {
				coopNode.AddConnection(child);
			}
			for (Node parent : node1.From) {
				parent.AddConnection(coopNode);
			}
			for (Node parent : node2.From) {
				parent.AddConnection(coopNode);
			}
			
			// Add the new coop node to the graph
			graph.put(nodeName, coopNode);
		}
		
		input.close();
		
		sortedGraph = new String[graph.size()];
		
		// Verify the graph is acyclic, return "Unsolvable" if it isn't
		if (!IsAcyclic(graph)) {
			System.out.print("Unsolvable");
			return;
		}
		
		// Run the sort
		TopoSort(graph);
		
		// Print the dependencies in order
		for (int i = 0; i < sortedGraph.length; i++) {
			System.out.println(sortedGraph[i]);
		}
	}
	
	/**
	 * Sorts the given graph by dependencies and fills the sortedGraph array
	 * 
	 * @param G - Graph to sort
	 */
	private static void TopoSort(HashMap<String, Node> G) {
		SetAllNodesToNew();
		int clock = G.size() - 1;
		for (Node node : G.values()) {
			if (node.status == "New") {
				clock = TopoSortDFS(node, clock);
			}
		}
	}
	
	private static int TopoSortDFS(Node node, int clock) {
		node.status = "Active";
		
		// For each edge 'node -> child' verify no cycle
		for (Node child : node.To) {
			if (child.status == "New") {
				clock = TopoSortDFS(child, clock);
			}
			else if (child.status == "Active") {
				// fail lol
				System.out.println("Found cycle at: parent(" + node.name + ") -> child(" + child.name + ")");
			}
		}
		
		node.status = "Done";
		sortedGraph[clock] = node.name;
		clock--;
		return clock;
	}
	
	/**
	 * Driver for a DFS to verify the given graph is acyclic
	 * 
	 * @param G - Graph to verify
	 * @return - Whether or not the graph is acyclic
	 */
	private static boolean IsAcyclic(HashMap<String, Node> G) {
		for (Node node : G.values()) {
			if (node.status == "New") {
				if (HasCycleDFS(node)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Helper DFS algorithm to verify cycle while also searching and marking nodes
	 * 
	 * @param node - Next node to search from
	 * @return - Whether or not the graph has a cycle
	 */
	private static boolean HasCycleDFS(Node node) {
		// Set status
		node.status = "Active";
		
		// For each edge 'node -> child' verify no cycle
		for (Node child : node.To) {
			if (child.status == "Active") {
				return true;
			}
			if (child.status == "New") {
				if (HasCycleDFS(child)) {
					return true;
				}
			}
		}
		node.status = "Done";
		return false;
	}
	
	/**
	 * Helper to reset all nodes
	 */
	private static void SetAllNodesToNew() {
		for (Node node : graph.values()) {
			node.status = "New";
		}
	}
}


class Node {
	public String name;
	
	public HashSet<Node> To;
	public HashSet<Node> From;
	// New, Active, Done
	public String status;
	
	// Default construction
	public Node(String name) {
		this.name = name;
		To = new HashSet<Node>();
		From = new HashSet<Node>();
		
		status = "New";
	}
	
	/**
	 * Add a directed connection
	 * 
	 * @param neighbor - Name of node to connect to
	 */
	public void AddConnection(Node neighbor) {
		To.add(neighbor);
		neighbor.From.add(this);
	}
}