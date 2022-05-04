package nameserver;

import agents.SyncAgent;
import com.google.gson.Gson;
import messages.FileInfo;
import messages.Message;
import messages.Shutdown;
import node.FileLog;
import common.Device;
import org.eclipse.jetty.util.SocketAddressResolver;

import java.io.File;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.Math.abs;
import static spark.Spark.*;

// gebruikt ook device want is ook een apparaat in het systeem
public class NameServer implements Device {
    private final CustomHashMap nodes;
    private final FileLookup fileLookup;
    private UdpInterface udpInterface;
    private SyncAgentWatcher syncAgentWatcher;
    private int port;
    private HashMap<String, FileLog> files;


    public NameServer(int port, String multicastIP) {
        this.nodes = new CustomHashMap();
        this.fileLookup = new FileLookup(nodes);
        this.port = port;
        this.files = new HashMap<>();

        // Import existing nodes from file.
        try {
            nodes.importMap();
        } catch (Exception e) {
            System.err.println(e);
        }

        // REST

        initRest(port + 1);

        // UDP
        try {
            InetAddress multicastAddress = InetAddress.getByName(multicastIP);
            this.udpInterface = new UdpInterface(this, multicastAddress, port);
            new Thread(this.udpInterface).start();
        } catch (Exception e) {
            System.err.println("[NAMESERVER] " + e);
        }

        // Start sync agent watcher.
        // syncAgentWatcher = new SyncAgentWatcher(this);
        // (new Thread(syncAgentWatcher)).start();
    }

    private void initRest(int port) {
        port(port);

        /* Create node */
        post("/nodes", (req, res) -> {
            // Parse body
            String nodename = req.queryParams("name");
            String ip = req.queryParams("ip");

            if (nodename.isEmpty() || ip.isEmpty()) {
                res.status(400);
                return "Invalid request!\n";
            }

            // Add node to map
            int nodeID = hashingFunction(nodename);
            if (nodes.putIfAbsent(nodeID, ip) == null) {
                nodes.exportMap();
                res.status(200);
                return "Node added with ID=" + nodeID + "!";
            } else {
                res.status(403);
                return "This name is not available!\n";
            }
        });

        /* Get Node IP */
        get("/nodes/:id", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            return nodes.get(id);
        });

        /* Get nodes list */
        get("/nodes", (req, res) -> {
            return new Gson().toJson(nodes);
        });

        /* Delete node */
        delete("/nodes/:name", (req, res) -> {
            // Parse name
            String nodename = req.params("name");

            // Delete node
            int nodeID = hashingFunction(nodename);
            if (nodes.remove(nodeID) == null) {
                res.status(404);
                return "Node does not exist!\n";
            } else {
                res.status(200);
                return "Node deleted!\n";
            }
        });

        /* Update node IP */
        put("/nodes/:name", (req, res) -> {
            // Parse name
            String nodename = req.params("name");

            // Parse IP
            String ip = req.queryParams("ip");
            if (ip.isEmpty()) {
                res.status(400);
                return "Invalid request!\n";
            }

            // Update node
            int nodeID = hashingFunction(nodename);
            if (nodes.replace(nodeID, ip) == null) {
                res.status(404);
                return "Node does not exist!\n";
            } else {
                nodes.exportMap();
                res.status(200);
                return "Node updated!";
            }
        });

        /* Get file */
        get("/files/:filename", (req, res) -> {
            // Parse filename
            String filename = req.params("filename");

            // Get file info
            FileInfo fileInfo = fileLookup.getFileInfo(filename);

            if (fileInfo != null) {
                res.status(200);
                System.out.println("[REST] File info for " + filename + " = " + new Gson().toJson(fileInfo));
                return new Gson().toJson(fileInfo);
            } else {
                res.status(404);
                return "No nodes are known.\n";
            }
        });
    }

    public void shutdownNode(int id) {
        try {
            Message msg = new Shutdown(0);
            InetAddress ip = InetAddress.getByName(nodes.get(id));
            udpInterface.unicast(msg, ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addNode(int id, String ip) {
        nodes.put(id, ip);
    }
    public void removeNode(int id) {
        nodes.remove(id);
    }

    public int getCount() {
        return nodes.size();
    }
    public Set<Integer> getNodes() {
        return nodes.keySet();
    }
    public static int hashingFunction(String name){
        return abs(name.hashCode()) % 32768;
    }

    @Override
    public common.UdpInterface getUdpInterface() {
        return this.udpInterface;
    }

    @Override
    public int getNodeID() {
        return 0;
    }

    @Override
    public int getPreviousNodeID() {
        return nodes.isEmpty() ? 0 : nodes.lastKey();
    }

    @Override
    public int getNextNodeID() {
        return nodes.isEmpty() ? 0 : nodes.firstKey();
    }

    @Override
    public InetAddress getNameServerIpAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public Map<String, FileLog> getReplicas() {
        return new HashMap<String, FileLog>();
    }

    @Override
    public HashMap<String, FileLog> getFiles() {
        return this.files;
    }

    @Override
    public void setFiles(HashMap<String, FileLog> files) {
        this.files = new HashMap<>(files);
    }

    public SyncAgentWatcher getSyncAgentWatcher() {
        return this.syncAgentWatcher;
    }
}
