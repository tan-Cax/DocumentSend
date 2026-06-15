package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import utils.AppConfig;
import java.io.File;

public class SettingsPage extends StackPane {
    public SettingsPage() {
        this.setFocusTraversable(true);
        GridPane content = new GridPane();
        content.setHgap(15);
        content.setVgap(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-font-size: 14px;");

        TextField userField = new TextField(AppConfig.getUsername());
        userField.textProperty().addListener((o, old, n) -> AppConfig.setUsername(n));
        userField.setPrefWidth(220);
        userField.setPrefHeight(36);
        userField.setStyle("-fx-font-size: 14px;");
        deselectOnFocus(userField);

        TextField lPortField = new TextField(String.valueOf(AppConfig.getListenPort()));
        lPortField.textProperty().addListener((o, old, n) -> {
            if(n.matches("\\d*")) AppConfig.setListenPort(n.isEmpty() ? 0 : Integer.parseInt(n));
        });
        lPortField.setPrefWidth(120);
        lPortField.setPrefHeight(36);
        lPortField.setStyle("-fx-font-size: 14px;");
        deselectOnFocus(lPortField);

        TextField sPortField = new TextField(String.valueOf(AppConfig.getSendPort()));
        sPortField.textProperty().addListener((o, old, n) -> {
            if(n.matches("\\d*")) AppConfig.setSendPort(n.isEmpty() ? 0 : Integer.parseInt(n));
        });
        sPortField.setPrefWidth(120);
        sPortField.setPrefHeight(36);
        sPortField.setStyle("-fx-font-size: 14px;");
        deselectOnFocus(sPortField);

        Label pathLabel = new Label(AppConfig.getSaveDir());
        pathLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
        Button changePath = new Button("更改");
        changePath.setStyle("-fx-font-size: 14px;");
        changePath.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("选择存储目录");
            File current = new File(AppConfig.getSaveDir());
            if (current.exists()) chooser.setInitialDirectory(current);
            File selected = chooser.showDialog(this.getScene().getWindow());
            if (selected != null) {
                AppConfig.setSaveDir(selected.getAbsolutePath());
                pathLabel.setText(selected.getAbsolutePath());
            }
        });

        Label lblUser = new Label("用户名:");
        lblUser.setStyle("-fx-font-size: 14px;");
        Label lblListen = new Label("监听端口:");
        lblListen.setStyle("-fx-font-size: 14px;");
        Label lblSend = new Label("发送端口:");
        lblSend.setStyle("-fx-font-size: 14px;");
        Label lblSave = new Label("存储位置:");
        lblSave.setStyle("-fx-font-size: 14px;");

        content.add(lblUser, 0, 0);
        content.add(userField, 1, 0);
        content.add(lblListen, 0, 1);
        content.add(lPortField, 1, 1);
        content.add(lblSend, 0, 2);
        content.add(sPortField, 1, 2);
        content.add(lblSave, 0, 3);
        content.add(new HBox(10, pathLabel, changePath), 1, 3);

        // 内容居中靠上
        VBox wrapper = new VBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(content, Priority.NEVER);
        getChildren().add(wrapper);

        Platform.runLater(() -> this.requestFocus());
    }

    private void deselectOnFocus(TextField field) {
        field.focusedProperty().addListener((obs, old, focused) -> {
            if (focused && !field.getText().isEmpty()) {
                Platform.runLater(() -> {
                    field.deselect();
                    field.positionCaret(field.getText().length());
                });
            }
        });
    }
}
