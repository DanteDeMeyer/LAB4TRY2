package messages;

public class Failure extends Message {
    private int dead;

    /**
     * Message to indicate that a node has failed to respond.
     * @param sender This node ID.
     * @param dead The node ID of the node that failed.
     */
    public Failure(int sender, int dead) {
        super("Failure", sender, false);
        this.dead = dead;
    }

    public int getDead() {
        return this.dead;
    }
}
