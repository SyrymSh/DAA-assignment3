import com.fasterxml.jackson.annotation.JsonProperty;

public class EdgeInput {
    private String from;
    private String to;
    private int weight;

    public EdgeInput() {}

    public EdgeInput(String from, String to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    // Getters and setters
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
}