package kz.aitu.routeoptimizer.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class RouteOptimizerGui {

    private RouteOptimizerGui() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            useSystemLookAndFeel();
            new RouteOptimizerFrame().setVisible(true);
        });
    }

    private static void useSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Swing keeps its cross-platform look and feel when the system theme is unavailable.
        }
    }
}
