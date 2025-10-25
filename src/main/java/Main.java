import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;

public class Main {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static void main(String[] args) {
        try {

        InputData inputData = JsonUtils.readInput(inputFile);
        List<Graph> graphs = JsonUtils.convertToGraphs(inputData);


        List<GraphResult> results = new ArrayList<>();
        PrimMST prim = new PrimMST();
        KruskalMST kruskal = new KruskalMST();

        for (Graph graph : graphs) {

            MSTResult primResult = prim.findMST(graph);
            MSTResult kruskalResult = kruskal.findMST(graph);

            InputStats stats = new InputStats(graph.getVertexCount(), graph.getEdgeCount());
            MSTOutput primOutput = convertToMSTOutput(primResult);
            MSTOutput kruskalOutput = convertToMSTOutput(kruskalResult);

            results.add(new GraphResult(graph.getId(), stats, primOutput, kruskalOutput));
        }

        // Write output
        OutputData outputData = new OutputData(results);
        objectMapper.writeValue(new File(outputFile), outputData);

    }

    private static MSTOutput convertToMSTOutput(MSTResult result) {
        List<EdgeOutput> edgeOutputs = new ArrayList<>();
        for (Edge edge : result.getMstEdges()) {
            edgeOutputs.add(new EdgeOutput(edge.getFrom(), edge.getTo(), edge.getWeight()));
        }
        return new MSTOutput(edgeOutputs, result.getTotalCost(),
                result.getOperationsCount(), result.getExecutionTime());
    }

        }
    }
}