import java.util.*;

public class GraphTestUtils {

    public static Graph generateConnectedGraph(int vertexCount, double density) {
        Random random = new Random(42); // Fixed seed for reproducibility
        List<String> vertices = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        // Generate vertex names
        for (int i = 0; i < vertexCount; i++) {
            vertices.add("V" + i);
        }

        Set<String> addedEdges = new HashSet<>();

        // Ensure connectivity - create spanning tree first
        List<Integer> connected = new ArrayList<>();
        connected.add(0);
        List<Integer> unconnected = new ArrayList<>();
        for (int i = 1; i < vertexCount; i++) {
            unconnected.add(i);
        }

        while (!unconnected.isEmpty()) {
            int fromIndex = random.nextInt(connected.size());
            int toIndex = random.nextInt(unconnected.size());
            int from = connected.get(fromIndex);
            int to = unconnected.get(toIndex);

            int weight = 1 + random.nextInt(100);
            edges.add(new Edge("V" + from, "V" + to, weight));
            addedEdges.add(Math.min(from, to) + "-" + Math.max(from, to));

            connected.add(to);
            unconnected.remove(toIndex);
        }

        // Add additional edges based on density
        int maxPossibleEdges = vertexCount * (vertexCount - 1) / 2;
        int targetEdges = (int) (maxPossibleEdges * density);
        targetEdges = Math.max(targetEdges, vertexCount - 1);

        while (edges.size() < targetEdges && edges.size() < maxPossibleEdges) {
            int from = random.nextInt(vertexCount);
            int to = random.nextInt(vertexCount);

            if (from != to) {
                String edgeKey = Math.min(from, to) + "-" + Math.max(from, to);
                if (!addedEdges.contains(edgeKey)) {
                    int weight = 1 + random.nextInt(100);
                    edges.add(new Edge("V" + from, "V" + to, weight));
                    addedEdges.add(edgeKey);
                }
            }
        }

        return new Graph(1, vertices, edges);
    }

    public static boolean isAcyclic(List<Edge> edges, int vertexCount) {
        // Use Union-Find to check for cycles
        UnionFind uf = new UnionFind(vertexCount);

        for (Edge edge : edges) {
            int from = Integer.parseInt(edge.getFrom().substring(1));
            int to = Integer.parseInt(edge.getTo().substring(1));

            if (uf.find(from) == uf.find(to)) {
                return false; // Cycle detected
            }
            uf.union(from, to);
        }
        return true;
    }

    public static boolean connectsAllVertices(List<Edge> edges, List<String> vertices) {
        if (edges.isEmpty() && vertices.size() > 1) return false;
        if (edges.isEmpty() && vertices.size() == 1) return true;

        Set<String> connectedVertices = new HashSet<>();
        for (Edge edge : edges) {
            connectedVertices.add(edge.getFrom());
            connectedVertices.add(edge.getTo());
        }

        return connectedVertices.size() == vertices.size();
    }

    // Simple Union-Find for cycle detection
    static class UnionFind {
        private int[] parent;
        private int[] rank;

        public UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }

        public int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        public void union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);

            if (rootX != rootY) {
                if (rank[rootX] < rank[rootY]) {
                    parent[rootX] = rootY;
                } else if (rank[rootX] > rank[rootY]) {
                    parent[rootY] = rootX;
                } else {
                    parent[rootY] = rootX;
                    rank[rootX]++;
                }
            }
        }
    }
}