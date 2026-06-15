package network;

import javafx.application.Platform;
import ui.MainLayout;

public class NetworkErrorCallback {
    private static NetworkErrorCallback instance;

    private NetworkErrorCallback() {}

    public static NetworkErrorCallback getInstance() {
        if (instance == null) instance = new NetworkErrorCallback();
        return instance;
    }

    public void sendError(String msg) {
        System.err.println(msg);
        try {
            Platform.runLater(() -> {
                MainLayout ml = MainLayout.getInstance();
                if (ml != null) ml.getFilePage().appendError(msg);
            });
        } catch (IllegalStateException e) {
            // JavaFX not initialized (e.g. during tests)
        }
    }

    public void textError(String msg) {
        System.err.println(msg);
        try {
            Platform.runLater(() -> {
                MainLayout ml = MainLayout.getInstance();
                if (ml != null) ml.getMessagePage().appendError(msg);
            });
        } catch (IllegalStateException e) {
            // JavaFX not initialized (e.g. during tests)
        }
    }

    public void receiveError(String msg) {
        System.err.println(msg);
        try {
            Platform.runLater(() -> {
                MainLayout ml = MainLayout.getInstance();
                if (ml != null) ml.getFilePage().appendError(msg);
            });
        } catch (IllegalStateException e) {
            // JavaFX not initialized (e.g. during tests)
        }
    }
}
