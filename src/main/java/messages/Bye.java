package messages;

public class Bye extends Message {
    private int previous;
    private int next;

    /**
     * Message to indicate to this node's neighbours is shutting down.
     * @param sender This node ID.
     * @param previous The node ID of this node's previous node. The receiver that has this node as its previous node
     *                 should update his previous node with this value.
     * @param next The node ID of this node's next node. The receiver that has this node as its next node should update
     *             his next node to this value.
     */
    public Bye(int sender, int previous, int next) {
        super("Bye", sender, false);
        this.previous = previous;
        this.next = next;
    }

    public int getPrevious() {
        return previous;
    }

    public int getNext() { return next; }
}
