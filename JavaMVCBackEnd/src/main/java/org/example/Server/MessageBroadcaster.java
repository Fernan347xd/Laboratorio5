package org.example.Server;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageBroadcaster {

    private final int port;
    private ServerSocket serverSocket;
    private final List<BroadcastClient> connectedClients = new CopyOnWriteArrayList<>();
    private final Gson gson = new Gson();

    public MessageBroadcaster(int port, SocketServer requestServer) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("[MessageBroadcaster] Started on port " + port);

            // Accept message connections in a separate thread
            new Thread(this::acceptConnections, "MessageBroadcaster-Acceptor").start();

        } catch (IOException e) {
            System.err.println("[MessageBroadcaster] Failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void acceptConnections() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[MessageBroadcaster] New message client connected from " + clientSocket.getInetAddress());

                BroadcastClient client = new BroadcastClient(clientSocket);
                connectedClients.add(client);
                System.out.println("[MessageBroadcaster] Message clients connected: " + connectedClients.size());
            }
        } catch (IOException e) {
            System.err.println("[MessageBroadcaster] Error accepting connections: " + e.getMessage());
        }
    }

    public void broadcastToAll(Object message) {
        System.out.println("[MessageBroadcaster] Broadcasting to " + connectedClients.size() + " clients: " + message);

        for (BroadcastClient client : connectedClients) {
            client.sendMessage(message);
        }
    }

    public int getConnectedClientCount() {
        return connectedClients.size();
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("[MessageBroadcaster] Server stopped.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Close all connected clients
        for (BroadcastClient client : connectedClients) {
            client.close();
        }
    }

    /**
     * Represents a client connected on the message port
     */
    private class BroadcastClient {
        private final Socket socket;
        private final PrintWriter out;

        public BroadcastClient(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }

        public void sendMessage(Object message) {
            if (out != null && !out.checkError()) {
                String jsonMessage = gson.toJson(message);
                out.println(jsonMessage);
            }
        }

        public void close() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}