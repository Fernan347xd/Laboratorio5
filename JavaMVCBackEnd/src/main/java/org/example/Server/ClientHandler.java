package org.example.Server;

import com.google.gson.Gson;
import org.example.API.controllers.AuthController;
import org.example.API.controllers.CarController;
import org.example.API.controllers.MaintenanceController;
import org.example.Domain.dtos.RequestDto;
import org.example.Domain.dtos.ResponseDto;
import org.example.Domain.dtos.auth.UserResponseDto;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final AuthController authController;
    private final CarController carController;
    private final MaintenanceController maintenanceController;
    private final SocketServer server;
    private final Gson gson = new Gson();
    private PrintWriter out;

    public ClientHandler(Socket clientSocket,
                         AuthController authController,
                         CarController carController,
                         MaintenanceController maintenanceController,
                         SocketServer server) {
        this.clientSocket = clientSocket;
        this.authController = authController;
        this.carController = carController;
        this.maintenanceController = maintenanceController;
        this.server = server;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            System.out.println("[ClientHandler] Connected: " + Thread.currentThread().getName());

            String inputJson;
            while ((inputJson = in.readLine()) != null) {
                System.out.println("[ClientHandler] " + Thread.currentThread().getName() + " received: " + inputJson);

                RequestDto request = gson.fromJson(inputJson, RequestDto.class);
                ResponseDto response = handleRequest(request);

                // Simulate processing
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }

                out.println(gson.toJson(response));
            }
        } catch (IOException e) {
            System.err.println("[ClientHandler] " + Thread.currentThread().getName() + " disconnected");
        } finally {
            try { clientSocket.close(); } catch (IOException ignore) {}
            server.removeClient(this);
        }
    }

    private ResponseDto handleRequest(RequestDto request) {
        ResponseDto response;

        switch (request.getController()) {
            case "Auth":
                response = authController.route(request);

                // If login successful, broadcast notification
                if ("login".equals(request.getRequest()) && response.isSuccess()) {
                    UserResponseDto user = gson.fromJson(response.getData(), UserResponseDto.class);
                    String notification = "User " + user.getUsername() + " just logged in!";
                    System.out.println("[ClientHandler] Login detected, broadcasting: " + notification);
                    server.broadcast(notification);
                }
                break;

            case "Cars":
                response = carController.route(request);
                break;

            case "Maintenance":
                response = maintenanceController.route(request);
                break;

            default:
                response = new ResponseDto(false, "Unknown controller", null);
        }

        return response;
    }

    public void sendMessage(Object message) {
        if (out != null) {
            String jsonMessage = gson.toJson(message);
            out.println(jsonMessage);
            System.out.println("[ClientHandler] " + Thread.currentThread().getName() + " sent: " + jsonMessage);
        }
    }
}
