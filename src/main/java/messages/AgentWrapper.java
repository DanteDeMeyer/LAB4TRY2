package messages;

public class AgentWrapper extends Message {
    private final byte[] agent;

    public AgentWrapper(int sender, byte[] agent) {
        super("AgentWrapper", sender, false);
        this.agent = agent;
    }

    public byte[] getAgent() {
        return agent;
    }
}
