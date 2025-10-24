import java.util.*;

public class Graph {
    private int id;
    private List<String> vertices;
    private List<Edge> edges;

    public Graph(int id, List<String> vertices, List<Edge> edges) {
        this.id = id;
        this.vertices = vertices;
        this.edges = edges;
    }

    // Getters
    public int getId() { return id; }
    public List<String> getVertices() { return vertices; }
    public List<Edge> getEdges() { return edges; }
    public int getVertexCount() { return vertices.size(); }
    public int getEdgeCount() { return edges.size(); }

    // Get adjacency list for Prim's algorithm
    public Map<String, List<Edge>> getAdjacencyList() {
        Map<String, List<Edge>> adjList = new HashMap<>();
        for (String vertex : vertices) {
            adjList.put(vertex, new ArrayList<>());
        }

        for (Edge edge : edges) {
            adjList.get(edge.getFrom()).add(edge);
            // For undirected graph, add reverse edge
            adjList.get(edge.getTo()).add(new Edge(edge.getTo(), edge.getFrom(), edge.getWeight()));
        }

        return adjList;
    }
}