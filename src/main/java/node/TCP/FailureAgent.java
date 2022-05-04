package node.TCP;

import node.Node;

import java.io.IOException;
import java.net.InetAddress;

public class FailureAgent implements Runnable{

    InetAddress inetAddress;
    Node node;

    public FailureAgent(InetAddress inetAddress, Node node) {
        this.inetAddress = inetAddress;
        this.node = node;
    }

    @Override
    public void run() {
        while (true) {
            try {
                ping();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void ping() throws IOException {
        if (!inetAddress.isReachable(10000)){
            node.failure(node.getNextNodeID());
        }

    }
}
