package com.vrp.app;

import static java.lang.System.nanoTime;

import java.util.ArrayList;

import com.vrp.app.wrapper.Args;
import com.vrp.app.wrapper.InstanceHandler;
import com.vrp.app.wrapper.ProblemInstance;
import com.vrp.app.wrapper.Solution;
import com.vrp.app.wrapper.Utils;

public class VRP {

	public static final String EXPERIMENTS_HOME = System.getenv("EXPERIMENTS_HOME");
	
	public static void main(String[] args) {
		Args arguments = Args.parse(args);

		ProblemInstance instance = InstanceHandler.createRandom(arguments.numVehicles, arguments.numLocations);
		if(!arguments.noSave)
			InstanceHandler.saveInstance(instance);
		
//		ProblemInstance instance = InstanceHandler.loadFromID("269903");

		runInstances(arguments, instance);
	}
	
	private static void runInstances(Args args, ProblemInstance... instances) {
		Runner solver = new Runner();
		for(ProblemInstance instance: instances) {
			solver.setup(instance);
			
			ArrayList<Solution> solutions = new ArrayList<Solution>();
			double bestCost = Float.POSITIVE_INFINITY;
			Solution bestSol = null;
			
			long startTime, duration;
			
			for(Algorithm alg: Algorithm.values()) {
				System.out.printf("Running %s ... \t\t\n", alg);
				startTime = nanoTime();
				Solution sol = solver.run(alg);
				duration = System.nanoTime() - startTime;
				System.out.printf("\tDone in %s\n", Utils.formatNanos(duration));
				
				if(sol.getCosts() < bestCost) {
					bestCost = sol.getCosts();
					bestSol = sol;
				}
				solutions.add(sol);
			}
			
			System.out.println(" ---------------------  Results  ---------------------");
			
			for(Solution sol: solutions) {
				sol.print();
			}

			System.out.println(" ------------------- Best Solution -------------------");
			
			bestSol.print();
			if(!args.noSave)
				InstanceHandler.saveSolution(bestSol);
		}
	}
}
