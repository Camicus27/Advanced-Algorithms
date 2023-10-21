package MazeMonster;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

// Create a method to find how much the player can gather from current maze
// Run this function over and over with different additional monster placements to find the minimum
// Then report back the position (row,col) and the amount of treasure gathered from this setup.

public class MazeMonster {
	
	private static int rows;
	private static int cols;
	private static Node[][] maze;
	private static Queue<Node> bag;
	
	private static Node playerNode;
	
	private static int currentTotalTreasure;
	private static int minAmountOfTreasure;
	private static int currentMonsterPosRow;
	private static int bestMonsterPosRow;
	private static int currentMonsterPosCol;
	private static int bestMonsterPosCol;

	public static void main(String[] args) {
		
		// Make a scanner for gathering input.
		Scanner input = new Scanner (System.in);
		
		// Set some start data
		minAmountOfTreasure = 1000000;
		
		// Get the rows/cols
		rows = input.nextInt();
		cols = input.nextInt();
		maze = new Node[rows][cols];
		bag = new LinkedList<>();
		
		// For each row, take the string input and add each character as 1 Node per column in the row of the maze
		for (int row = 0; row < rows; row++) { 
			// Get the next row
			String inputRow = input.next();
			
			for (int col = 0; col < cols; col++) {
				// Set the Node at 'row' 'col' to be the next character in the string
				Node node = new Node(inputRow.charAt(col) + "");
				// Add some connections!
				AddConnections(node, row, col);
				
				maze[row][col] = node;
				
				if (node.isPlayer)
					playerNode = node;
			}
		}
		input.close();
		
		// Find the best location for the monster!
        MonsterPlacementLocator();
        
        // Output the coordinates of the monster placement
        System.out.println(bestMonsterPosRow + " " + bestMonsterPosCol);
        // Output the amount of treasure the player will collect
        System.out.print(minAmountOfTreasure);
	}
	
	/**
	 * The WFS algorithm.
	 */
	private static void MonsterPlacementLocator() {
		// For each row
		for (int row = 1; row < rows; row++) {
			// For each col
			for (int col = 1; col < cols; col++) {
				if (maze[row][col].isEmpty && !maze[row][col].IsNextToPlayer()) {
					// Set the position to a monster
					maze[row][col].isEmpty = false;
					maze[row][col].isMonster = true;
					maze[row][col].representation = "m";
					// Set the current data
					currentMonsterPosRow = row;
					currentMonsterPosCol = col;
					currentTotalTreasure = 0;
					// Try and find as much treasure as possible with new placement
					FindTreasures();
					// Clear the checks to start fresh for next search
					ClearCheckMarks();
					// Reset the position to empty
					maze[row][col].isEmpty = true;
					maze[row][col].isMonster = false;
					maze[row][col].representation = ".";
				}
			}
		}
	}
	
	/**
	 * Moves player to find as much treasure as possible
	 */
	private static void FindTreasures() {
		// Add the player to the bag
		bag.offer(playerNode);
		// While the bag still has positions to check
		while (bag.size() > 0) {
			// Remove the next position to check
			Node currentNode = bag.remove();
			
			if (currentNode.isTreasure) {
				// Add treasure amount to the total collected so far
				currentTotalTreasure += currentNode.treasureValue;
			}
			
			// If not smelly, add all adjacent, not yet checked, and non-wall tiles to the bag
			if (!currentNode.HasMonsterConnection()) {
				for (Node node : currentNode.connections) {
					if (!node.hasBeenChecked) {
						bag.add(node);
						node.hasBeenChecked = true;
					}
				}
			}
		}
		
		// Check if path collected less than current min treasure, set as current best case
		if (currentTotalTreasure < minAmountOfTreasure) {
			minAmountOfTreasure = currentTotalTreasure;
			bestMonsterPosCol = currentMonsterPosCol;
			bestMonsterPosRow = currentMonsterPosRow;
		}
	}
	
	/**
	 * Adds a connection from node to node with certain parameters
	 */
	private static void AddConnections(Node node, int row, int col) {
		// There is a connection to the West if not the leftmost column
		if (col != 0) {
			node.AddConnection(maze[row][col - 1]);
		}
		// There is a connection to the North if not the topmost row
		if (row != 0) {
			node.AddConnection(maze[row - 1][col]);
		}
	}
	
	/**
	 * Iterate every row/column and clear the marks off all nodes
	 */
	private static void ClearCheckMarks() {
		// For each row
		for (int row = 1; row < rows; row++) {
			// For each col
			for (int col = 1; col < cols; col++) {
				if (maze[row][col].hasBeenChecked)
					maze[row][col].hasBeenChecked = false;
			}
		}
	}
}

class Node {
	public String representation;
	
	public boolean hasBeenChecked;
	public boolean isEmpty;
	public boolean isWall;
	public boolean isMonster;
	public boolean isPlayer;
	public boolean isTreasure;
	public int treasureValue;
	public HashSet<Node> connections;
	
	
	public Node(String whatItIs) {
		representation = whatItIs;
		connections = new HashSet<Node>();
		
		switch (representation) {
			case ".":
				isEmpty = true;
				break;
			case "#":
				isWall = true;
				break;
			case "m":
				isMonster = true;
				break;
			case "p":
				isPlayer = true;
				hasBeenChecked = true;
				break;
			default:
				isTreasure = true;
				treasureValue = Integer.parseInt(representation);
				break;
		}
	}
	
	/**
	 * If the node is not a wall, add it to each other's connections
	 * 
	 * @param neighbor - Node to connect to
	 */
	public void AddConnection(Node neighbor) {
		if (!this.isWall) {
			neighbor.connections.add(this);
		}
		if (!neighbor.isWall) {
			connections.add(neighbor);
		}
	}
	
	/**
	 * Check if the current node is smelly (connected to a monster)
	 */
	public boolean HasMonsterConnection() {
		boolean isNextToMonster = false;
		for (Node node : connections) {
			if (node.isMonster)
				isNextToMonster = true;
		}
		return isNextToMonster || isMonster;
	}
	
	/**
	 * Check if the current node is connected to a player
	 */
	public boolean IsNextToPlayer() {
		boolean isNextToPlayer = false;
		for (Node node : connections) {
			if (node.isPlayer)
				isNextToPlayer = true;
		}
		return isNextToPlayer;
	}
}
