package messages;

public class UpdateFile extends Message {
    private String filename;

    public UpdateFile(int sender, String filename) {
        super("UpdateFile", sender, false);
        this.filename = filename;
    }
}

