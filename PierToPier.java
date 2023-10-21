package PierToPier;

import java.util.ArrayList;
import java.util.Scanner;

public class PierToPier {

	private static long currentBestSolutionSet;
	private static int minNumOfDestinations;
    private static int totalNumOfDestinations;
    private static int totalNumOfConnections;
    private static long[] possibleConnectionsSet;
    private static long completeSolutionSet;
	
	public static void main(String[] args) {
		
		// Make a scanner for gathering input.
		Scanner input = new Scanner (System.in);
		
		System.out.print("How many destinations? ");
		totalNumOfDestinations = input.nextInt();
		minNumOfDestinations = totalNumOfDestinations;
		
		System.out.print("How many connections? ");
		totalNumOfConnections = input.nextInt();
		
		System.out.println("Please type all the connections ('a b' is a connects to b)");
		
		// Loop numOfDestinations times to build the complete solution
		ArrayList<Integer> completeSetList = new ArrayList<Integer>();
		possibleConnectionsSet = new long[totalNumOfDestinations + 1]; // excludes 0; (1-n)
		for (int i = 1; i <= totalNumOfDestinations; i++) { 
			completeSolutionSet = completeSolutionSet | (1L << i);
		}
		
		// Loop numOfConnections times to build the set of connections
		for (int i = 0; i < totalNumOfConnections; i++) {
			// Get and store the next connection, a -> b  &  b -> a
			int a = input.nextInt();
			int b = input.nextInt();
			possibleConnectionsSet[a] = possibleConnectionsSet[a] | (1L << a);
			possibleConnectionsSet[a] = possibleConnectionsSet[a] | (1L << b);
			possibleConnectionsSet[b] = possibleConnectionsSet[b] | (1L << a);
			possibleConnectionsSet[b] = possibleConnectionsSet[b] | (1L << b);
		}
        
        Driver();
        
        // Output the number of destinations
        System.out.println(minNumOfDestinations);
        
        // Build a string of all the destinations included in the solution
        String destinations = "";
        for (int i = 1; i <= totalNumOfDestinations; i++) {
        	if ((currentBestSolutionSet & (1L << i)) != 0)  // checks if position i is set
            	destinations += i + " ";
        }
        // Output the actual solution destinations
        System.out.println(destinations.strip());
        
        input.close();
	}
	
	/**
	 * The driver for the backtracking algorithm. Sets up the solve step with initial parameters
	 */
	private static void Driver() {
		Solver(0L, 0L, 1);
	}
	
	/** 
	 * Solving step of a backtracking algorithm.
	 * The use of hash sets allows for the stopping of duplications, and if there are duplications
	 * we can potentially prune the search, allowing for faster run times.
	 * 
	 * destinations stores current destinations that are part of the solution.   position:  6,4,3
	 * destinations is a long bit mask, thus if destinations includes 3,4,6, destinations = 1011000
	 * 															the position excludes 0, a.k.a. starts at 1
	 * 
	 * destinationConnections works the same way, but stores the total connections possible at this point of the recursion
	 * 
	 * @param destinations - partial set of destinations to place shops				
	 * @param destinationConnections - partial set of all the connections of the set destinations
	 * @param currentDestination - the potential destination to add to the solution
	 */
	private static boolean Solver(long destinations, long destinationConnections, int currentDestination) {
		// If this set of destinationConnections includes every destination possible, it is a solution
		if (destinationConnections == completeSolutionSet) {
	        // Check if this is the current best solution and set if it is
			int bitCount = Long.bitCount(destinations);
	        if (bitCount <= minNumOfDestinations) {
	        	minNumOfDestinations = bitCount;
	        	currentBestSolutionSet = destinations;
	        }
	        return false;
		}
		
		// If we have ran out of destinations to check, stop
		if (currentDestination > totalNumOfDestinations) {
			return false;
		}
		
		// If the destination count is more than the current minimum destination solution, cut it short
		if(minNumOfDestinations < Long.bitCount(destinations)) {
			return false;
		}
		
		// If the destination has no connections, you MUST include it
		if (possibleConnectionsSet[currentDestination] == 0) {
			// Keep searching with the next destination (including the current)
			if (Solver((destinations | (1L << currentDestination)), (destinationConnections | (1L << currentDestination)), currentDestination + 1)) {
				return true;
			}
			else
				return false;
		}
		
		// If the connections of the destination are not all already in the partial set of connections, search 2 paths, with/without it
		if ((destinationConnections & possibleConnectionsSet[currentDestination]) != possibleConnectionsSet[currentDestination]) {
			// Keep searching with the next destination (including the current)
			if (Solver((destinations | (1L << currentDestination)), (destinationConnections | possibleConnectionsSet[currentDestination]), currentDestination + 1)) {
				return true;
			}
			
			// Keep searching with the next destination (not including the current)
			if (Solver(destinations, destinationConnections, currentDestination + 1)) {
				return true;
			}
		}
		// Has all duplicate connections, skip it
		else {
			// Keep searching with the next destination
			if (Solver(destinations, destinationConnections, currentDestination + 1)) {
				return true;
			}
		}
		
		// None of the solution conditions were true, no solution found
		return false;
	}

}