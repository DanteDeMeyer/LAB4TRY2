package messages;

public class InsertAfter extends Message {
    private int next;

    /**
     * Message to indicate that the receiving node should insert itself after the sending node.
     * @param sender This node ID. The receiving node should update it's previous node to this value.
     * @param next The next node of the sender before insertion.
     *             The receiving node should update it's next node to this value.
     */
    public InsertAfter(int sender, int next) {
        super("InsertAfter", sender, true);
        this.next = next;
    }

    public int getNext() {
        return next;
    }
}
