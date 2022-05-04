package node;

import messages.Message;
import messages.PreviousPrevious;
import messages.RequestPreviousPrevious;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ReplicaManager {
    private String folder;
    private Node node;
    private Map<String, FileLog> replicas;

    public ReplicaManager(Node node, String folder) {
        this.folder = folder;
        this.node = node;
        replicas = new HashMap<>();
    }

    public String getFilepath(String filename) {
        return this.folder + filename;
    }

    public String addFile(FileLog log) {
        log.setOwnerNodeID(node.getNodeID());
        replicas.put(log.getFilename(), log);
        System.out.println("[REPLICA MANAGER] Received " + log.getFilename());
        return getFilepath(log.getFilename());
    }

    public FileLog getFile(String filename) {
        return replicas.getOrDefault(filename, null);
    }
    public Map<String, FileLog> getFiles() { return this.replicas; }

    /**
     * Remove a replica from this node.
     * @param filename The filename of the file that needs to be removed.
     */
    public void removeFile(String filename, boolean isForced) {
        FileLog log = replicas.get(filename);
        System.out.println("[REPLICA MANAGER] Owner of " + log.getFilename() + "down. This file has " + log.getDownloads() + " downloads.");

        if (log.getDownloads() == 0 || isForced) {
            File file = new File(getFilepath(filename));
            if (file.delete()) {
                replicas.remove(filename);
            }
            System.out.println("[REPLICA MANAGER] Deleted " + log.getFilename());
        } else {
            log.setLocalNodeID(0);
            replicas.put(filename, log);
            System.out.println("[REPLICA MANAGER] Removed download location of " + log.getFilename());
        }
    }

    /**
     * Move files to other node when it is inserted after this node.
     * @param newNodeID the ID of the new node.
     */
    public void handleNewNodeAfter(int newNodeID) {
        HttpRequestor httpRequestor = new HttpRequestor();

        try {
            InetAddress newNodeIP = httpRequestor.getIpFromNodeID(node.getNameServerIpAddress(), node.getPort() + 1, newNodeID);

            Iterator<FileLog> it = replicas.values().iterator();
            while (it.hasNext()) {
                FileLog replica = it.next();
                if (replica.getFileID() > newNodeID || node.getPreviousNodeID() == node.getNextNodeID()) {
                    String filePath = getFilepath(replica.getFilename());
                    node.getTcpInterface().sendFile(newNodeIP, filePath, replica);
                    File file = new File(filePath);
                    System.out.println("[REPLICA MANAGER] Moved " + replica.getFilename() + " to node " + newNodeID);
                    if (file.delete()) {
                        it.remove();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Move all replicas to another node when this node shuts down.
     */
    public void shutdown() {
        try {

            if (node.getPreviousNodeID() != node.getNodeID()) {
                HttpRequestor httpRequestor = new HttpRequestor();

                Iterator<FileLog> it = replicas.values().iterator();
                while (it.hasNext()) {
                    FileLog replica = it.next();
                    String filePath = getFilepath(replica.getFilename());

                    if (replica.getLocalNodeID() == node.getPreviousNodeID()) { // Edge case
                        // Get ID of previous previous node.
                        InetAddress previousNodeIP = httpRequestor.getIpFromNodeID(node.getNameServerIpAddress(), node.getPort() + 1, node.getPreviousNodeID());
                        RequestPreviousPrevious req = new RequestPreviousPrevious(node.getNodeID());
                        node.getUdpInterface().unicast(req, previousNodeIP);

                        // Get response
                        Message res;
                        while ((res = node.getUdpInterface().getResponse("PreviousPrevious", node.getPreviousNodeID())) == null) { Thread.sleep(2);}
                        int prevPrevID = ((PreviousPrevious) res).getPrevious();

                        // Send file.
                        InetAddress prevPrevIP = httpRequestor.getIpFromNodeID(node.getNameServerIpAddress(), node.getPort() + 1, prevPrevID);
                        node.getTcpInterface().sendFile(prevPrevIP, filePath, replica);

                        // Remove file.
                        File file = new File(filePath);
                        if (file.delete()) {
                            it.remove();
                        }

                        System.out.println("[REPLICA MANAGER] Moved " + replica.getFilename() + "to node " + prevPrevID);
                    } else {
                        // Send file.
                        InetAddress prevIP = httpRequestor.getIpFromNodeID(node.getNameServerIpAddress(), node.getPort() + 1, node.getPreviousNodeID());
                        node.getTcpInterface().sendFile(prevIP, filePath, replica);

                        // Remove file.
                        File file = new File(filePath);
                        if (file.delete()) {
                            it.remove();
                        }

                        System.out.println("[REPLICA MANAGER] Moved " + replica.getFilename() + "to node " + node.getPreviousNodeID());
                    }
                }
            } else {
                Iterator<FileLog> it = replicas.values().iterator();
                while (it.hasNext()) {
                    FileLog replica = it.next();
                    String filePath = getFilepath(replica.getFilename());
                    File file = new File(filePath);
                    if (file.delete()) {
                        it.remove();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
