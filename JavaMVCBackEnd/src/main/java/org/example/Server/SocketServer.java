package org.example.Server;

import org.example.API.controllers.AuthController;
import org.example.API.controllers.CarController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SocketServer {

    private final int port;
    private final AuthController authController;
    private final CarController carController;
    private ServerSocket serverSocket;
    private final List<ClientHandler> activeClients = new CopyOnWriteArrayList<>();
    private MessageBroadcaster messageBroadcaster;

    public SocketServer(int port, AuthController authController, CarController carController) {
        this.port = port;
        this.authController = authController;
        this.carController = carController;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("[SocketServer] Started on port " + port);

            // Move accept loop to separate thread so main thread isn't blocked
            new Thread(this::acceptConnections, "SocketServer-Acceptor").start();

        } catch (IOException e) {
            System.err.println("[SocketServer] Failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void acceptConnections() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SocketServer] New client connected from " + clientSocket.getInetAddress());

                // Track this client
                ClientHandler handler = new ClientHandler(clientSocket, authController, carController, this);
                activeClients.add(handler);

                // Give it a descriptive thread name
                Thread clientThread = new Thread(handler, "ClientHandler-" + activeClients.size());
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("[SocketServer] Error in accept loop: " + e.getMessage());
        }
    }

    public void removeClient(ClientHandler handler) {
        activeClients.remove(handler);
        System.out.println("[SocketServer] Client removed. Active clients: " + activeClients.size());
    }

    public void broadcast(Object message) {
        System.out.println("[SocketServer] Broadcasting to " + activeClients.size() + " clients: " + message);

        // Broadcast to all connected message clients
        if (messageBroadcaster != null) {
            messageBroadcaster.broadcastToAll(message);
        }
    }

    public void setMessageBroadcaster(MessageBroadcaster broadcaster) {
        this.messageBroadcaster = broadcaster;
        System.out.println("[SocketServer] MessageBroadcaster registered");
    }

    public int getActiveClientCount() {
        return activeClients.size();
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("[SocketServer] Server stopped.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}