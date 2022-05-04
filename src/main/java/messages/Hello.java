package messages;

public class Hello extends Message {
    /**
     * Message to annnounce this node on the network.
     * @param sender This nodename.
     */
    public Hello(int sender) {
        super("Hello", sender, false);
    }
}
