package node.UDP;

import agents.SyncAgent;
import com.google.gson.Gson;
import common.Device;
import messages.*;
import node.Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class RequestHandler implements Runnable {
    private final Node node;
    private final DatagramSocket socket;
    private final DatagramPacket request;
    private ArrayList<Message> responses;

    public RequestHandler(Node node, DatagramSocket socket, DatagramPacket request, ArrayList<Message> responses) {
        this.node = node; // node that is receiving
        this.socket = socket; // socket of the one who received
        this.request = request; // request from other node
        this.responses = responses; // idk
    }

    public void run()  {
        try {
            // Get destination address and port.
            InetAddress senderAddr = this.request.getAddress(); // get IP sender
            int senderPort = this.request.getPort(); // get port sender
            // note: now all sender/receiver ports/IP's are known

            // Get message from request.
            String json = new String(this.request.getData(), 0, this.request.getLength());
            //System.out.println("[UDP HANDLER] Request = " + json);
            Gson gson = new Gson();
            Message message = gson.fromJson(json, Message.class); // https://www.tutorialspoint.com/gson/gson_quick_guide.htm

            // Check sender.
            if (message.getSender() == node.getNodeID()) {
                throw new Exception("Received own message");
            }

            // Process request (and generate response message).
            Message response = null;
            switch (message.getType()) {
                case "Hello":
                    Hello msgHello = gson.fromJson(json, Hello.class);

                    int senderID = msgHello.getSender();
                    if (node.getNodeID() < senderID && senderID < node.getNextNodeID()) {
                        System.out.println("[UDP HANDLER] Insert after");
                        response = new InsertAfter(node.getNodeID(), node.getNextNodeID());
                        node.setNextNodeID(senderID);
                        if (node.getReplicaManager() != null)
                            node.getReplicaManager().handleNewNodeAfter(senderID);
                    } else if (node.getPreviousNodeID() < senderID && senderID < node.getNodeID()) {
                        System.out.println("[UDP HANDLER] Insert before");
                        response = new InsertBefore(node.getNodeID(), node.getPreviousNodeID());
                        node.setPreviousNodeID(senderID);
                    } else if (node.getNextNodeID() < node.getNodeID() && senderID < node.getNextNodeID()) { // End of chain
                        System.out.println("[UDP HANDLER] Insert after");
                        response = new InsertAfter(node.getNodeID(), node.getNextNodeID());
                        node.setNextNodeID(senderID);
                        node.getReplicaManager().handleNewNodeAfter(senderID);
                    } else if (node.getPreviousNodeID() > node.getNodeID() && senderID < node.getNodeID()) { // Begin of chain
                        node.setPreviousNodeID(senderID);
                    } else if (node.getPreviousNodeID() == node.getNextNodeID() && node.getNodeID() == node.getPreviousNodeID()){
                        System.out.println("[UDP HANDLER] Insert after and before");
                        node.setNextNodeID(senderID);
                        node.setPreviousNodeID(senderID);
                        response = new InsertAfterAndBefore(node.getNodeID(), node.getNodeID(), node.getNodeID());

                        node.getReplicaManager().handleNewNodeAfter(senderID);
                    }
                    break;
                case "NodeCount":
                    NodeCount msgNodeCount = gson.fromJson(json, NodeCount.class);
                    node.setNameServerIpAddress(senderAddr);
                    int nodeCount = msgNodeCount.getCount();
                    if (nodeCount <= 1) {
                        node.setNextNodeID(node.getNodeID());
                        node.setPreviousNodeID(node.getNodeID());
                    }
                    break;
                case "Bye":
                    Bye msgBye = gson.fromJson(json, Bye.class);

                    if (msgBye.getSender() == node.getPreviousNodeID()) {
                        node.setPreviousNodeID(msgBye.getPrevious());
                    } else if (msgBye.getSender() == node.getNextNodeID()) {
                        node.setNextNodeID(msgBye.getNext());
                    }
                    break;
                case "InsertBefore":
                    InsertBefore msgInBef = gson.fromJson(json, InsertBefore.class);

                    node.setPreviousNodeID(msgInBef.getPrevious());
                    node.setNextNodeID(msgInBef.getSender());
                    break;
                case "InsertAfter":
                    InsertAfter msgInAft = gson.fromJson(json, InsertAfter.class);

                    node.setPreviousNodeID(msgInAft.getSender());
                    node.setNextNodeID(msgInAft.getNext());
                    break;
                case "InsertAfterAndBefore":
                    InsertAfterAndBefore msgInAftBef = gson.fromJson(json, InsertAfterAndBefore.class);
                    node.setPreviousNodeID(msgInAftBef.getPrevious());
                    node.setNextNodeID(msgInAftBef.getNext());
                    break;
                case "FailedInfo":
                    System.out.println("[UDP HANDLER] Failed node info");
                    FailedInfo msgFailedInf = gson.fromJson(json, FailedInfo.class);

                    int failedNextNodeID = msgFailedInf.getNextNode();
                    int failedPrevNodeID = msgFailedInf.getPreviousNode();
                    int nodeID = this.node.getNodeID();

                    //update 'next node' of previous node
                    String outMsg = new Gson().toJson(new UpdateNext(nodeID, failedNextNodeID));
                    byte[] buffer = outMsg.getBytes();
                    // get IP of the failed nodes next Nodes IP
                    InetAddress failedNextNodeIP = this.node.httpRequestor.getIpFromNodeID(node.getNameServerIpAddress(), node.getPort()+1, failedPrevNodeID);
                    DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, failedNextNodeIP, senderPort);
                    try {
                        socket.send(sendPacket);
                        System.out.println("[UDP HANDLER] UpdateNext message sent to node " + failedPrevNodeID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // update 'previous node' of next node
                    outMsg = new Gson().toJson(new UpdatePrevious(nodeID, failedPrevNodeID));
                    buffer = outMsg.getBytes();
                    // get IP of the failed nodes previous Nodes IP
                    InetAddress failedPrevNodeIP = this.node.httpRequestor.getIpFromNodeID(node.getNameServerIpAddress(), node.getPort()+1, failedPrevNodeID);
                    sendPacket = new DatagramPacket(buffer, buffer.length, failedPrevNodeIP, senderPort);
                    try {
                        socket.send(sendPacket);
                        System.out.println("[UDP HANDLER] UpdatePrevious message sent to node " + failedNextNodeID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case "UpdateNext":
                    UpdateNext msgInUNext = gson.fromJson(json, UpdateNext.class);
                    node.setNextNodeID(msgInUNext.getNext());
                    break;
                case "UpdatePrevious":
                    UpdatePrevious msgUPrev = gson.fromJson(json, UpdatePrevious.class);
                    node.setPreviousNodeID(msgUPrev.getPrevious());
                    break;
                case "PreviousPrevious":
                    this.responses.add(message);
                    break;
                case "RequestPreviousPrevious":
                    response = new PreviousPrevious(node.getNodeID(), node.getPreviousNodeID());
                    break;
                case "DeleteFile":
                    DeleteFile msgDelFile = gson.fromJson(json, DeleteFile.class);
                    node.getReplicaManager().removeFile(msgDelFile.getFilename(), msgDelFile.getIsForced());
                    break;
                case "Shutdown":
                    node.shutdown();
                    System.exit(-1);
                    break;
                case "AgentWrapper":
                    SyncAgent agent = new SyncAgent(json);
                    agent.setParent(node);
                    new Thread(agent).start();
                    break;
            }

            System.out.println("[UDP HANDLER] Own ID = " + node.getNodeID() + " Next = " + node.getNextNodeID() + " Prev. = " + node.getPreviousNodeID());

            // Send response message
            if (response != null) {
                System.out.println("[UDP HANDLER] Response = " + gson.toJson(response) + " to " + senderAddr.toString());
                byte[] buffer = gson.toJson(response).getBytes(StandardCharsets.UTF_8);
                this.socket.send(new DatagramPacket(buffer, buffer.length, senderAddr, senderPort));
            }
        } catch (Exception e) {
            System.err.println("[UDP HANDLER] " + e);
            e.printStackTrace();
        }
    }
}
