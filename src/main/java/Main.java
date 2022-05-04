import nameserver.NameServer;
import nameserver.NameserverGUI;
import node.Node;

public class Main {
    // arguments have to be given in main
    public static void main(String[] args) {
        if (args[0].equalsIgnoreCase("nameserver")) {
            if (!(args.length < 3)) {
                new NameServer(Integer.parseInt(args[1]), args[2]);
            }
        } else if (args[0].equalsIgnoreCase("node")) {
            if (!(args.length < 4)) {
                new Node(Integer.parseInt(args[1]), args[2], args[3], System.getProperty("user.dir"));
            }
        } else if (args[0].equalsIgnoreCase("nsgui")) {
            NameserverGUI.main(args);
        }
    }
}
