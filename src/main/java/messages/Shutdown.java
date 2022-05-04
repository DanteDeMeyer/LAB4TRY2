package messages;

public class Shutdown extends Message {

    public Shutdown(int sender) {
        super("Shutdown", sender, false);
    }
}
