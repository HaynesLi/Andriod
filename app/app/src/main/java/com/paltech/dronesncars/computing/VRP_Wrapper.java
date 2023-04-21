package com.paltech.dronesncars.computing;

import com.vrp.app.Algorithm;
import com.vrp.app.Runner;
import com.vrp.app.components.Node;
import com.vrp.app.wrapper.ProblemInstance;
import com.vrp.app.wrapper.Solution;
import com.vrp.app.wrapper.Utils;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper class for the Vehicle Routing Problem Library/Framework in {@link com.vrp}
 * TODO does this link work like this?
 */
public class VRP_Wrapper {

    /**
     * compute the best routes for a certain number of vehicles to visit a number of GeoPoints,
     * assuming the first point in the list of targets is the starting point
     *
     * @param num_of_vehicles the number of vehicles to use (e.g. drones, rovers)
     * @param targets the locations to visit
     * @return returns a list of routes (= lists of GeoPoints)
     */
    public static List<List<GeoPoint>> get_routes_for_vehicles(int num_of_vehicles, List<GeoPoint> targets) {
        return get_routes_for_vehicles(num_of_vehicles, targets, 0);
    }

    /**
     * compute the best routes for a certain number of vehicles to visit a number of GeoPoints
     * @param num_of_vehicles the number of vehicles to use (e.g. drones, rovers)
     * @param targets the locations to visit
     * @param index_of_depot the index of the start location inside targets
     * @return returns a list of routes (= lists of GeoPoints)
     */
    public static List<List<GeoPoint>> get_routes_for_vehicles(int num_of_vehicles, List<GeoPoint> targets, int index_of_depot) {
        if (targets.size() <= 0 || num_of_vehicles <= 0) { return new ArrayList<>(); }

        ProblemInstance problem_instance = get_instance_for_problem(num_of_vehicles, targets, index_of_depot);

        Solution solution = run_problem_instance(problem_instance);

        return get_routes_from_solution(solution, targets);
    }

    /**
     * Run a specified problem instance
     * @param problem_instance the problem instance
     * @return returns the Solution to the problem
     */
    private static Solution run_problem_instance(ProblemInstance problem_instance) {
        Runner solver = new Runner();
        solver.setup(problem_instance);

        double best_cost = Float.POSITIVE_INFINITY;
        Solution best_solution = null;

        for (Algorithm algorithm: Algorithm.values()) {
            //if (algorithm == Algorithm.TabuSearch) continue;
            Solution solution = solver.run(algorithm);

            if (solution.getCosts() < best_cost) {
                best_cost = solution.getCosts();
                best_solution = solution;
            }
        }

        return best_solution;
    }

    /**
     * Translate the solution to a problem instance into a list of routes (= lists of GeoPoints)
     * @param solution the solution to translate
     * @param targets the GeoPoints which are part of the solution = all GeoPoints to visit
     * @return a list of routes (= lists of GeoPoints)
     */
    private static List<List<GeoPoint>> get_routes_from_solution(Solution solution, List<GeoPoint> targets) {
        ArrayList<ArrayList<Integer>> routes_with_ids = solution.getRoutes();

        List<List<GeoPoint>> routes = new ArrayList<>();

        for (ArrayList<Integer> id_route: routes_with_ids) {
            List<GeoPoint> route = new ArrayList<>();

            for (int i = 0; i < id_route.size(); i++) {
                int id = id_route.get(i);
                route.add(targets.get(id));
            }

            routes.add(route);
        }

        return routes;
    }

    /**
     * build and prepare the problem instance for a vehicle routing problem with the given number
     * of vehicles and the given targets
     * @param num_of_vehicles the number of vehicles
     * @param targets the GeoPoints to visit
     * @return returns the resulting ProblemInstance, which is ready to be solved by
     *  run_problem_instance(...)
     */
    private static ProblemInstance get_instance_for_problem(int num_of_vehicles, List<GeoPoint> targets, int index_of_depot) {


        ProblemInstance problem_instance = new ProblemInstance();
        problem_instance.setProblemID(Utils.generateId());
        problem_instance.setNumVehicles(num_of_vehicles);
        Node[] nodes = geo_points_to_nodes(targets);
        Node depot = nodes[index_of_depot];
        depot.setRouted(true);
        problem_instance.setLocations(nodes);
        problem_instance.setDepot(depot);

        double[][] distances = get_distances_from_geopoints(targets);
        problem_instance.setDistances(distances);

        problem_instance.setInfo(true);

        return problem_instance;
    }

    /**
     * calculate the distances between a set of GeoPoints
     * @param geo_points the geopoints to calculate the distances for
     * @return returns a NxN matrix of distances, where N = geo_points.size() and
     *  distances[i][j] is the distance between geo_points.get(i) and geo_points.get(j)
     */
    private static double[][] get_distances_from_geopoints(List<GeoPoint> geo_points) {
        double[][] distances = new double[geo_points.size()][geo_points.size()];

        for (int i = 0; i < geo_points.size(); i++) {
            distances[i][i] = 0.0;
            for (int j = i+1; j < geo_points.size(); j++) {
                double distance = geo_points.get(i).distanceToAsDouble(geo_points.get(j));
                distances[i][j] = distance;
                distances[j][i] = distance;
            }
        }

        return distances;
    }

    /**
     * create the corresponding Nodes for a set of GeoPoints
     * @param geo_points the GeoPoints to create the Nodes for
     * @return returns an array of the corresponding Nodes
     */
    private static Node[] geo_points_to_nodes(List<GeoPoint> geo_points) {
        Node[] nodes = new Node[geo_points.size()];

        for (int id = 0; id < geo_points.size(); id++) {
            Node new_node = new Node(id, id, id, 1);
            new_node.setRouted(false);
            nodes[id] = new_node;
        }

        return nodes;
    }
}
