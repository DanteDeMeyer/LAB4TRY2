package messages;

public class PreviousPrevious extends Message {
    private int previous;

    public PreviousPrevious(int sender, int previous) {
        super("PreviousPrevious", sender, false);
        this.previous = previous;
    }

    public int getPrevious() {
        return previous;
    }
}
