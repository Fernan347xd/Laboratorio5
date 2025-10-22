package org.example;

import org.example.API.controllers.AuthController;
import org.example.API.controllers.CarController;
import org.example.DataAccess.services.AuthService;
import org.example.DataAccess.services.CarService;
import org.example.DataAccess.HibernateUtil;
import org.example.Server.SocketServer;
import org.example.Server.MessageBroadcaster;

public class Main {
    public static void main(String[] args) {
        var sessionFactory = HibernateUtil.getSessionFactory();

        // Initialize services and controllers
        AuthService authService = new AuthService(sessionFactory);
        AuthController authController = new AuthController(authService);

        CarService carService = new CarService(sessionFactory);
        CarController carController = new CarController(carService);

        var createUsers = true;
        if(createUsers) {
            authService.register("user", "email@example.com", "pass", "USER");
            authService.register("otro", "otro@example.com", "pass", "USER");

        }

        // Server for request/response (API-like)
        int requestPort = 7000;
        SocketServer requestServer = new SocketServer(
                requestPort,
                authController,
                carController);

        // Server for chat/broadcasting (persistent connections)
        int messagePort = 7001;
        MessageBroadcaster messageBroadcaster = new MessageBroadcaster(messagePort, requestServer);

        // Register the broadcaster with the request server so it can broadcast messages
        requestServer.setMessageBroadcaster(messageBroadcaster);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down servers...");
            requestServer.stop();
            messageBroadcaster.stop();
        }));

        // Start servers
        requestServer.start();
        messageBroadcaster.start();
        System.out.println("Servers started - Requests: " + requestPort + ", Messages: " + messagePort);
    }
}