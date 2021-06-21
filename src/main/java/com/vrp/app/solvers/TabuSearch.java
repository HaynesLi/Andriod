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
package com.vrp.app.solvers;

import com.vrp.app.Solver;
import com.vrp.app.Runner;
import com.vrp.app.components.Arc;
import com.vrp.app.components.Node;
import com.vrp.app.components.RelocationMove;
import com.vrp.app.components.Result;

import java.util.ArrayList;
import java.util.Random;

/**
 * A tabu-search method is finally implemented for improving the initial solution generated by Nearest Neighbor algoirthm.
 * This tabu search method considers all possible customer relocations (both intra- and inter-route) with respect to the selected tabu policy.
 * The method terminates after 200 iterations.
 */
public class TabuSearch implements Solver {
    private final double TOLERANCE = 0.000001;
    private Random globalRandom = new Random(1);
    private final int numberOfVehicles;
    private final int numberOfCustomers;
    private final ArrayList<Node> allNodes;
    private final double[][] distanceMatrix;
    private Result solution;
    private int[][] tabuArcs;
    private int TABU;

    public TabuSearch(int numberOfVehicles, int numberOfCustomers, ArrayList<Node> allNodes, Node depot, double[][] distanceMatrix) {
        this.numberOfVehicles = numberOfVehicles;
        this.numberOfCustomers = numberOfCustomers;
        this.allNodes = allNodes;
        this.distanceMatrix = distanceMatrix;
        this.solution = new Result();
        initTabuArcs();
    }

    private void initTabuArcs() {
        tabuArcs = new int[allNodes.size()][allNodes.size()];
        for (int i = 0; i < allNodes.size(); i++) {
            Node from = allNodes.get(i);
            for (int j = 0; j < allNodes.size(); j++) {
                Node to = allNodes.get(j);
                tabuArcs[from.getId()][to.getId()] = -1;
            }
        }
    }

    @Override
    public void run() {
        int tabuSearchIterator = 0;
        int MAX_ITERATIONS = 100;
        int bestSolutionIterator = 0;
        StringBuilder debug = new StringBuilder();

        TABU = 18;

        Result bestSolution = Result.cloneSolution(solution);

        RelocationMove relocationMove = new RelocationMove(-1, -1, 0, 0, Double.MAX_VALUE, Double.MAX_VALUE);

        while (tabuSearchIterator < MAX_ITERATIONS) {
            tabuSearchIterator = tabuSearchIterator + 1;
            findBestRelocationMove(relocationMove, solution, distanceMatrix, tabuSearchIterator, bestSolution, numberOfVehicles);

            applyRelocationMove(relocationMove, solution, tabuSearchIterator);

            if (solution.getCost() < (bestSolution.getCost() - TOLERANCE)) {
                bestSolution = Result.cloneSolution(solution);
                bestSolutionIterator = tabuSearchIterator;
            }

            debug.append("Iteration: " + tabuSearchIterator + " Best Cost: " + bestSolution.getCost() + " Current Cost: " + solution.getCost() + "\n");

            if (tabuSearchIterator == 16 || tabuSearchIterator == 17) {
                debug.append("Iteration: " + tabuSearchIterator + "\n");

                for (int j = 0; j < numberOfVehicles; j++) {
                    int vehicle = j + 1;
                    debug.append("New Assignment to Vehicle " + vehicle + ": ");
                    for (int k = 0; k < solution.getRoute().get(j).getNodes().size(); k++) {
                        debug.append(solution.getRoute().get(j).getNodes().get(k).getId() + "  ");
                    }
                    debug.append("\n");
                    debug.append("Route Cost: " + solution.getRoute().get(j).getCost() + " - Route Load: " + solution.getRoute().get(j).getLoad() + "\n");
                    debug.append("\n");
                }
            }
        }
        if (Runner.DEBUG_ROUTES) {
            debug.append("Best Solution Iteration: " + bestSolutionIterator + "\n");
            System.out.println(debug);
        }
    }

