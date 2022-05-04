package node;

import common.Device;
import messages.Bye;
import messages.Failure;
import messages.Hello;
import messages.Message;
import node.TCP.TcpInterface;
import node.UDP.UdpInterface;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static java.lang.Math.abs;

public class Node implements common.Device {
    private final String name;
    private int nodeID;
    private int previousNodeID;
    private int nextNodeID;
    private ReplicaManager replicaManager;
    private FolderWatcher folderWatcher;

    private TcpInterface tcpInterface;

    private UdpInterface udpInterface;
    private final int port;
    private InetAddress nameServerIpAddress;
    public HttpRequestor httpRequestor;

    private HashMap<String, FileLog> files;

    public Node(int port, String name, String multicastIP, String filePath) {
        this.nodeID = hashingFunction(name);
        this.name = name;
        this.port = port;
        this.httpRequestor = new HttpRequestor();
        this.files = new HashMap<>();

        try {
            // Initialize UDP listener.
            InetAddress multicastAddress = InetAddress.getByName(multicastIP); // create BC address
            this.udpInterface = new UdpInterface(this, multicastAddress, port); // hmm
            new Thread(this.udpInterface).start();

            // Initialize TCP listener
            this.tcpInterface = new TcpInterface(this, port);
            new Thread(this.tcpInterface).start();

            //Initialize FolderWatcher
            // folderWatcher = new FolderWatcher(this, filePath + "/local/");

            // Initialize ReplicaManager
            this.replicaManager = new ReplicaManager(this, filePath + "/replicated/");

            // Perform bootstrap procedure.
            this.previousNodeID = -1;
            this.nextNodeID = -1;
            bootstrap();
            Thread.sleep(5000);
            if (this.previousNodeID < 0 || this.nextNodeID < 0) System.exit(-1);

            // Replicate files
            new Thread(folderWatcher).start();

        } catch (Exception e) {
            System.out.println("[NODE] " + e);
            e.printStackTrace();
        }

        // Add Ctrl-C shutdown handler.
        Runtime.getRuntime().addShutdownHook(new Thread() {

            /** This handler will be called on Control-C pressed */
            @Override
            public void run() {
                System.out.println("[NODE] Shutdown hook");
                shutdown();
            }
        });
    }

    public void bootstrap() {
        try {
            Message message = new Hello(this.nodeID);
            this.udpInterface.multicast(message);
        } catch (Exception e) {
            System.out.println("[NODE] " + e);
            e.printStackTrace();
        }
    }

    public void shutdown() {
        Message message = new Bye(this.nodeID, previousNodeID, nextNodeID);

        try {
            // Move replicas.
            replicaManager.shutdown();

            // Delete local files from replica nodes.
            //folderWatcher.shutdown();

            // Restructure ring topology.
            System.out.println("[NODE] Sending goodbye!");
            udpInterface.unicast(message, nameServerIpAddress);

            if (previousNodeID != nodeID) {
                InetAddress previousNodeIP = httpRequestor.getIpFromNodeID(nameServerIpAddress, port + 1, previousNodeID);
                InetAddress nextNodeIP = httpRequestor.getIpFromNodeID(nameServerIpAddress, port + 1, nextNodeID);
                udpInterface.unicast(message, previousNodeIP);
                udpInterface.unicast(message, nextNodeIP);
            }

            System.out.println("[NODE] Goodbye sent!");
        } catch (IOException | InterruptedException e) {
            System.out.println("[NODE] " + e);
            e.printStackTrace();
        }
    }

    public void failure(int failedNode) {
        System.out.println("[NODE] Failure detected on node: " + failedNode);

        Message message = new Failure(this.nodeID, failedNode);
        try {
            udpInterface.unicast(message, nameServerIpAddress);
        } catch (IOException e) {
            System.out.println("[NODE] " + e);
            e.printStackTrace();
        }
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public void setPreviousNodeID(int previousNodeID) {
        this.previousNodeID = previousNodeID;
    }

    public void setNextNodeID(int nextNodeID) {
        this.nextNodeID = nextNodeID;
    }

    public int getNodeID() {
        return nodeID;
    }

    public int getPreviousNodeID() {
        return previousNodeID;
    }

    public int getNextNodeID() {
        return nextNodeID;
    }

    public void setNameServerIpAddress(InetAddress ip) {
        this.nameServerIpAddress = ip;
    }

    public InetAddress getNameServerIpAddress() {
        return this.nameServerIpAddress;
    }

    public int getPort() {
        return this.port;
    }

    public Map<String, FileLog> getReplicas() {
        return this.replicaManager.getFiles();
    }

    @Override
    public Map<String, FileLog> getFiles() {
        return this.replicaManager.getFiles();
    }

    @Override
    public void setFiles(HashMap<String, FileLog> files) {
        this.files = new HashMap<>(files);
    }

    public TcpInterface getTcpInterface() {
        return tcpInterface;
    }

    public static int hashingFunction(String name) {
        return abs(name.hashCode()) % 32768;
    }

    public UdpInterface getUdpInterface() {
        return udpInterface;
    }

    public ReplicaManager getReplicaManager() {
        return replicaManager;
    }
}
