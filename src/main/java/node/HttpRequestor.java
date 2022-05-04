package node;

import com.google.gson.Gson;
import messages.FileInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpRequestor {

    private final HttpClient client = HttpClient.newHttpClient();
    private HttpRequest request;

    public InetAddress getIpFromNodeID(InetAddress ip, int port, int nodeID) throws IOException, InterruptedException {
        request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip.getHostAddress() + ":" + port + "/nodes/" + nodeID))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[NODE HTTP] Node ID = " + nodeID + " resolved to " + response.body());
        return InetAddress.getByName(response.body());
    }

    public FileInfo getReplicatedNodeID(InetAddress ip, int port, String filename) throws IOException, InterruptedException {
        request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip.getHostAddress() + ":" + port + "/files/" + URLEncoder.encode(filename, "UTF-8")))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return (new Gson()).fromJson(response.body(), FileInfo.class);
    }
}
