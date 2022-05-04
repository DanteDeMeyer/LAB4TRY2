package messages;

public class UpdatePrevious extends Message {
    private int previous;

    public UpdatePrevious(int sender, int previous) {
        super("UpdatePrevious", sender, false);
        this.previous = previous;
    }

    public int getPrevious() {
        return previous;
    }
}
