package nameserver;

import messages.FileInfo;

import java.util.Map;

public class FileLookup {
    private final CustomHashMap nodeMap;

    public FileLookup(CustomHashMap nodeMap){
        // Map with the worker node IDs and IP pairs
        this.nodeMap = nodeMap;
    }

    public FileInfo getFileInfo(String fileName) {

        if (nodeMap.size() > 0) {
            // Get the hash ID of the file to find the node
            int fileID = NameServer.hashingFunction(fileName);

            // Get the ip of the node.
            Map.Entry<Integer, String> entry = (nodeMap.lowerEntry(fileID) != null) ? nodeMap.lowerEntry(fileID) : nodeMap.lastEntry();

            // Create the wanted fileInfo.
            return new FileInfo(entry.getKey(), fileID, entry.getValue());
        } else {
            return null;
        }
    }
}
