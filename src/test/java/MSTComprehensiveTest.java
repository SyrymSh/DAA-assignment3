import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class MSTComprehensiveTest {

    @Test
    public void testMSTCostIdenticalForBothAlgorithms() {
        // Test multiple graphs of different sizes
        for (int vertices : new int[]{5, 10, 20, 50}) {
            Graph graph = GraphTestUtils.generateConnectedGraph(vertices, 0.4);

            PrimMST prim = new PrimMST();
            KruskalMST kruskal = new KruskalMST();

            MSTResult primResult = prim.findMST(graph);
            MSTResult kruskalResult = kruskal.findMST(graph);

            assertEquals(primResult.getTotalCost(), kruskalResult.getTotalCost(),
                    "MST cost should be identical for graph with " + vertices + " vertices");
        }
    }

    @Test
    public void testMSTHasVMinusOneEdges() {
        Graph graph = GraphTestUtils.generateConnectedGraph(15, 0.5);

        PrimMST prim = new PrimMST();
        MSTResult result = prim.findMST(graph);

        assertEquals(graph.getVertexCount() - 1, result.getMstEdges().size(),
                "MST should have V-1 edges");
    }

    @Test
    public void testMSTIsAcyclic() {
        Graph graph = GraphTestUtils.generateConnectedGraph(10, 0.6);

        PrimMST prim = new PrimMST();
        MSTResult result = prim.findMST(graph);

        // MST should be acyclic - test by checking no duplicate vertices in edge traversal
        assertTrue(GraphTestUtils.isAcyclic(result.getMstEdges(), graph.getVertexCount()),
                "MST should be acyclic");
    }

    @Test
    public void testMSTConnectsAllVertices() {
        Graph graph = GraphTestUtils.generateConnectedGraph(8, 0.4);

        PrimMST prim = new PrimMST();
        MSTResult result = prim.findMST(graph);

        assertTrue(GraphTestUtils.connectsAllVertices(result.getMstEdges(), graph.getVertices()),
                "MST should connect all vertices");
    }

    @Test
    public void testDisconnectedGraphHandling() {
        // Create a disconnected graph
        List<String> vertices = Arrays.asList("A", "B", "C", "D", "E", "F");
        List<Edge> edges = Arrays.asList(
                // First component: A-B-C
                new Edge("A", "B", 1),
                new Edge("B", "C", 2),
                // Second component: D-E-F
                new Edge("D", "E", 3),
                new Edge("E", "F", 4)
                // No connection between the two components
        );

        Graph graph = new Graph(1, vertices, edges);

        PrimMST prim = new PrimMST();
        KruskalMST kruskal = new KruskalMST();

        // Both should handle disconnected graphs gracefully
        MSTResult primResult = prim.findMST(graph);
        MSTResult kruskalResult = kruskal.findMST(graph);

        // For disconnected graphs, MST should connect as many as possible
        // but we should have fewer than V-1 edges
        assertTrue(primResult.getMstEdges().size() < graph.getVertexCount() - 1,
                "Disconnected graph should have MST with fewer than V-1 edges");
        assertTrue(kruskalResult.getMstEdges().size() < graph.getVertexCount() - 1,
                "Disconnected graph should have MST with fewer than V-1 edges");
    }

    @Test
    public void testPerformanceMetricsNonNegative() {
        Graph graph = GraphTestUtils.generateConnectedGraph(20, 0.5);

        PrimMST prim = new PrimMST();
        KruskalMST kruskal = new KruskalMST();

        MSTResult primResult = prim.findMST(graph);
        MSTResult kruskalResult = kruskal.findMST(graph);

        // Execution time should be non-negative
        assertTrue(primResult.getExecutionTime() >= 0, "Prim execution time should be non-negative");
        assertTrue(kruskalResult.getExecutionTime() >= 0, "Kruskal execution time should be non-negative");

        // Operation counts should be positive
        assertTrue(primResult.getOperationsCount() > 0, "Prim operations count should be positive");
        assertTrue(kruskalResult.getOperationsCount() > 0, "Kruskal operations count should be positive");

        // MST cost should be non-negative
        assertTrue(primResult.getTotalCost() >= 0, "MST cost should be non-negative");
        assertTrue(kruskalResult.getTotalCost() >= 0, "MST cost should be non-negative");
    }

    @Test
    public void testResultsReproducible() {
        Graph graph = GraphTestUtils.generateConnectedGraph(25, 0.4);

        PrimMST prim1 = new PrimMST();
        PrimMST prim2 = new PrimMST();
        KruskalMST kruskal1 = new KruskalMST();
        KruskalMST kruskal2 = new KruskalMST();

        // Run same algorithm twice on same graph
        MSTResult primResult1 = prim1.findMST(graph);
        MSTResult primResult2 = prim2.findMST(graph);
        MSTResult kruskalResult1 = kruskal1.findMST(graph);
        MSTResult kruskalResult2 = kruskal2.findMST(graph);

        // Results should be identical for same input
        assertEquals(primResult1.getTotalCost(), primResult2.getTotalCost(),
                "Prim should produce same MST cost for same input");
        assertEquals(kruskalResult1.getTotalCost(), kruskalResult2.getTotalCost(),
                "Kruskal should produce same MST cost for same input");
    }

    @Test
    public void testSmallGraphsCorrectness() {
        // Test specifically with small graphs (4-6 vertices)
        for (int vertices : new int[]{4, 5, 6}) {
            Graph graph = GraphTestUtils.generateConnectedGraph(vertices, 0.7);

            PrimMST prim = new PrimMST();
            KruskalMST kruskal = new KruskalMST();

            MSTResult primResult = prim.findMST(graph);
            MSTResult kruskalResult = kruskal.findMST(graph);

            // Verify all MST properties for small graphs
            assertEquals(primResult.getTotalCost(), kruskalResult.getTotalCost());
            assertEquals(vertices - 1, primResult.getMstEdges().size());
            assertEquals(vertices - 1, kruskalResult.getMstEdges().size());
            assertTrue(GraphTestUtils.isAcyclic(primResult.getMstEdges(), vertices));
            assertTrue(GraphTestUtils.connectsAllVertices(primResult.getMstEdges(), graph.getVertices()));
        }
    }
}