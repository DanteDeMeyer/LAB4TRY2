package node;

import messages.DeleteFile;
import messages.FileInfo;
import messages.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.*;
import java.util.HashMap;

public class FolderWatcher implements Runnable {
    private final Node node;
    private final String folderPath;
    private final HashMap<String, Integer> replicaLocations;

    public FolderWatcher(Node node, String localPath) {
        this.node = node;
        this.folderPath = localPath;
        this.replicaLocations = new HashMap<>();
    }

    @Override
    public void run() {
        try {

            WatchService watchService = FileSystems.getDefault().newWatchService();

            //scan all local files and send them to their location
            startUp();

            //register our watchService to our object and choose which triggers to look out for
            Paths.get(folderPath).register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            WatchKey watchKey;
            while ((watchKey = watchService.take()) != null) {
                //loop over all events
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    System.out.println("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
                    WatchEvent.Kind<?> kind = event.kind();

                    if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
                        // New file created
                        update(event.context());
                    } else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
                        // Existing file modified
                        update(event.context());
                    } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
                        // Existing file deleted
                        delete(event.context());
                    }
                }
                //re add our key to the queue so that it is never empty
                watchKey.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Startup scan folder
    public void startUp() throws IOException, InterruptedException {
        // Iterator for entries in directory containing local files
        DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folderPath));
        for (Path entry : stream) {
            String filepath = entry.toAbsolutePath().toString();
            String filename = entry.getFileName().toString();

            // Get the destination node's ID ==> IP ==> send file to destination.
            FileInfo fileInfo = node.httpRequestor.getReplicatedNodeID(node.getNameServerIpAddress(), node.getPort() + 1, filename);

            int receiverID = (fileInfo.getNodeID() == node.getNodeID()) ? node.getPreviousNodeID() : fileInfo.getNodeID();

            InetAddress IP = node.httpRequestor.getIpFromNodeID(node.getNameServerIpAddress(), node.getPort() + 1, receiverID);
            FileLog fileLog = new FileLog(filename, fileInfo.getFileID(), node.getNodeID(), receiverID);
            node.getTcpInterface().sendFile(IP, filepath, fileLog);
            System.out.println("[FOLDERWATCHER] Created " + filepath + " at node " + receiverID);

            // Keep track of where the replicas are.
            replicaLocations.put(filename, receiverID);
        }
    }

    private void update(Object context) throws IOException, InterruptedException {
        String filepath = this.folderPath + context.toString();
        String filename = Paths.get(filepath).getFileName().toString();

        // File has been modified or created ==> get destination node ID ==> IP ==> send file.
        FileInfo fileInfo = node.httpRequestor.getReplicatedNodeID(node.getNameServerIpAddress(), node.getPort() + 1, filename);

        int receiverID = (fileInfo.getNodeID() == node.getNodeID()) ? node.getPreviousNodeID() : fileInfo.getNodeID();

        InetAddress IP = node.httpRequestor.getIpFromNodeID(node.getNameServerIpAddress(), node.getPort() + 1, receiverID);
        FileLog fileLog = new FileLog(filename, fileInfo.getFileID(), node.getNodeID(), receiverID);
        node.getTcpInterface().sendFile(IP, filepath, fileLog);
        System.out.println("[FOLDERWATCHER] Updated " + filepath + " at node " + receiverID);

        // Keep track of where the replicas are
        replicaLocations.put(filename, receiverID);
    }

    private void delete(Object context) throws IOException, InterruptedException {
        String filename = Paths.get(context.toString()).getFileName().toString();

        // File has been deleted locally ==> get replication node's ID ==> IP ==> send to delete file
        int nodeID = replicaLocations.get(filename);
        InetAddress IP = node.httpRequestor.getIpFromNodeID(node.getNameServerIpAddress(), node.getPort() + 1, nodeID);
        Message msg = new DeleteFile(node.getNodeID(), filename, true);
        node.getUdpInterface().unicast(msg, IP);
        System.out.println("[FOLDERWATCHER] Deleted " + filename + " from node " + nodeID);

        // Keep track of where the replicas are
        replicaLocations.remove(filename);
    }

    public void shutdown() throws IOException, InterruptedException {
        if (node.getPreviousNodeID() != node.getNodeID()) {
            // Go over all replicated files
            for (String filename : replicaLocations.keySet()) {
                // Get replication node's ID ==> IP ==> send to delete file
                FileInfo fileInfo = node.httpRequestor.getReplicatedNodeID(node.getNameServerIpAddress(), node.getPort() + 1, filename);
                int nodeID = (fileInfo.getNodeID() == node.getNodeID()) ? node.getPreviousNodeID() : fileInfo.getNodeID();
                InetAddress IP = node.httpRequestor.getIpFromNodeID(node.getNameServerIpAddress(), node.getPort() + 1, nodeID);
                Message msg = new DeleteFile(node.getNodeID(), filename, false);
                node.getUdpInterface().unicast(msg, IP);
                System.out.println("[FOLDERWATCHER] Deleted/updated " + filename + " from node " + nodeID);
            }
        }
    }
}
