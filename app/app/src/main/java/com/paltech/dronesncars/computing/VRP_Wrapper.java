package com.paltech.dronesncars.computing;

import com.vrp.app.Algorithm;
import com.vrp.app.Runner;
import com.vrp.app.components.Node;
import com.vrp.app.wrapper.ProblemInstance;
import com.vrp.app.wrapper.Solution;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class VRP_Wrapper {

    public static List<List<GeoPoint>> get_routes_for_vehicles(int num_of_vehicles, List<GeoPoint> targets) {
        if (targets.size() <= 0 || num_of_vehicles <= 0) { return new ArrayList<>(); }

        ProblemInstance problem_instance = get_instance_for_problem(num_of_vehicles, targets);

        Solution solution = run_problem_instance(problem_instance);

        return get_routes_from_solution(solution, targets);
    }

    private static Solution run_problem_instance(ProblemInstance problem_instance) {
        Runner solver = new Runner();
        solver.setup(problem_instance);

        double best_cost = Float.POSITIVE_INFINITY;
        Solution best_solution = null;

        for (Algorithm algorithm: Algorithm.values()) {
            Solution solution = solver.run(algorithm);

            if (solution.getCosts() < best_cost) {
                best_cost = solution.getCosts();
                best_solution = solution;
            }
        }

        return best_solution;
    }

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

    private static ProblemInstance get_instance_for_problem(int num_of_vehicles, List<GeoPoint> targets) {
        ProblemInstance problem_instance = new ProblemInstance(num_of_vehicles, targets.size());
        Node[] nodes = geo_points_to_nodes(targets);
        problem_instance.setLocations(nodes);

        Node depot = nodes[0];
        problem_instance.setDepot(depot);

        double[][] distances = get_distances_from_geopoints(targets);
        problem_instance.setDistances(distances);

        return problem_instance;
    }

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

    private static Node[] geo_points_to_nodes(List<GeoPoint> geo_points) {
        Node[] nodes = new Node[geo_points.size()];

        for (int id = 0; id < geo_points.size(); id++) {
            Node new_node = new Node(0, 0, id, 1);
            new_node.setRouted(false);
            nodes[id] = new_node;
        }

        return nodes;
    }



}