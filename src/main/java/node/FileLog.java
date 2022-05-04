package node;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public class FileLog implements Serializable {
    private String filename;
    private int fileID;
    private int localNodeID;
    private int ownerNodeID;
    private int downloads;
    private int lock;

    public FileLog(String filename, int fileID, int local, int owner) {
        this.filename = filename;
        this.fileID = fileID;
        this.localNodeID = local;
        this.ownerNodeID = owner;
        this.downloads = 0;
        this.lock = -1;
    }

    public String getFilename() {
        return filename;
    }

    public int getLocalNodeID() {
        return localNodeID;
    }

    public int getOwnerNodeID() {
        return ownerNodeID;
    }

    public int getDownloads() {
        return downloads;
    }

    public int getFileID() { return fileID; }

    public void incrementDownload() {
        downloads++;
    }

    public void setLocalNodeID(int localNodeID) {
        this.localNodeID = localNodeID;
    }

    public void setOwnerNodeID(int ownerNodeID) {
        this.ownerNodeID = ownerNodeID;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public int getLock() {
        return lock;
    }

    public void setLock(int lock) {
        this.lock = lock;
    }

    public boolean acquire(int nodeID) {
        if (lock == -1) {
            lock = nodeID;
            return true;
        } else {
            return false;
        }
    }

    public void release() {
        lock = -1;
    }

    @Override
    public String toString() {
        return "FileLog{" +
                "filename='" + filename + '\'' +
                ", fileID=" + fileID +
                ", localNodeID=" + localNodeID +
                ", ownerNodeID=" + ownerNodeID +
                ", downloads=" + downloads +
                ", lock=" + lock +
                '}';
    }
}
