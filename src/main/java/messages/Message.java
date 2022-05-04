package messages;

public class Message {
    private String type;
    private int sender;
    private boolean isResponse;

    public Message(String type, int sender, boolean isResponse) {
        this.type = type;
        this.sender = sender;
        this.isResponse = isResponse;
    }

    public String getType() {
        return this.type;
    }

    public int getSender() {
        return sender;
    }

    public boolean isResponse() {
        return isResponse;
    }
}
