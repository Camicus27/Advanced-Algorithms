package ShortestPath;

import java.util.*;

public class GenerateMap {

	public static void main(String[] args) {
		 //printRandomRoadConnections(9000, 40000, 12321);
		 printDeliberateRoadConnections(10000, 9999, 12);
	}
	
	/**
	 * Prints out a randomly generated road mapping.
	 */
	public static void printRandomRoadConnections(int numberOfRoads, int numberOfIntersections, long seed) {
		Random rng = new Random(seed);
		System.out.println(numberOfRoads + " " + numberOfIntersections);
		System.out.println(rng.nextInt(numberOfRoads) + " " + rng.nextInt(numberOfRoads));
		
		for (int i = 0; i < numberOfIntersections; i++) {
			int u = rng.nextInt(numberOfRoads);
			String action = "error";
			int v = rng.nextInt(numberOfRoads);
			
			switch (rng.nextInt(3)) {
				case 0:
					action = " left ";
					break;
				case 1:
					action = " right ";
					break;
				case 2:
					action = " straight ";
					break;
			}
			System.out.println(u + action + v);
		}			
	}
	
	/**
	 * Prints out a deliberately generated road mapping.
	 */
	public static void printDeliberateRoadConnections(int numberOfRoads, int numberOfIntersections, long seed) {
		System.out.println(numberOfRoads + " " + numberOfIntersections);
		System.out.println("0 9999");
		
		for (int i = 0; i < numberOfRoads - 1; i++) {
			int u = i;
			String action = " left ";
			int v = i + 1;
			System.out.println(u + action + v);
		}
		
		// System.out.println("0 left 9999");
	}
}
