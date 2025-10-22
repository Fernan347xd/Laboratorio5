package Presentation.Views;

import javax.swing.*;
import java.awt.*;

public class LoadingOverlay {
    private final JPanel overlayPanel;
    private final JProgressBar progressBar;

    public LoadingOverlay(JFrame parentFrame) {
        overlayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                // Semi-transparent background (60 out of 255 alpha = ~75% transparent)
                g2d.setColor(new Color(0, 0, 0, 60));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };

        overlayPanel.setLayout(new GridBagLayout());
        overlayPanel.setOpaque(false);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(160, 30));
        progressBar.setStringPainted(true);
        progressBar.setString("Loading...");
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        overlayPanel.add(progressBar, new GridBagConstraints());

        // Intercept mouse events (mask prevents clicks)
        overlayPanel.addMouseListener(new java.awt.event.MouseAdapter() {});
        overlayPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {});

        // Attach to the frame as a glass pane
        parentFrame.setGlassPane(overlayPanel);
        overlayPanel.setVisible(false);
    }

    /** Shows or hides the overlay */
    public void show(boolean visible) {
        SwingUtilities.invokeLater(() -> {
            overlayPanel.setVisible(visible);
            if (visible) overlayPanel.repaint();
        });
    }
}
