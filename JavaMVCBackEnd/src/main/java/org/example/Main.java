package org.example;

import com.google.gson.Gson;

import org.example.API.controllers.AuthController;
import org.example.API.controllers.CarController;
import org.example.API.controllers.MaintenanceController;

import org.example.DataAccess.HibernateUtil;
import org.example.DataAccess.services.AuthService;
import org.example.DataAccess.services.CarService;
import org.example.DataAccess.services.MaintenanceService;

import org.example.Domain.models.User;
import org.example.Domain.dtos.RequestDto;

import org.example.Server.AppServer;

public class Main {
    public static void main(String[] args) {

        var sessionFactory = HibernateUtil.getSessionFactory();

        AuthService authService = new AuthService(sessionFactory);
        CarService carService = new CarService(sessionFactory);
        MaintenanceService maintenanceService = new MaintenanceService(sessionFactory);

        AuthController authController = new AuthController(authService);
        CarController carController = new CarController(carService);
        MaintenanceController maintenanceController = new MaintenanceController(maintenanceService, carService);

        var createUsers = true;
        if (createUsers) {
            try {
                User u1 = authService.register("user", "email@example.com", "pass", "USER");
                System.out.println(u1 == null
                        ? "User 'user' already exists, skipping creation."
                        : "Created default user: " + u1.getUsername());
            } catch (Exception ex) {
                System.out.println("User 'user' registration failed but application will continue: " + ex.getMessage());
                ex.printStackTrace();
            }

            try {
                User u2 = authService.register("otro", "otro@example.com", "pass", "USER");
                System.out.println(u2 == null
                        ? "User 'otro' already exists, skipping creation."
                        : "Created default user: " + u2.getUsername());
            } catch (Exception ex) {
                System.out.println("User 'otro' registration failed but application will continue: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        final int REQUEST_PORT = 7000;
        final int MESSAGE_PORT = 7001;
        AppServer.initialize(REQUEST_PORT, MESSAGE_PORT);

        final Gson gson = new Gson();

        AppServer.getRequestServer().addController(
                authController,
                (String requestJson) -> gson.fromJson(requestJson, RequestDto.class),
                (Object responseObj) -> gson.toJson(responseObj)
        );

        AppServer.getRequestServer().addController(
                carController,
                (String requestJson) -> gson.fromJson(requestJson, RequestDto.class),
                (Object responseObj) -> gson.toJson(responseObj)
        );

        AppServer.getRequestServer().addController(
                maintenanceController,
                (String requestJson) -> gson.fromJson(requestJson, RequestDto.class),
                (Object responseObj) -> gson.toJson(responseObj)
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down servers...");
            try { AppServer.getRequestServer().stop(); } catch (Exception ignore) {}
            try { AppServer.getMessageBroadcaster().stop(); } catch (Exception ignore) {}
        }));

        AppServer.getRequestServer().start();
        AppServer.getMessageBroadcaster().start();

        AppServer.getRequestServer().broadcast("Server started!");

        System.out.println("Servers started - Requests: " + REQUEST_PORT + ", Messages: " + MESSAGE_PORT);
    }
}
