package messages;

public class UpdateNext extends Message {
    private int next;

    public UpdateNext(int sender, int next) {
        super("UpdateNext", sender, false);
        this.next = next;
    }

    public int getNext() { return next; }
}