    private void findBestRelocationMove(RelocationMove relocationMove, Result currentSolution, double[][] distanceMatrix, int iteration, Result bestSol, int numberOfVehicles) {
        Arc cr;
        ArrayList<Arc> toBeCreated = new ArrayList<>();

        double bestMoveCost = Double.MAX_VALUE;

        for (int from = 0; from < numberOfVehicles; from++) {
            for (int to = 0; to < numberOfVehicles; to++) {
                for (int relFromIndex = 1; relFromIndex < currentSolution.getRoute().get(from).getNodes().size() - 1; relFromIndex++) {

                    Node A = currentSolution.getRoute().get(from).getNodes().get(relFromIndex - 1);
                    Node B = currentSolution.getRoute().get(from).getNodes().get(relFromIndex);
                    Node C = currentSolution.getRoute().get(from).getNodes().get(relFromIndex + 1);

                    for (int afterToInd = 0; afterToInd < currentSolution.getRoute().get(to).getNodes().size() - 1; afterToInd++) {
                        if (afterToInd != relFromIndex && afterToInd != relFromIndex - 1) {
                            Node F = currentSolution.getRoute().get(to).getNodes().get(afterToInd);
                            Node G = currentSolution.getRoute().get(to).getNodes().get(afterToInd + 1);

                            double costRemovedFrom = distanceMatrix[A.getId()][B.getId()] + distanceMatrix[B.getId()][C.getId()];
                            double costRemovedTo = distanceMatrix[F.getId()][G.getId()];

                            double costAddedFrom = distanceMatrix[A.getId()][C.getId()];
                            double costAddedTo = distanceMatrix[F.getId()][B.getId()] + distanceMatrix[B.getId()][G.getId()];

                            double moveCostFrom = costAddedFrom - costRemovedFrom;
                            double moveCostTo = costAddedTo - costRemovedTo;

                            double moveCost = moveCostFrom + moveCostTo;

                            toBeCreated.clear();
                            cr = new Arc(A.getId(), C.getId());
                            toBeCreated.add(cr);
                            cr = new Arc(F.getId(), B.getId());
                            toBeCreated.add(cr);
                            cr = new Arc(B.getId(), G.getId());
                            toBeCreated.add(cr);

                            if (!isTabuArcs(toBeCreated, moveCost, currentSolution, iteration, bestSol)) {
                                if ((moveCost < bestMoveCost) && (from == to || (currentSolution.getRoute().get(to).getLoad() + currentSolution.getRoute().get(from).getNodes().get(relFromIndex).getDemand() <= currentSolution.getRoute().get(to).getCapacity()))) {
                                    bestMoveCost = moveCost;

                                    relocationMove.setPositionOfRelocated(relFromIndex);
                                    relocationMove.setPositionToBeInserted(afterToInd);
                                    relocationMove.setMoveCostTo(moveCostTo);
                                    relocationMove.setMoveCostFrom(moveCostFrom);
                                    relocationMove.setFromRoute(from);
                                    relocationMove.setToRoute(to);
                                    relocationMove.setMoveCost(moveCost);

                                    if (from != to) {
                                        relocationMove.setNewLoadFrom(currentSolution.getRoute().get(from).getLoad() - currentSolution.getRoute().get(from).getNodes().get(relFromIndex).getDemand());
                                        relocationMove.setNewLoadTo(currentSolution.getRoute().get(to).getLoad() + currentSolution.getRoute().get(from).getNodes().get(relFromIndex).getDemand());
                                    } else {
                                        relocationMove.setNewLoadFrom(currentSolution.getRoute().get(from).getLoad());
                                        relocationMove.setNewLoadTo(currentSolution.getRoute().get(to).getLoad());
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void applyRelocationMove(RelocationMove relocationMove, Result currentSolution, int iterations) {
        Node relocatedNode = currentSolution.getRoute().get(relocationMove.getFromRoute()).getNodes().get(relocationMove.getPositionOfRelocated());

        Node A = currentSolution.getRoute().get(relocationMove.getFromRoute()).getNodes().get(relocationMove.getPositionOfRelocated() - 1);
        Node B = currentSolution.getRoute().get(relocationMove.getFromRoute()).getNodes().get(relocationMove.getPositionOfRelocated());
        Node C = currentSolution.getRoute().get(relocationMove.getFromRoute()).getNodes().get(relocationMove.getPositionOfRelocated() + 1);
        Node F = currentSolution.getRoute().get(relocationMove.getToRoute()).getNodes().get(relocationMove.getPositionToBeInserted());
        Node G = currentSolution.getRoute().get(relocationMove.getToRoute()).getNodes().get(relocationMove.getPositionToBeInserted() + 1);

        tabuArcs[A.getId()][B.getId()] = iterations + globalRandom.nextInt(TABU);
        tabuArcs[B.getId()][C.getId()] = iterations + globalRandom.nextInt(TABU);
        tabuArcs[F.getId()][G.getId()] = iterations + globalRandom.nextInt(TABU);

        currentSolution.getRoute().get(relocationMove.getFromRoute()).getNodes().remove(relocationMove.getPositionOfRelocated());


        if ((relocationMove.getPositionToBeInserted() < relocationMove.getPositionOfRelocated()) || relocationMove.getToRoute() != relocationMove.getFromRoute()) {
            currentSolution.getRoute().get(relocationMove.getToRoute()).getNodes().add(relocationMove.getPositionToBeInserted() + 1, relocatedNode);
        } else {
            currentSolution.getRoute().get(relocationMove.getToRoute()).getNodes().add(relocationMove.getPositionToBeInserted(), relocatedNode);
        }

        currentSolution.setCost(currentSolution.getCost() + relocationMove.getMoveCost());
        currentSolution.getRoute().get(relocationMove.getFromRoute()).setCost(currentSolution.getRoute().get(relocationMove.getFromRoute()).getCost() + relocationMove.getMoveCostFrom());

        if (relocationMove.getToRoute() != relocationMove.getFromRoute()) {
            currentSolution.getRoute().get(relocationMove.getToRoute()).setLoad(relocationMove.getNewLoadTo());
            currentSolution.getRoute().get(relocationMove.getFromRoute()).setLoad(relocationMove.getNewLoadFrom());
        } else {
            currentSolution.getRoute().get(relocationMove.getToRoute()).setLoad(relocationMove.getNewLoadTo());
        }

        setSolution(currentSolution);
    }

    private boolean isTabuArcs(ArrayList<Arc> toBeCrt, double moveCost, Result currentSolution, int iteration, Result bestSolution) {
        if ((currentSolution.getCost() + moveCost) < (bestSolution.getCost() - TOLERANCE)) {
            return false;
        }

        for (int i = 0; i < toBeCrt.size(); i++) {
            Arc arc = toBeCrt.get(i);
            if (iteration < tabuArcs[arc.getN1()][arc.getN2()]) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void setSolution(Result solution) {
        this.solution = solution;
    }

    @Override
    public Result getSolution() {
        return solution;
    }
}
