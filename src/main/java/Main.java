import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static void main(String[] args) {
        try {
            System.out.println("=== MST Algorithm Comparison ===");

            // Read input data
            String inputFile = "src/input/ass_3_input.json";
            InputData inputData = JsonUtils.readInput(inputFile);
            List<Graph> graphs = JsonUtils.convertToGraphs(inputData);

            System.out.println("Loaded " + graphs.size() + " graphs from " + inputFile);

            // Process each graph
            List<GraphResult> results = new ArrayList<>();
            PrimMST prim = new PrimMST();
            KruskalMST kruskal = new KruskalMST();

            for (Graph graph : graphs) {
                System.out.println("\n--- Processing Graph " + graph.getId() + " ---");
                System.out.println("Vertices: " + graph.getVertexCount() + ", Edges: " + graph.getEdgeCount());

                // Run Prim's algorithm
                MSTResult primResult = prim.findMST(graph);
                System.out.println("Prim's MST - Cost: " + primResult.getTotalCost() +
                        ", Time: " + primResult.getExecutionTime() + "ms" +
                        ", Operations: " + primResult.getOperationsCount());

                // Run Kruskal's algorithm
                MSTResult kruskalResult = kruskal.findMST(graph);
                System.out.println("Kruskal's MST - Cost: " + kruskalResult.getTotalCost() +
                        ", Time: " + kruskalResult.getExecutionTime() + "ms" +
                        ", Operations: " + kruskalResult.getOperationsCount());

                // Convert to output format
                InputStats stats = new InputStats(graph.getVertexCount(), graph.getEdgeCount());
                MSTOutput primOutput = convertToMSTOutput(primResult);
                MSTOutput kruskalOutput = convertToMSTOutput(kruskalResult);

                results.add(new GraphResult(graph.getId(), stats, primOutput, kruskalOutput));
            }

            // Write output
            String outputFile = "output/ass_3_output.json";
            OutputData outputData = new OutputData(results);
            objectMapper.writeValue(new File(outputFile), outputData);

            System.out.println("\n=== Results written to " + outputFile + " ===");
            printSummary(results);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static MSTOutput convertToMSTOutput(MSTResult result) {
        List<EdgeOutput> edgeOutputs = new ArrayList<>();
        for (Edge edge : result.getMstEdges()) {
            edgeOutputs.add(new EdgeOutput(edge.getFrom(), edge.getTo(), edge.getWeight()));
        }
        return new MSTOutput(edgeOutputs, result.getTotalCost(),
                result.getOperationsCount(), result.getExecutionTime());
    }

    private static void printSummary(List<GraphResult> results) {
        System.out.println("\n=== Performance Summary ===");
        for (GraphResult result : results) {
            System.out.println("Graph " + result.getGraph_id() + ":");
            System.out.println("  Prim     - Time: " + result.getPrim().getExecution_time_ms() +
                    "ms, Ops: " + result.getPrim().getOperations_count());
            System.out.println("  Kruskal  - Time: " + result.getKruskal().getExecution_time_ms() +
                    "ms, Ops: " + result.getKruskal().getOperations_count());
            System.out.println("  Both found MST with cost: " + result.getPrim().getTotal_cost());
        }
    }
}