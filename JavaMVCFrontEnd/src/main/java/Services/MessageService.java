package Services;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Service for receiving broadcast messages from the server (port 7001).
 * This connection stays open and listens for incoming messages.
 */
public class MessageService {
    private final String host;
    private final int port;
    private final Gson gson = new Gson();
    private Socket socket;
    private BufferedReader reader;
    private Thread listenerThread;
    private boolean running = false;
    private final Consumer<String> onMessageReceived;

    public MessageService(String host, int port, Consumer<String> onMessageReceived) {
        this.host = host;
        this.port = port;
        this.onMessageReceived = onMessageReceived;
    }

    public void connect() throws IOException {
        System.out.println("[MessageService] Connecting to " + host + ":" + port);
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        running = true;

        listenerThread = new Thread(this::listenForMessages, "MessageService-Listener");
        listenerThread.start();
        System.out.println("[MessageService] Connected and listening for messages");
    }

    private void listenForMessages() {
        try {
            String message;
            while (running && (message = reader.readLine()) != null) {
                System.out.println("[MessageService] Received: " + message);
                onMessageReceived.accept(message);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("[MessageService] Connection lost: " + e.getMessage());
            }
        } finally {
            close();
        }
    }

    public void close() {
        running = false;
        try {
            if (reader != null) reader.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("[MessageService] Closed");
    }
}