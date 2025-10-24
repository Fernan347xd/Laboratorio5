package org.example;

import org.example.API.controllers.AuthController;
import org.example.API.controllers.CarController;
import org.example.API.controllers.MaintenanceController;
import org.example.DataAccess.services.AuthService;
import org.example.DataAccess.services.CarService;
import org.example.DataAccess.services.MaintenanceService;
import org.example.DataAccess.HibernateUtil;
import org.example.Server.SocketServer;
import org.example.Server.MessageBroadcaster;
import org.example.Domain.models.User;

public class Main {
    public static void main(String[] args) {
        var sessionFactory = HibernateUtil.getSessionFactory();

        // Initialize services and controllers
        AuthService authService = new AuthService(sessionFactory);
        AuthController authController = new AuthController(authService);

        CarService carService = new CarService(sessionFactory);
        CarController carController = new CarController(carService);

        MaintenanceService maintenanceService = new MaintenanceService(sessionFactory);
        MaintenanceController maintenanceController = new MaintenanceController(maintenanceService, carService);

        var createUsers = true;
        if (createUsers) {
            // Intentar crear usuarios por defecto sin detener la aplicación si ya existen
            try {
                User u1 = authService.register("user", "email@example.com", "pass", "USER");
                if (u1 == null) {
                    System.out.println("User 'user' already exists, skipping creation.");
                } else {
                    System.out.println("Created default user: " + u1.getUsername());
                }
            } catch (Exception ex) {
                System.out.println("User 'user' registration failed but application will continue: " + ex.getMessage());
                ex.printStackTrace();
            }

            try {
                User u2 = authService.register("otro", "otro@example.com", "pass", "USER");
                if (u2 == null) {
                    System.out.println("User 'otro' already exists, skipping creation.");
                } else {
                    System.out.println("Created default user: " + u2.getUsername());
                }
            } catch (Exception ex) {
                System.out.println("User 'otro' registration failed but application will continue: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        // Server for request/response (API-like)
        int requestPort = 7000;
        SocketServer requestServer = new SocketServer(
                requestPort,
                authController,
                carController,
                maintenanceController);

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
