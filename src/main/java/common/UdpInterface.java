package common;

import messages.Message;

import com.google.gson.Gson;
import messages.Message;


import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public abstract class UdpInterface {
    protected final MulticastSocket socket;
    protected final InetAddress multicastGroup;

    public UdpInterface(InetAddress multicastGroup, int port) throws IOException {
        this.multicastGroup = multicastGroup;
        this.socket = new MulticastSocket(port);
    }

    public void unicast(Message msg, InetAddress dest) throws IOException {
        String json = new Gson().toJson(msg);
        byte[] buffer = json.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, dest, this.socket.getLocalPort());
        socket.send(sendPacket);

        //System.out.println("[NODE UDP INT] Sent " + json + " to " + sendPacket.getSocketAddress());
    }
}
