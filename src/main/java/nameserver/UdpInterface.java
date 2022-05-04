package nameserver;

import com.google.gson.Gson;
import messages.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class UdpInterface extends common.UdpInterface implements Runnable {
    private final NameServer nameServer;


    public UdpInterface(NameServer nameServer, InetAddress multicastGroup, int port) throws IOException {
        super(multicastGroup, port);
        this.nameServer = nameServer;
    }

    @Override
    public void run() {
        System.out.println("[NS UDP INT] Listening for UDP requests at " + this.socket.getLocalSocketAddress());

        try {
            this.socket.joinGroup(multicastGroup);
            while (true) {
                byte[] buffer = new byte[2048];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(request);
                //System.out.println("[NS UDP INT] Received message from " + request.getSocketAddress());
                Thread handler = new Thread(new RequestHandler(this.nameServer, this.socket, request));
                handler.setPriority(Thread.NORM_PRIORITY - 1);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("[NS UDP INT] " + e);
        }
    }
}
