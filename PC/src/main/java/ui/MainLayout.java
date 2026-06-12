package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class MainLayout extends BorderPane {
    private final StackPane contentArea = new StackPane();
    private final MessagePage messagePage = new MessagePage();
    private final FileTransferPage filePage = new FileTransferPage();
    private final SettingsPage settingsPage = new SettingsPage();
    private Label activeLabel;

    public MainLayout() {
        this.setStyle("-fx-background-color: white;");

        HBox topNav = new HBox();
        topNav.setPadding(new Insets(15, 0, 15, 20));
        topNav.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        topNav.setStyle("-fx-background-color: #E3F2FD;");

        Label txtItem = createNavItem("文本", messagePage);
        Label pipe1 = new Label("|");
        pipe1.setStyle("-fx-font-size: 14px; -fx-text-fill: #ccc;");
        Label fileItem = createNavItem("文件", filePage);
        Label pipe2 = new Label("|");
        pipe2.setStyle("-fx-font-size: 14px; -fx-text-fill: #ccc;");
        Label setItem = createNavItem("设置", settingsPage);

        topNav.getChildren().addAll(txtItem, pipe1, fileItem, pipe2, setItem);
        setTop(topNav);
        setCenter(contentArea);

        contentArea.getChildren().addAll(messagePage, filePage, settingsPage);
        selectLabel(txtItem);
        showPage(messagePage);
    }

    private Label createNavItem(String text, StackPane page) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1976D2; -fx-padding: 3 6;");
        label.setCursor(Cursor.HAND);
        label.setOnMouseEntered(e -> {
            if (label != activeLabel) {
                label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0D47A1; -fx-padding: 3 6; -fx-translate-y: -2;");
            }
        });
        label.setOnMouseExited(e -> {
            if (label != activeLabel) {
                label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1976D2; -fx-padding: 3 6; -fx-translate-y: 0;");
            }
        });
        label.setOnMouseClicked(e -> {
            selectLabel(label);
            showPage(page);
        });
        return label;
    }

    private void selectLabel(Label label) {
        if (activeLabel != null) {
            activeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1976D2; -fx-padding: 3 6; -fx-translate-y: 0;");
        }
        activeLabel = label;
        activeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0D47A1; -fx-padding: 3 6; -fx-translate-y: 0;");
    }

    private void showPage(StackPane page) {
        for (javafx.scene.Node child : contentArea.getChildren()) {
            child.setVisible(false);
        }
        page.setVisible(true);
        page.toFront();
        Platform.runLater(() -> page.requestFocus());
    }
}
