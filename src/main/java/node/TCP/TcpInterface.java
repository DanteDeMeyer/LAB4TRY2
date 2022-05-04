package node.TCP;

import node.FileLog;
import node.Node;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpInterface implements Runnable {

    private final int port;
    private final Node node;
    private final ServerSocket serverSocket;
    private final Thread pingThread;

    public TcpInterface(Node node, int port) throws IOException {
        serverSocket = new ServerSocket(port);
        this.port = port;
        this.node = node;
        this.pingThread = new Thread(new FailureAgent(serverSocket.getInetAddress(), node));
    }

    @Override
    public void run() {
        System.out.println("[NODE TCP INT] Listening for TCP requests to port " + this.port);

        try {
            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println("[NODE TCP INT] Connection started from " + clientSocket.getInetAddress());

                Thread receiver = new Thread(new FileReceiver(clientSocket, this.node));
                receiver.setPriority(Thread.NORM_PRIORITY - 1);
                receiver.start();

            }
        } catch (Exception e) {
            System.err.println("[NODE TCP INT] " + e);
            e.printStackTrace();
        }

    }

    public void sendFile(InetAddress ip, String path, FileLog log) throws IOException {

        Socket socket = new Socket(ip, port);

        // Send the filelog first
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(log);

        // Now send the file
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        int bytes;
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);

        // Send file size
        dataOutputStream.writeLong(file.length());
        // Break file into chunks
        byte[] buffer = new byte[4*1024];
        while ((bytes=fileInputStream.read(buffer))!=-1){
            dataOutputStream.write(buffer,0, bytes);
            dataOutputStream.flush();
        }
        fileInputStream.close();
    }
}
