package Presentation.Views;

import Domain.Dtos.auth.UserResponseDto;
import Presentation.IObserver;
import Utilities.EventType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class LoginView extends JFrame implements IObserver {
    private JPanel MainPanel;
    private JPanel UsernamePanel;
    private JPanel PasswordPanel;
    private JPanel ButtonsPanel;
    private JButton LoginButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton AddUser;
    private final LoadingOverlay loadingOverlay;

    // lista propia de listeners para creación de usuario
    private final List<ActionListener> addUserListeners = new ArrayList<>();

    public LoginView() {
        setTitle("Login");
        setContentPane(MainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 175);
        setLocationRelativeTo(null);

        loadingOverlay = new LoadingOverlay(this);

        AddUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                JTextField userField = new JTextField(20);
                JTextField emailField = new JTextField(20);
                JPasswordField passField = new JPasswordField(20);

                panel.add(new JLabel("Username:"));
                panel.add(userField);
                panel.add(Box.createVerticalStrut(6));
                panel.add(new JLabel("Email:"));
                panel.add(emailField);
                panel.add(Box.createVerticalStrut(6));
                panel.add(new JLabel("Password:"));
                panel.add(passField);

                int result = JOptionPane.showConfirmDialog(
                        LoginView.this,
                        panel,
                        "Create User",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                if (result != JOptionPane.OK_OPTION) return;

                String u = userField.getText().trim();
                String em = emailField.getText().trim();
                String p = new String(passField.getPassword());

                if (u.isEmpty() || em.isEmpty() || p.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginView.this, "All fields are required", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // construir action command y notificar listeners registrados por el controller
                String cmd = u + "|" + em + "|" + p;
                ActionEvent evt = new ActionEvent(AddUser, ActionEvent.ACTION_PERFORMED, cmd);
                for (ActionListener listener : new ArrayList<>(addUserListeners)) {
                    try {
                        listener.actionPerformed(evt);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    // Exponer listener para el botón Login
    public void addLoginListener(ActionListener listener) {
        LoginButton.addActionListener(listener);
    }

    // Nuevo: exponer listener para el flujo "AddUser"
    public void addAddUserListener(ActionListener listener) {
        addUserListeners.add(listener);
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

    // helpers para permitir al controller autopoblar el campo username si lo desea
    public void setUsernameField(String username) {
        usernameField.setText(username);
    }
}
