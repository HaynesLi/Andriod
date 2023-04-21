package com.vrp.app.wrapper;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.vrp.app.Algorithm;
import com.vrp.app.components.Node;
import com.vrp.app.components.Result;

public class Solution {
	
	private ArrayList<ArrayList<Integer>> routes;
	private double[] routeCosts;
	private double bestCosts, worstCosts;
	private ProblemInstance instance;
	private Algorithm algorithm;
	
	private Solution(ArrayList<ArrayList<Integer>> routes, double[] routeCosts, double bestCosts, double worstCosts, ProblemInstance instance, Algorithm algorithm) {
		this.routes = routes;
		this.routeCosts = routeCosts;
		this.bestCosts = bestCosts;
		this.worstCosts = worstCosts;
		this.instance = instance;
		this.algorithm = algorithm;
	}
	
	public void print() {
		System.out.println(this.toString());
	}
	
	public String printForDesmos() {
		String asString = String.format(" * * * * Printing for Desmos * * * *\n");
		Node[] locations = instance.getLocations();
		for(Node loc: locations) {
			asString += String.format("%d\t%d\n", loc.getX(), loc.getY());
		}
		asString += String.format("\n");
		for(ArrayList<Integer> rt: routes) {
			for(Integer ix: rt)
				asString += String.format("%d\t%d\n", locations[ix].getX(), locations[ix].getY());
			asString += String.format("\n");
		}
		return asString;
	}
	
	public String format() {
		String asString = "";
		asString += String.format("Algorithm: %s\n", algorithm);
		asString += String.format("MaxRouteCost: %.2f\n", worstCosts);
		asString += String.format("Routes:\n");
		for(ArrayList<Integer> rt: routes) {
			asString += rt.stream().map(i -> i.toString()).collect(Collectors.joining(" ")) + "\n";
		}
		return asString;
	}

	@Override
	public String toString() {
		String asString = String.format("Algorithm:\t%s\nTotal Costs:\t%.2f\nQuality Factor:\t%.3f\n", algorithm, worstCosts, getQualityFactor());
		for(int k = 0; k < routeCosts.length; k++) {
			String path = formatPath(routes.get(k));
			asString += String.format("Vehicle %d (Costs: %.1f):\t%s\n", k+1, routeCosts[k], path);
		}
		return asString;
	}
	
	private static String formatPath(ArrayList<Integer> route) {
		if(route.size() <= 30) {
			return route
					.stream()
			        .map( n -> n.toString() )
			        .collect( Collectors.joining( " -> " ) );
		} else {
			return route.subList(0, 12)
					.stream()
			        .map( n -> n.toString() )
			        .collect( Collectors.joining( " -> " ) )
			       + " -> [ ... ] -> " +
			       route.subList(route.size() - 13, route.size())
					.stream()
			        .map( n -> n.toString() )
			        .collect( Collectors.joining( " -> " ) );
		}
	}
	
	public static Solution from(ProblemInstance instance, Result solution, double[][] distanceMatrix, Algorithm algorithm) {
		int K = instance.getNumVehicles();
		
		ArrayList<ArrayList<Integer>> routes = new ArrayList<ArrayList<Integer>>(); 
		double[] routeCosts = new double[K];
		double worstCosts = 0, bestCosts = Double.POSITIVE_INFINITY;
        for (int j = 0; j < K; j++) {
			routeCosts[j] = 0;
			ArrayList<Integer> currentRoute = new ArrayList<Integer>();
			int prev = -1;
            for (int k = 0; k < solution.getRoute().get(j).getNodes().size(); k++) {
            	int id = solution.getRoute().get(j).getNodes().get(k).getId();
            	currentRoute.add(id);
            	if(k > 0) {
        			routeCosts[j] += distanceMatrix[prev][id];
            	}
    			prev = id;
            }
			routes.add(currentRoute);
			worstCosts = Math.max(worstCosts, routeCosts[j]);
			bestCosts = Math.min(bestCosts, routeCosts[j]);
		}
		
		return new Solution(routes, routeCosts, bestCosts, worstCosts, instance, algorithm);
			
	}

	public ArrayList<ArrayList<Integer>> getRoutes() {
		return routes;
	}

	public double[] getRouteCosts() {
		return routeCosts;
	}

	public ProblemInstance getInstance() {
		return instance;
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}

	public double getShortestVehicleCosts() {
		return bestCosts;
	}

	public double getCosts() {
		return worstCosts;
	}
	
	public double getQualityFactor() {
		return worstCosts / bestCosts;
	}

}
