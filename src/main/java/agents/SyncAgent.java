package agents;

import com.google.gson.Gson;
import common.UdpInterface;
import messages.AgentWrapper;
import messages.Message;
import nameserver.NameServer;
import node.FileLog;
import node.Node;
import common.Device;
import node.HttpRequestor;

import java.io.*;
import java.net.InetAddress;
import java.security.spec.NamedParameterSpec;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SyncAgent implements Runnable, Serializable  {
    int iteration, progress;
    private int resetID;
    private transient Device parent; // Transient keyword avoids serialization of this field.
    private HashMap<String, FileLog> files;
    int agentIdentifier;

    public SyncAgent(Device parent, int creatorID, int agentIdentifier) {
        this.parent = parent;
        this.resetID = creatorID;
        this.files = new HashMap<>();
    }

    public SyncAgent(String json) {
        AgentWrapper msg = new Gson().fromJson(json, AgentWrapper.class);
        SyncAgent agent = deserialize(msg.getAgent());
        if (agent != null) {
            this.parent = agent.parent;
            this.resetID = agent.resetID;
            this.files = agent.files;
            this.agentIdentifier = agent.agentIdentifier;
        }
    }

    public int getAgentIdentifier() {
        return agentIdentifier;
    }

    public void setParent(Device parent) {
        this.parent = parent;
    }

    private byte[] serializeThis() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.close();
            byte[] buffer = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return buffer;
        } catch (IOException e) {
            System.out.println("[SYNC] " + e);
            e.printStackTrace();
            return new byte[0];
        }
    }

    private SyncAgent deserialize(byte[] buffer) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            SyncAgent agent =  (SyncAgent) objectInputStream.readObject();
            byteArrayInputStream.close();
            objectInputStream.close();
            return agent;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[SYNC] " + e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {
        System.out.println("[SYNC] Execution started!");

        // Wait if the agent is on the nameserver and there are no nodes in the system.
        while (this.parent.getNextNodeID() == 0) {
            try {
                ((NameServer) this.parent).getSyncAgentWatcher().update(agentIdentifier);
                Thread.sleep(2000);
                System.out.println("[SYNC] Execution retry!");
            } catch (InterruptedException e) {
                System.out.println("[SYNC] " + e);
                e.printStackTrace();
            }
        }

        // Update the agent file list when on a node.
        if (this.parent.getNodeID() != 0 ) {
            // Get replica list and add new files to the list.
            this.parent.getReplicas().forEach((k, v) -> this.files.putIfAbsent(k, v));

            // Get full list and update file locks if permitted.
            this.parent.getFiles().forEach((k, v) -> {
                if (v.getLock() == this.parent.getNodeID()) {
                    this.files.put(k, v);
                }
            });
        }

        // Clear file list when a new round is started. Set the resetID to the previous node in the circle. Else update locks on local list only.
        if (this.parent.getNodeID() == this.resetID) {
            // Dump the current agent file list to the local file list.
            this.parent.setFiles(files);
            System.out.println("Local list: " + this.parent.getFiles());

            // Reset the agent.
            this.files.clear();
            this.resetID = (this.parent.getPreviousNodeID() >= this.parent.getNodeID() && this.parent.getNodeID() != 0) ? 0 : this.parent.getPreviousNodeID();

            // Initialize new list with replicas only and update the locks of the replicas.
            this.files.putAll(this.parent.getFiles());
        }

        this.files.forEach((k, v) -> {
            if (this.parent.getFiles().containsKey(k)) {
                int lock = this.parent.getFiles().get(k).getLock();
                this.files.get(k).setLock(lock);
            }
        });


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println("[SYNC] " + e);
            e.printStackTrace();
        }

        // Transfer this class to the next node. Send it to the nameserver when this is the last node.
        try {
            InetAddress nextIP;
            if (this.parent.getNextNodeID() > this.parent.getNodeID()) {
                nextIP = new HttpRequestor().getIpFromNodeID(this.parent.getNameServerIpAddress(), this.parent.getPort() + 1, this.parent.getNextNodeID());
            } else {
                nextIP = this.parent.getNameServerIpAddress();
            }
            UdpInterface udp = this.parent.getUdpInterface();
            Message msg = new AgentWrapper(this.parent.getNodeID(), serializeThis());

            udp.unicast(msg, nextIP);
        } catch (Exception e) {
            System.out.println("[SYNC] " + e);
            e.printStackTrace();
        }
    }
}
