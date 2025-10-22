package Presentation.Views;

import Services.MessageService;
import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;

public class MainView extends JFrame {
    private JTabbedPane MainTabbedPanel;
    private JPanel MainPanel;
    private JTextArea MessageTextArea;
    private MessageService messageService;

    public MainView() {
        setTitle("Sistema con Tabs");
        setContentPane(MainPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    /**
     * Connect to the message service and start listening for broadcasts
     */
    public void connectToMessages(String host, int port) {
        messageService = new MessageService(host, port, message -> {
            // This callback is called whenever a message is received
            SwingUtilities.invokeLater(() -> {
                appendMessageToTextArea(message);
            });
        });

        try {
            messageService.connect();
            System.out.println("[MainView] Connected to message service");
        } catch (Exception e) {
            System.err.println("[MainView] Failed to connect to message service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Append a message to the text area with timestamp
     */
    private void appendMessageToTextArea(String messageJson) {
        try {
            // Parse the message (it comes as a plain string from the server)
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            String formattedMessage = "[" + timestamp + "] " + messageJson + "\n";

            MessageTextArea.append(formattedMessage);
            MessageTextArea.setCaretPosition(MessageTextArea.getDocument().getLength());

            System.out.println("[MainView] Message displayed: " + messageJson);
        } catch (Exception e) {
            System.err.println("[MainView] Error displaying message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Agrega pestañas al tab panel.
     * @param tabs Diccionario con título y contenido del tab.
     */
    public void AddTabs(Dictionary<String, JPanel> tabs) {
        Enumeration<String> keys = tabs.keys();
        while (keys.hasMoreElements()) {
            String titulo = keys.nextElement();
            JPanel contenido = tabs.get(titulo);
            MainTabbedPanel.addTab(titulo, contenido);
        }
    }

    public void closeMessageService() {
        if (messageService != null) {
            messageService.close();
        }
    }
}