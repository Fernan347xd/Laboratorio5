package Presentation.Controllers;

import Domain.Dtos.auth.UserResponseDto;
import Presentation.Observable;
import Presentation.Views.CarsView;
import Presentation.Views.LoginView;
import Presentation.Views.MainView;
import Services.AuthService;
import Services.CarService;
import Services.MaintenanceService;
import Utilities.EventType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Dictionary;
import java.util.Hashtable;

public class LoginController extends Observable {

    private final LoginView loginView;
    private final AuthService authService;

    public LoginController(LoginView loginView, AuthService authService) {
        this.loginView = loginView;
        this.authService = authService;
        this.loginView.addLoginListener(e -> handleLogin());

        this.loginView.addAddUserListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                if (cmd == null || cmd.isEmpty()) return;
                String[] parts = cmd.split("\\|", 3);
                if (parts.length < 3) return;

                String newUser = parts[0];
                String newEmail = parts[1];
                String newPass = parts[2];

                // registrar asíncrono
                loginView.showLoading(true);
                SwingWorker<UserResponseDto, Void> regWorker = new SwingWorker<>() {
                    @Override
                    protected UserResponseDto doInBackground() throws Exception {
                        return authService.register(newUser, newEmail, newPass).get();
                    }

                    @Override
                    protected void done() {
                        loginView.showLoading(false);
                        try {
                            UserResponseDto created = get();
                            if (created != null) {
                                JOptionPane.showMessageDialog(loginView, "User created: " + created.getUsername());
                                loginView.setUsernameField(created.getUsername());
                            } else {
                                JOptionPane.showMessageDialog(loginView, "User not created (maybe already exists)", "Info", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(loginView, "Error creating user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                regWorker.execute();
            }
        });
    }

    private void handleLogin() {
        String username = loginView.getUsername();
        String password = loginView.getPassword();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(loginView, "Username or password cannot be empty", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        loginView.showLoading(true);

        SwingWorker<UserResponseDto, Void> worker = new SwingWorker<>() {
            @Override
            protected UserResponseDto doInBackground() throws Exception {
                return authService.login(username, password).get();
            }

            @Override
            protected void done() {
                loginView.showLoading(false);
                try {
                    UserResponseDto user = get();
                    if (user != null) {
                        loginView.setVisible(false);
                        openMainView(user);
                        notifyObservers(EventType.UPDATED, user);
                    } else {
                        JOptionPane.showMessageDialog(loginView, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(loginView, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void openMainView(UserResponseDto user) {
        MainView mainView = new MainView();

        String host = "localhost";
        int serverPort = 7000;
        int messagesPort = 7001;

        CarsView carsView = new CarsView(mainView);

        Long userId = user != null ? user.getId() : null;
        CarService carService = new CarService(host, serverPort, userId);
        MaintenanceService maintenanceService = new MaintenanceService(host, serverPort, userId);

        new CarsController(carsView, carService, maintenanceService, user);

        Dictionary<String, JPanel> tabs = new Hashtable<>();
        tabs.put("Cars", carsView.getContentPanel());

        mainView.connectToMessages(host, messagesPort);
        mainView.AddTabs(tabs);
        mainView.setVisible(true);
    }
}
