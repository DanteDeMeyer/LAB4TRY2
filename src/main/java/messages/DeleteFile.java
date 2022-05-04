package messages;

public class DeleteFile extends Message {
    private String filename;
    private boolean isForced;

    public DeleteFile(int sender, String filename, boolean isForced) {
        super("DeleteFile", sender, false);
        this.filename = filename;
        this.isForced = isForced;
    }

    public String getFilename() {
        return filename;
    }

    public boolean getIsForced() { return isForced; }
}
