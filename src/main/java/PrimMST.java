import java.util.*;

public class PrimMST {
    private int operationsCount;

    public MSTResult findMST(Graph graph) {
        operationsCount = 0;
        long startTime = System.nanoTime();

        List<Edge> mstEdges = new ArrayList<>();
        int totalCost = 0;

        if (graph.getVertices().isEmpty()) {
            long executionTime = (System.nanoTime() - startTime) / 1_000_000;
            return new MSTResult(mstEdges, totalCost, operationsCount, executionTime);
        }

        Set<String> visited = new HashSet<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>();
        Map<String, List<Edge>> adjList = graph.getAdjacencyList();

        // Start with first vertex
        String startVertex = graph.getVertices().get(0);
        visited.add(startVertex);
        operationsCount++;

        // Add all edges from start vertex to priority queue
        pq.addAll(adjList.get(startVertex));
        operationsCount += adjList.get(startVertex).size();

        while (!pq.isEmpty() && visited.size() < graph.getVertexCount()) {
            Edge edge = pq.poll();
            operationsCount++;

            String nextVertex = null;
            if (visited.contains(edge.getFrom()) && !visited.contains(edge.getTo())) {
                nextVertex = edge.getTo();
            } else if (visited.contains(edge.getTo()) && !visited.contains(edge.getFrom())) {
                nextVertex = edge.getFrom();
            }

            if (nextVertex != null) {
                visited.add(nextVertex);
                mstEdges.add(edge);
                totalCost += edge.getWeight();
                operationsCount += 3;

                // Add edges from the new vertex
                for (Edge e : adjList.get(nextVertex)) {
                    operationsCount++;
                    if (!visited.contains(e.getTo())) {
                        pq.add(e);
                        operationsCount++;
                    }
                }
            }
        }

        long executionTime = (System.nanoTime() - startTime) / 1_000_000;
        return new MSTResult(mstEdges, totalCost, operationsCount, executionTime);
    }
}