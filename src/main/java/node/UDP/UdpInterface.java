package node.UDP;

import com.google.gson.Gson;
import messages.Message;
import node.Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

public class UdpInterface extends common.UdpInterface implements Runnable {
    private final Node node;
    private ArrayList<Message> responses;

    public UdpInterface(Node node, InetAddress multicastGroup, int port) throws IOException {
        super(multicastGroup, port);
        this.node = node;
        this.responses = new ArrayList<>();
    }

    @Override
    public void run() {
        System.out.println("[NODE UDP INT] Listening for UDP requests at  " + this.socket.getLocalSocketAddress());
        // socket reeds gedefinieerd in superklasse

        try {
            this.socket.joinGroup(multicastGroup);
            while (true) {
                byte[] buffer = new byte[2048];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                System.out.println("[NODE UDP INT] Wait for request...");
                this.socket.receive(request); // blocks until message received
                System.out.println("[NODE UDP INT] Received message from " + request.getSocketAddress());
                Thread handler = new Thread(new RequestHandler(this.node, this.socket, request, responses));
                handler.setPriority(Thread.NORM_PRIORITY - 1); // hmm waarom
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("[NODE UDP INT] " + e);
        }
    }

    public void multicast(Message msg) throws Exception {
        String json = new Gson().toJson(msg);
        byte[] buffer = json.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, this.multicastGroup, this.socket.getLocalPort());
        socket.send(sendPacket);

        //System.out.println("[NODE UDP INT] Sent " + json + " to " + sendPacket.getSocketAddress());
    }

    // geef welke message wilt, geeft terug als er is, handig voor crash?
    public Message getResponse(String type, int senderID) {
        Iterator<Message> it = responses.iterator();
        while (it.hasNext()) {
            Message msg = it.next();
            if (msg.getType().equals(type) && msg.getSender() == senderID) {
                it.remove();
                return msg;
            }
        }

        return null;
    }
}
