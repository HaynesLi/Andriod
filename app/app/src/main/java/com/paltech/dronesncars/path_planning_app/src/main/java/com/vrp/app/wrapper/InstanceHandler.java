package com.vrp.app.wrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

import com.vrp.app.VRP;
import com.vrp.app.components.Node;


public class InstanceHandler {

	public static final String EXTENSION = ".txt";
	
	public static void main(String[] args) {
		Args arguments = Args.parse(args);
		ProblemInstance instance = InstanceHandler.createRandom(arguments.numVehicles, arguments.numLocations);
		saveInstance(instance);
	}
	
	public static ProblemInstance loadAndAddVehicle(String id) {
		ProblemInstance base = loadFromID(id);
		base.setNumVehicles(base.getNumVehicles() + 1);
		int pid = Integer.parseInt(base.getProblemID());
		base.setProblemID(String.valueOf(pid + 1));
		saveInstance(base);
		return base;
	}
	
	public static ProblemInstance loadFromID(String id) {
		String inputDir = VRP.EXPERIMENTS_HOME + "/input/";
		String filename = "input_" + id;
		try {
			ProblemInstance instance = parseFromInputStream(new Scanner(new File(inputDir + filename + EXTENSION)));
			instance.setProblemID(id);
			return instance;
		} catch (FileNotFoundException e) {
			System.out.printf("No input file with id %s was found in the main experiment directory:\n%s\n", id, VRP.EXPERIMENTS_HOME);
			return null;
		}
	}
	
	public static void saveInstance(ProblemInstance instance) {
		String inputDir = VRP.EXPERIMENTS_HOME + "/input/";
		String filename = "input_" + instance.getProblemID();
		Utils.writeToFile(inputDir + filename + EXTENSION, instance.toString());
	}
	
	public static void saveSolution(Solution solution) {
		ProblemInstance instance = solution.getInstance();
		String outputDir = VRP.EXPERIMENTS_HOME + "/output/";
		String filename = "solution_" + instance.getProblemID();
		Utils.writeToFile(outputDir + filename + EXTENSION, solution.format());
	}
	
	public static ProblemInstance createRandom(int numVehicles, int numLocations) {
		ProblemInstance instance = new ProblemInstance();

        Random ran = new Random();
        Node[] locations = new Node[numLocations];

		int range = 500;
		boolean[][] taken = new boolean[range][range];

        for (int i = 0; i < numLocations; i++) {
        	int x = -1, y = -1;
        	do {
                x = ran.nextInt(range);
                y = ran.nextInt(range);
        	} while(taken[x][y]);
        	taken[x][y] = true;
        	Node loc = new Node(x, y, i);
    		loc.setRouted(false);
            loc.setDemand(1);
            locations[i] = loc;
        }
        
        Node depot = locations[0];
        depot.setRouted(true);
        
		instance.setProblemID(Utils.generateId());
		instance.setNumVehicles(numVehicles);
		instance.setLocations(locations);
		instance.setDepot(depot);
		instance.calculateDistances();
		
		return instance;
	}
	
	public static ProblemInstance parseFromSdtIn() {
		return parseFromInputStream(new Scanner(System.in));
	}
	
	public static ProblemInstance parseFromInputStream(Scanner scanner) {
		int numVehicles = scanner.nextInt();
		int numLocations = scanner.nextInt();
		
		ProblemInstance instance = new ProblemInstance(numVehicles, numLocations);
		
		Node[] locations = new Node[numLocations];
		
		for(int i = 0; i < numLocations; i++) {
			locations[i] = Node.parseFrom(scanner, i);
			locations[i].setRouted(false);
			locations[i].setDemand(1);
		}
		
		locations[0].setRouted(true);

		instance.setProblemID(Utils.generateId());
		instance.setNumVehicles(numVehicles);
		instance.setLocations(locations);
		instance.setDepot(locations[0]);
		instance.calculateDistances();
		instance.setInfo(true);

		scanner.close();
		
		return instance;
	}

}
