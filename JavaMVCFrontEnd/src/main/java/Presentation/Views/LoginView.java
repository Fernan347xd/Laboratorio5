package Presentation.Views;

import Domain.Dtos.auth.UserResponseDto;
import Presentation.IObserver;
import Utilities.EventType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginView extends JFrame implements IObserver {
    private JPanel MainPanel;
    private JPanel UsernamePanel;
    private JPanel PasswordPanel;
    private JPanel ButtonsPanel;
    private JButton LoginButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private final LoadingOverlay loadingOverlay;

    public LoginView() {
        setTitle("Login");
        setContentPane(MainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 175);
        setLocationRelativeTo(null);

        loadingOverlay = new LoadingOverlay(this);
    }

    public void addLoginListener(ActionListener listener) {
        LoginButton.addActionListener(listener);
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    @Override
    public void update(EventType eventType, Object data) {
        switch (eventType) {
            case CREATED:
                UserResponseDto user = (UserResponseDto) data;
                JOptionPane.showMessageDialog(this, "Welcome " + user.getUsername());
                break;
            case UPDATED:
                break;
            case DELETED:
                JOptionPane.showMessageDialog(this, data.toString(), "Login Info", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    /**
     * Shows or hides the loading overlay.
     */
    public void showLoading(boolean visible) {
        loadingOverlay.show(visible);
    }
}
