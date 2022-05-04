package messages;

public class NodeCount extends Message {
    private int count;

    /**
     * Message to announce the amount of nodes in the network.
     * @param count The amount of nodes in the network.
     */
    public NodeCount(int count) {
        super("NodeCount", 0, true);
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }
}
