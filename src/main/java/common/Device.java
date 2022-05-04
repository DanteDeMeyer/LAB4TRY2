package common;

import common.UdpInterface;
import node.FileLog;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public interface Device {
    public UdpInterface getUdpInterface();

    public int getNodeID();
    public int getPreviousNodeID();
    public int getNextNodeID();

    public InetAddress getNameServerIpAddress();

    public int getPort();

    public Map<String, FileLog> getReplicas();
    public Map<String, FileLog> getFiles();
    public void setFiles(HashMap<String, FileLog> files);

}
