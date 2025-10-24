import java.util.*;

public class KruskalMST {
    private int operationsCount;

    public MSTResult findMST(Graph graph) {
        operationsCount = 0;
        long startTime = System.nanoTime();

        List<Edge> mstEdges = new ArrayList<>();
        int totalCost = 0;

        // Sort edges by weight
        List<Edge> sortedEdges = new ArrayList<>(graph.getEdges());
        Collections.sort(sortedEdges);
        operationsCount += (int) (sortedEdges.size() * Math.log(sortedEdges.size()));

        UnionFind uf = new UnionFind(graph.getVertices());

        for (Edge edge : sortedEdges) {
            operationsCount++;
            if (mstEdges.size() == graph.getVertexCount() - 1) break;

            String fromRoot = uf.find(edge.getFrom());
            String toRoot = uf.find(edge.getTo());
            operationsCount += 2;

            if (!fromRoot.equals(toRoot)) {
                mstEdges.add(edge);
                totalCost += edge.getWeight();
                uf.union(edge.getFrom(), edge.getTo());
                operationsCount += 3;
            }
        }

        long executionTime = (System.nanoTime() - startTime) / 1_000_000;
        return new MSTResult(mstEdges, totalCost, operationsCount, executionTime);
    }

    // Union-Find data structure for Kruskal's algorithm
    private static class UnionFind {
        private Map<String, String> parent;
        private Map<String, Integer> rank;

        public UnionFind(List<String> vertices) {
            parent = new HashMap<>();
            rank = new HashMap<>();
            for (String vertex : vertices) {
                parent.put(vertex, vertex);
                rank.put(vertex, 0);
            }
        }

        public String find(String x) {
            if (!parent.get(x).equals(x)) {
                parent.put(x, find(parent.get(x)));
            }
            return parent.get(x);
        }

        public void union(String x, String y) {
            String rootX = find(x);
            String rootY = find(y);

            if (rootX.equals(rootY)) return;

            if (rank.get(rootX) < rank.get(rootY)) {
                parent.put(rootX, rootY);
            } else if (rank.get(rootX) > rank.get(rootY)) {
                parent.put(rootY, rootX);
            } else {
                parent.put(rootY, rootX);
                rank.put(rootX, rank.get(rootX) + 1);
            }
        }
    }
}