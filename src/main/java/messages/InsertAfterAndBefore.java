package messages;

public class InsertAfterAndBefore extends Message{
    private int previous;
    private int next;

    /**
     * Message to indicate that the receiving node should insert itself before the sending node.
     * @param sender This node ID. The receiving node should update it's next node to this value.
     * @param previous The next node of the sending node before insertion.
     *                 The receiver should update it's previous node to this value.
     */
    public InsertAfterAndBefore(int sender, int previous, int next) {
        super("InsertAfterAndBefore", sender, true);
        this.previous = previous;
        this.next = next;
    }

    public int getPrevious() {
        return previous;
    }

    public int getNext() {
        return next;
    }

}


