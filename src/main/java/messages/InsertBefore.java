package messages;

public class InsertBefore extends Message {
    private int previous;

    /**
     * Message to indicate that the receiving node should insert itself before the sending node.
     * @param sender This node ID. The receiving node should update it's next node to this value.
     * @param previous The next node of the sending node before insertion.
     *                 The receiver should update it's previous node to this value.
     */
    public InsertBefore(int sender, int previous) {
        super("InsertBefore", sender, true);
        this.previous = previous;
    }

    public int getPrevious() {
        return previous;
    }
}
