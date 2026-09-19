package com.dronenetwork;

import java.util.LinkedList;
import java.util.Queue;

/**
 * MaxFlowSolver implements the Edmonds-Karp algorithm (which is an implementation
 * of the Ford-Fulkerson method using Breadth-First Search) to find the maximum
 * flow in a flow network.
 * 
 * This is used to optimize drone deployment routes for disaster response.
 */
public class MaxFlowSolver {
    private int numNodes;
    private int[][] capacity; // Original capacities of the flight corridors
    private int[][] residualGraph; // Residual capacities tracking available space

    /**
     * Initializes the solver with a given capacity matrix.
     * @param capacityMatrix 2D adjacency matrix representing maximum drone capacity on each route.
     */
    public MaxFlowSolver(int[][] capacityMatrix) {
        this.numNodes = capacityMatrix.length;
        this.capacity = new int[numNodes][numNodes];
        this.residualGraph = new int[numNodes][numNodes];
        
        // Initialize original capacities and the residual graph
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                this.capacity[i][j] = capacityMatrix[i][j];
                this.residualGraph[i][j] = capacityMatrix[i][j]; // Initially, residual = capacity
            }
        }
    }

    /**
     * Breadth-First Search (BFS) to find an augmenting path from source to sink.
     * Edmonds-Karp uses BFS to guarantee finding the shortest path in terms of number of edges.
     *
     * @param source The source node (Dispatch).
     * @param sink The sink node (Disaster zone).
     * @param parent Array to store the path found (used for path reconstruction).
     * @return true if an augmenting path exists, false otherwise.
     */
    private boolean bfs(int source, int sink, int[] parent) {
        boolean[] visited = new boolean[numNodes];
        Queue<Integer> queue = new LinkedList<>();

        queue.add(source);
        visited[source] = true;
        parent[source] = -1;

        // Standard BFS loop
        while (!queue.isEmpty()) {
            int u = queue.poll();

            for (int v = 0; v < numNodes; v++) {
                // If node 'v' is not visited and there is available residual capacity on edge u->v
                if (!visited[v] && residualGraph[u][v] > 0) {
                    // If we reached the sink node, we found a complete path
                    if (v == sink) {
                        parent[v] = u;
                        return true;
                    }
                    queue.add(v);
                    parent[v] = u;
                    visited[v] = true;
                }
            }
        }
        // No path found
        return false;
    }

    /**
     * Calculates the maximum flow from source to sink using Ford-Fulkerson.
     *
     * @param source The source node index (Dispatch).
     * @param sink The sink node index (Disaster zone).
     * @return The maximum number of drones that can be deployed simultaneously.
     */
    public int solveMaxFlow(int source, int sink) {
        int[] parent = new int[numNodes];
        int maxFlow = 0;

        // Loop as long as there is an augmenting path from source to sink
        while (bfs(source, sink, parent)) {
            // Find the bottleneck capacity (minimum residual capacity) along the found path
            int pathFlow = Integer.MAX_VALUE;

            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, residualGraph[u][v]);
            }

            // Update residual capacities of the edges and reverse edges along the path
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                residualGraph[u][v] -= pathFlow; // Reduce capacity for forward edge
                residualGraph[v][u] += pathFlow; // Increase capacity for reverse edge (allows "undoing" flow)
            }

            // Add the flow of this path to the total max flow
            maxFlow += pathFlow;
        }

        return maxFlow;
    }

    /**
     * Gets the final flow on a specific directed edge after max flow calculation.
     * 
     * @param u Source node of the edge.
     * @param v Destination node of the edge.
     * @return The utilized flow value on the edge.
     */
    public int getFlow(int u, int v) {
        // The utilized flow on edge (u, v) is the original capacity minus the remaining residual capacity.
        return capacity[u][v] - residualGraph[u][v];
    }
}
