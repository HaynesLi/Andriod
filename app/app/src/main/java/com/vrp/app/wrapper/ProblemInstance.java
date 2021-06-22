package com.vrp.app.wrapper;

import java.util.ArrayList;
import java.util.Arrays;

import com.vrp.app.components.Node;

public class ProblemInstance {
	
	private String problemID;
	private int numVehicles;
	private int numLocations;
	private Node[] locations;
	private Node depot;
	private double[][] distances;
	private boolean hasInfo = true;
	
	public ProblemInstance() {}
	
	public ProblemInstance(int numVehicles, int numLocations) {
		this.numVehicles = numVehicles;
		this.numLocations = numLocations;
		this.hasInfo = false;
	}
	
	public void print() {
		System.out.printf("%d %d\n", numVehicles, numLocations);
		for(Node location: locations) {
			System.out.printf("%d\t%d\n", location.getX(), location.getY());
		}
	}
	
	@Override
	public String toString() {
		String asString = "";
		asString += String.format("%d %d\n", numVehicles, numLocations);
		for(Node location: locations) {
			asString += String.format("%d\t%d\n", location.getX(), location.getY());
		}
		return asString;
	}
	
	public void calculateDistances() {
		for(int i = 0; i < numLocations; i++) {
			distances[i][i] = 0;
			for(int j = i+1; j < numLocations; j++) {
				double dist = Utils.distanceBetween(locations[i], locations[j]);
				distances[i][j] = dist;
				distances[j][i] = dist;
			}
		}
	}
	
	public String getProblemID() {
		return problemID;
	}

	public void setProblemID(String problemID) {
		this.problemID = problemID;
	}

	public Node[] getLocations() {
		return locations;
	}

	public void setLocations(Node[] locations) {
		this.locations = locations;
		setNumLocations(locations.length);
		setDistances(new double[locations.length][locations.length]);
	}

	public int getNumVehicles() {
		return numVehicles;
	}

	public void setNumVehicles(int numVehicles) {
		this.numVehicles = numVehicles;
	}
	public int getNumLocations() {
		return numLocations;
	}

	private void setNumLocations(int numLocations) {
		this.numLocations = numLocations;
	}

	public void setDistances(double[][] distances) {
		this.distances = distances;
	}
	
	public double getDistance(int i, int j) {
		Utils.checkArrayBounds(i, numLocations);
		Utils.checkArrayBounds(j, numLocations);
		return distances[i][j];
	}

	public Node getDepot() {
		return depot;
	}

	public void setDepot(Node depot) {
		this.depot = depot;
	}

	public ArrayList<Node> getLocationsAsList() {
		return new ArrayList<>(Arrays.asList(getLocations()));
	}

	public double[][] distanceMatrix() {
		return distances;
	}

	public boolean hasInfo() {
		return hasInfo;
	}

	public void setInfo(boolean hasInfo) {
		this.hasInfo = hasInfo;
	}
	
}
