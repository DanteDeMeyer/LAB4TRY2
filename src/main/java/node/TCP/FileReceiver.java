package node.TCP;

import node.FileLog;
import node.Node;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class FileReceiver implements Runnable {

    private final Socket clientSocket;
    private ObjectInputStream objectInputStream;
    private DataInputStream dataInputStream;
    private Node node;

    public FileReceiver(Socket clientSocket, Node node) {
        this.clientSocket = clientSocket;
        this.node = node;
    }

    @Override
    public void run() {
        try {
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            receiveFile();
            dataInputStream.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveFile() throws Exception{

        FileLog log = (FileLog) objectInputStream.readObject();
        String filepath = node.getReplicaManager().addFile(log);

        int bytes;
        FileOutputStream fileOutputStream = new FileOutputStream(filepath);

        long size = this.dataInputStream.readLong();     // read file size
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = this.dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            size -= bytes;      // read upto file size
        }
        fileOutputStream.close();
    }
}
