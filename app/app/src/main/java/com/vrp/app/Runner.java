/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * */
package com.vrp.app;

import com.vrp.app.components.Node;
import com.vrp.app.components.Result;
import com.vrp.app.solvers.LocalSearchIntraAndInterRelocation;
import com.vrp.app.solvers.NearestNeighbor;
import com.vrp.app.solvers.LocalSearchIntraRelocation;
import com.vrp.app.solvers.TabuSearch;
import com.vrp.app.utils.Printer;
import com.vrp.app.wrapper.ProblemInstance;
import com.vrp.app.wrapper.Solution;

import java.util.ArrayList;
import java.util.Random;

public class Runner {
	
	public static boolean DEBUG_ROUTES = false;
	
	private ProblemInstance instance;
	private NearestNeighbor nearestNeighbor;
	private int numLocations;
	private int numVehicles;
	private ArrayList<Node> allNodes;
	private Node depot;
	private double[][] distanceMatrix;
	
	public void setup(ProblemInstance instance) {
		this.instance = instance;
    	numLocations = instance.getNumLocations();
    	numVehicles = instance.getNumVehicles();
    	
		if(instance.hasInfo()) {
	        allNodes = instance.getLocationsAsList(); 
	        depot = instance.getDepot();
	        distanceMatrix = instance.distanceMatrix();
		} else {
	        allNodes = initCustomers(numLocations);
	        depot = allNodes.get(0); 
	        distanceMatrix = initDistanceMatrix(allNodes);
		}

        nearestNeighbor = new NearestNeighbor(numLocations, numVehicles, depot, distanceMatrix, allNodes);
        nearestNeighbor.run();
	}

    public Solution run(Algorithm algorithm) {
        Result finalSolution;
        String solverName = "";

        switch (algorithm) {
            case LocalSearch:
                finalSolution = nearestNeighbor.getSolution();
                solverName = "LocalSearch";
                break;
            case LocalSearchIntraRelocation:
                LocalSearchIntraRelocation localSearchIntraRelocation = new LocalSearchIntraRelocation(numLocations, numVehicles, depot, distanceMatrix, allNodes);
                localSearchIntraRelocation.setSolution(nearestNeighbor.getSolution());
                localSearchIntraRelocation.run();
                finalSolution = localSearchIntraRelocation.getSolution();
                solverName = "LocalSearch with Intra Relocation";
                break;
            case LocalSearchIntraAndInterRelocation:
                LocalSearchIntraAndInterRelocation localSearchIntraAndInterRelocation = new LocalSearchIntraAndInterRelocation(numVehicles, numLocations, allNodes, depot, distanceMatrix);
                localSearchIntraAndInterRelocation.setSolution(nearestNeighbor.getSolution());
                localSearchIntraAndInterRelocation.run();
                finalSolution = localSearchIntraAndInterRelocation.getSolution();
                solverName = "LocalSearch with Intra and Inner Relocation";
                break;
            case TabuSearch:
                TabuSearch tabuSearch = new TabuSearch(numVehicles, numLocations, allNodes, depot, distanceMatrix);
                tabuSearch.setSolution(nearestNeighbor.getSolution());
                tabuSearch.run();
                finalSolution = tabuSearch.getSolution();
                solverName = "\t TABU Search ";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + algorithm);
        }

//        Printer.printResults(numVehicles, finalSolution, solverName);
        
        return Solution.from(instance, finalSolution, distanceMatrix, algorithm);
    }


    private static double[][] initDistanceMatrix(ArrayList<Node> allNodes) {
        double[][] distanceMatrix = new double[allNodes.size()][allNodes.size()];
        for (int i = 0; i < allNodes.size(); i++) {
            Node from = allNodes.get(i);

            for (int j = 0; j < allNodes.size(); j++) {
                Node to = allNodes.get(j);

                double Delta_x = (from.getX() - to.getX());
                double Delta_y = (from.getY() - to.getY());
                double distance = Math.sqrt((Delta_x * Delta_x) + (Delta_y * Delta_y));

                distance = Math.round(distance);

                distanceMatrix[i][j] = distance;
            }
        }
        return distanceMatrix;
    }

    private static ArrayList<Node> initCustomers(int numLocations) {
        ArrayList<Node> allNodes = new ArrayList<>();

        Node depot = new Node(50, 50, 0);
        depot.setRouted(true);
        allNodes.add(depot);

        Random ran = new Random(150589);

        for (int i = 1; i <= numLocations; i++) {
            int x = ran.nextInt(100);
            int y = ran.nextInt(100);
            Node customer = new Node(x, y, i);
            customer.setDemand(1);
            customer.setRouted(false);
            allNodes.add(customer);
        }
        print(allNodes);

        return allNodes;
    }
    
    private static void print(ArrayList<Node> allNodes) {
    	for (Node node: allNodes) {
    		System.out.printf("%2d\t%2d\n", node.getX(), node.getY());
    	}
    }
}
