import java.util.List;

public class OutputData {
    private List<GraphResult> results;

    public OutputData(List<GraphResult> results) {
        this.results = results;
    }

    // Getters and setters
    public List<GraphResult> getResults() { return results; }
    public void setResults(List<GraphResult> results) { this.results = results; }
}