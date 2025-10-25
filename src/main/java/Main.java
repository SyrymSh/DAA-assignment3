import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static void main(String[] args) {
        String[] inputFiles = {
                "src/input/ass_3_input.json",      // Original test graphs
                "src/input/small_graphs.json",     // 5 graphs, up to 50 vertices
                "src/input/medium_graphs.json",    // 10 graphs, up to 300 vertices
                "src/input/large_graphs.json",     // 10 graphs, up to 1000 vertices
                "src/input/extra_large_graphs.json" // 5 graphs, up to 3000 vertices
        };

        List<PerformanceRecord> allRecords = new ArrayList<>();

        for (String inputFile : inputFiles) {
            try {
                List<PerformanceRecord> records = processFile(inputFile);
                allRecords.addAll(records);
            } catch (Exception e) {
                System.out.println("Skipping " + inputFile + ": " + e.getMessage());
            }
        }

        // Generate comprehensive performance report
        try {
            generatePerformanceReport(allRecords);
        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }

    public static List<PerformanceRecord> processFile(String inputFile) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Processing: " + inputFile);
        System.out.println("=".repeat(80));

        InputData inputData = JsonUtils.readInput(inputFile);
        List<Graph> graphs = JsonUtils.convertToGraphs(inputData);

        System.out.println("Loaded " + graphs.size() + " graphs");

        List<GraphResult> results = new ArrayList<>();
        List<PerformanceRecord> records = new ArrayList<>();
        PrimMST prim = new PrimMST();
        KruskalMST kruskal = new KruskalMST();

        for (Graph graph : graphs) {
            int maxPossibleEdges = graph.getVertexCount() * (graph.getVertexCount() - 1) / 2;
            double density = (graph.getEdgeCount() * 100.0) / maxPossibleEdges;

            System.out.println("\n--- Graph " + graph.getId() + " ---");
            System.out.printf("Vertices: %d, Edges: %d (Density: %.1f%%)%n",
                    graph.getVertexCount(), graph.getEdgeCount(), density);

            MSTResult primResult = prim.findMST(graph);
            MSTResult kruskalResult = kruskal.findMST(graph);

            // Verify correctness
            if (primResult.getTotalCost() != kruskalResult.getTotalCost()) {
                System.err.println("❌ COST MISMATCH! Prim: " + primResult.getTotalCost() +
                        ", Kruskal: " + kruskalResult.getTotalCost());
            }

            System.out.printf("Prim:    cost=%-6d time=%-8.3fms ops=%-8d%n",
                    primResult.getTotalCost(),
                    primResult.getExecutionTime() / 1000.0,
                    primResult.getOperationsCount());
            System.out.printf("Kruskal: cost=%-6d time=%-8.3fms ops=%-8d%n",
                    kruskalResult.getTotalCost(),
                    kruskalResult.getExecutionTime() / 1000.0,
                    kruskalResult.getOperationsCount());

            if (primResult.getTotalCost() == kruskalResult.getTotalCost()) {
                System.out.println("✅ Algorithms agree on MST cost");
            }

            double timeRatio = kruskalResult.getExecutionTime() > 0 ?
                    (double) primResult.getExecutionTime() / kruskalResult.getExecutionTime() : 0;
            System.out.printf("Time Ratio (Prim/Kruskal): %.3f%n", timeRatio);

            InputStats stats = new InputStats(graph.getVertexCount(), graph.getEdgeCount());
            MSTOutput primOutput = convertToMSTOutput(primResult);
            MSTOutput kruskalOutput = convertToMSTOutput(kruskalResult);

            results.add(new GraphResult(graph.getId(), stats, primOutput, kruskalOutput));

            // Store performance record with density
            records.add(new PerformanceRecord(
                    inputFile.replace("src/input/", "").replace(".json", ""),
                    graph.getId(),
                    graph.getVertexCount(),
                    graph.getEdgeCount(),
                    density,
                    primResult.getTotalCost(),
                    kruskalResult.getTotalCost(),
                    primResult.getExecutionTime(),
                    kruskalResult.getExecutionTime(),
                    primResult.getOperationsCount(),
                    kruskalResult.getOperationsCount()
            ));
        }

        // Write output
        String outputFile = inputFile.replace("src/input/", "output/").replace(".json", "_output.json");
        File outputDir = new File("output");
        if (!outputDir.exists()) outputDir.mkdirs();

        OutputData outputData = new OutputData(results);
        objectMapper.writeValue(new File(outputFile), outputData);

        System.out.println("Results written to: " + outputFile);
        return records;
    }

    private static MSTOutput convertToMSTOutput(MSTResult result) {
        List<EdgeOutput> edgeOutputs = new ArrayList<>();
        for (Edge edge : result.getMstEdges()) {
            edgeOutputs.add(new EdgeOutput(edge.getFrom(), edge.getTo(), edge.getWeight()));
        }
        return new MSTOutput(edgeOutputs, result.getTotalCost(),
                result.getOperationsCount(), result.getExecutionTime());
    }

    public static void generatePerformanceReport(List<PerformanceRecord> records) throws Exception {
        FileWriter writer = new FileWriter("output/performance_comparison.csv");
        writer.write("Dataset,GraphID,Vertices,Edges,Density,Prim_Cost,Kruskal_Cost,Prim_Time_ms,Kruskal_Time_ms,Prim_Operations,Kruskal_Operations,Time_Ratio,Operations_Ratio\n");

        for (PerformanceRecord record : records) {
            double timeRatio = record.kruskalTime > 0 ? (double) record.primTime / record.kruskalTime : 0;
            double opsRatio = record.kruskalOperations > 0 ? (double) record.primOperations / record.kruskalOperations : 0;

            writer.write(String.format("%s,%d,%d,%d,%.1f,%d,%d,%.3f,%.3f,%d,%d,%.3f,%.3f\n",
                    record.dataset, record.graphId, record.vertices, record.edges, record.density,
                    record.primCost, record.kruskalCost, record.primTime / 1000.0, record.kruskalTime / 1000.0,
                    record.primOperations, record.kruskalOperations, timeRatio, opsRatio));
        }

        writer.close();
        System.out.println("\n📊 Comprehensive performance report saved to: output/performance_comparison.csv");

        // Print summary statistics by dataset and size ranges
        printDetailedSummaryStatistics(records);
    }

    public static void printDetailedSummaryStatistics(List<PerformanceRecord> records) {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("DETAILED PERFORMANCE SUMMARY BY GRAPH SIZE");
        System.out.println("=".repeat(100));

        // Group by dataset and size ranges
        Map<String, List<PerformanceRecord>> byDataset = new HashMap<>();
        for (PerformanceRecord record : records) {
            byDataset.computeIfAbsent(record.dataset, k -> new ArrayList<>()).add(record);
        }

        for (Map.Entry<String, List<PerformanceRecord>> entry : byDataset.entrySet()) {
            String dataset = entry.getKey();
            List<PerformanceRecord> datasetRecords = entry.getValue();

            System.out.printf("%n%s (%d graphs):%n", dataset.toUpperCase(), datasetRecords.size());
            System.out.println("GraphID | Vertices | Density% | Prim Time | Kruskal Time | Time Ratio | Prim Ops | Kruskal Ops");
            System.out.println("--------|----------|----------|-----------|--------------|------------|----------|------------");

            for (PerformanceRecord record : datasetRecords) {
                double timeRatio = record.kruskalTime > 0 ? (double) record.primTime / record.kruskalTime : 0;
                System.out.printf("%7d | %8d | %8.1f | %9.3f | %12.3f | %10.3f | %8d | %10d%n",
                        record.graphId, record.vertices, record.density,
                        record.primTime / 1000.0, record.kruskalTime / 1000.0, timeRatio,
                        record.primOperations, record.kruskalOperations);
            }

            // Averages for the dataset
            double avgPrimTime = datasetRecords.stream().mapToLong(r -> r.primTime).average().orElse(0) / 1000.0;
            double avgKruskalTime = datasetRecords.stream().mapToLong(r -> r.kruskalTime).average().orElse(0) / 1000.0;
            double avgPrimOps = datasetRecords.stream().mapToLong(r -> r.primOperations).average().orElse(0);
            double avgKruskalOps = datasetRecords.stream().mapToLong(r -> r.kruskalOperations).average().orElse(0);
            double avgDensity = datasetRecords.stream().mapToDouble(r -> r.density).average().orElse(0);

            System.out.printf("AVERAGE | %8.0f | %8.1f | %9.3f | %12.3f | %10.3f | %8.0f | %10.0f%n",
                    datasetRecords.stream().mapToInt(r -> r.vertices).average().orElse(0),
                    avgDensity, avgPrimTime, avgKruskalTime,
                    avgKruskalTime > 0 ? avgPrimTime / avgKruskalTime : 0,
                    avgPrimOps, avgKruskalOps);
        }

        // Overall comparison
        System.out.println("\n" + "=".repeat(100));
        System.out.println("OVERALL ALGORITHM COMPARISON");
        System.out.println("=".repeat(100));

        double overallPrimTime = records.stream().mapToLong(r -> r.primTime).average().orElse(0) / 1000.0;
        double overallKruskalTime = records.stream().mapToLong(r -> r.kruskalTime).average().orElse(0) / 1000.0;
        double overallPrimOps = records.stream().mapToLong(r -> r.primOperations).average().orElse(0);
        double overallKruskalOps = records.stream().mapToLong(r -> r.kruskalOperations).average().orElse(0);

        System.out.printf("Prim's Algorithm:     Avg Time = %.3f ms, Avg Operations = %.0f%n",
                overallPrimTime, overallPrimOps);
        System.out.printf("Kruskal's Algorithm:  Avg Time = %.3f ms, Avg Operations = %.0f%n",
                overallKruskalTime, overallKruskalOps);
        System.out.printf("Performance Ratio:    Time = %.3f (Prim/Kruskal), Operations = %.3f (Prim/Kruskal)%n",
                overallPrimTime / overallKruskalTime,
                overallPrimOps / overallKruskalOps);
    }

    static class PerformanceRecord {
        String dataset;
        int graphId;
        int vertices;
        int edges;
        double density;
        int primCost;
        int kruskalCost;
        long primTime;
        long kruskalTime;
        int primOperations;
        int kruskalOperations;

        public PerformanceRecord(String dataset, int graphId, int vertices, int edges, double density,
                                 int primCost, int kruskalCost, long primTime, long kruskalTime,
                                 int primOperations, int kruskalOperations) {
            this.dataset = dataset;
            this.graphId = graphId;
            this.vertices = vertices;
            this.edges = edges;
            this.density = density;
            this.primCost = primCost;
            this.kruskalCost = kruskalCost;
            this.primTime = primTime;
            this.kruskalTime = kruskalTime;
            this.primOperations = primOperations;
            this.kruskalOperations = kruskalOperations;
        }
    }
}