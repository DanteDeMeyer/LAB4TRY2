package nameserver;

import agents.SyncAgent;
import com.google.gson.Gson;
import messages.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestHandler implements Runnable {
    private final NameServer nameServer;
    private final DatagramSocket socket;
    private final DatagramPacket request;

    public RequestHandler(NameServer nameserver, DatagramSocket socket, DatagramPacket request) {
        this.nameServer = nameserver;
        this.socket = socket;
        this.request = request;
    }


    public void run() {
        try {
            // Get destination address and port.
            InetAddress senderAddr = this.request.getAddress();
            int senderPort = this.request.getPort();

            // Get message from request.
            String json = new String(this.request.getData(), 0, this.request.getLength());
            //System.out.println("[NS UDP HANDLER] Request = " + json);
            Gson gson = new Gson();
            Message message = gson.fromJson(json, Message.class);

            // Check sender.
            if (message.getSender() == 0) {
                throw new Exception("Received own message");
            }

            // Generate response message.
            Message response = null;
            switch (message.getType()) {
                case "Hello":
                    nameServer.addNode(message.getSender(), senderAddr.toString().replace("/", ""));
                    response = new NodeCount(nameServer.getCount());
                    System.out.println("[NS UDP HANDLER] Hello from node with ID = " + message.getSender());
                    System.out.println("[NS UDP HANDLER] Current number of nodes = " + nameServer.getCount());
                    break;
                case "Failure":
                    Failure msgFail = gson.fromJson(json, Failure.class);

                    List<Integer> nodes = new ArrayList<>(nameServer.getNodes());
                    Collections.sort(nodes);
                    int failedIndex = nodes.indexOf(msgFail.getDead());

                    if (failedIndex >= 0) {
                        // Get previous.
                        int previous;
                        if (failedIndex == 0) {
                            // Last node.
                            previous = nodes.get(nodes.size() - 1);
                        } else {
                            previous = nodes.get(failedIndex - 1);
                        }

                        // Get next.
                        int next;
                        if (failedIndex == nodes.size() - 1) {
                            // First node.
                            next = nodes.get(0);
                        } else {
                            next = nodes.get(failedIndex + 1);
                        }

                        nameServer.removeNode(msgFail.getDead());

                        response = new FailedInfo(0, previous, next);
                        System.out.println("[NS UDP HANDLER] Node " + msgFail.getSender() + "notified failure of node " + msgFail.getDead() + ".");
                        System.out.println("[NS UDP HANDLER] Replied with prev = " + previous + " and next = " + next + ".");
                        System.out.println("[NS UDP HANDLER] Current number of nodes = " + nameServer.getCount());
                    }
                    break;
                case "Bye":
                    Bye msgBye = gson.fromJson(json, Bye.class);
                    nameServer.removeNode(msgBye.getSender());
                    System.out.println("[NS UDP HANDLER] Node " + msgBye.getSender() + " shut down.");
                    System.out.println("[NS UDP HANDLER] Current number of nodes = " + nameServer.getCount());
                    break;
                case "AgentWrapper":
                    SyncAgent agent = new SyncAgent(json);
                    agent.setParent(nameServer);

                    if (nameServer.getSyncAgentWatcher().update(agent.getAgentIdentifier())) {
                        new Thread(agent).start();
                    }
            }

            // Send response message
            if (response != null) {
                System.out.println("[NS UDP HANDLER] Response = " + gson.toJson(response) + " to " + senderAddr.toString());
                byte[] buffer = gson.toJson(response).getBytes(StandardCharsets.UTF_8);
                this.socket.send(new DatagramPacket(buffer, buffer.length, senderAddr, senderPort));
            }
        } catch (Exception e) {
            System.err.println("[NS UDP HANDLER] " + e);
        }
    }
}
