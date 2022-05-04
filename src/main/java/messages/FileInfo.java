package messages;

public class FileInfo {
    private int nodeID;
    private int fileID;
    private String ip;

    public FileInfo(int noteId,int fileID, String ip){
        this.nodeID = noteId;
        this.fileID = fileID;
        this.ip = ip;
    }

    public int getNodeID() {

        return this.nodeID;
    }

    public int getFileID() {
        return fileID;
    }

    public String getIP() {
        return ip;
    }
}
