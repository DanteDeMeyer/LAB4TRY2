package messages;

public class FailedInfo extends Message {
    private int previousNode;
    private int nextNode;

    /**
     * Message to send back the info of the failed node
     * @param sender This node ID.
     * @param previousNode The node ID of the previous node for the failed node.
     * @param nextNode The node ID of the next node for the failed node.
     */
    public FailedInfo(int sender, int previousNode, int nextNode) {
        super("FailedInfo", sender, false);
        this.nextNode = nextNode;
        this.previousNode = previousNode;
    }

    public int getPreviousNode() {
        return this.previousNode;
    }
    public int getNextNode(){return this.nextNode;}
}
