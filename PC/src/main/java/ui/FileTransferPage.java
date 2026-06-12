package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import network.SocketClient;
import utils.AppConfig;

import java.io.File;
import java.util.List;

public class FileTransferPage extends StackPane {
    private final TextField ipField = new TextField();
    private final TextField portField = new TextField();
    private final Label filePathLabel = new Label("未选择文件");
    private final TextArea logArea = new TextArea();
    private final VBox dropZone = new VBox(10);
    private File selectedFile;

    public FileTransferPage() {
        this.setFocusTraversable(true);
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        // 目标地址栏
        ipField.setPromptText("目标 IP");
        ipField.setText(AppConfig.getTargetIp());
        ipField.textProperty().addListener((obs, oldV, newV) -> AppConfig.setTargetIp(newV));
        ipField.setPrefWidth(140);
        deselectOnFocus(ipField);

        portField.setPromptText("端口");
        portField.setText(String.valueOf(AppConfig.getSendPort()));
        portField.setPrefWidth(70);
        deselectOnFocus(portField);

        HBox targetBar = new HBox(10, new Label("目标:"), ipField, portField);
        targetBar.setAlignment(Pos.CENTER_LEFT);

        // 拖拽区域
        dropZone.setAlignment(Pos.CENTER);
        dropZone.setPrefHeight(160);
        dropZone.setStyle("-fx-border-color: #BBDEFB; -fx-border-width: 2; -fx-border-style: dashed; -fx-background-color: #F5F5F5; -fx-background-radius: 8; -fx-border-radius: 8;");
        Label hint = new Label("拖拽文件至此或粘贴文件");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");

        filePathLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");
        VBox.setVgrow(dropZone, Priority.ALWAYS);

        dropZone.getChildren().addAll(hint, filePathLabel);

        dropZone.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasFiles()) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });
        dropZone.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                if (files.size() > 1) {
                    showAlert("一次只能发送一个文件");
                    e.setDropCompleted(false);
                    e.consume();
                    return;
                }
                selectedFile = files.get(0);
                filePathLabel.setText(selectedFile.getName() + " (" + formatSize(selectedFile.length()) + ")");
                e.setDropCompleted(true);
            }
            e.consume();
        });

        dropZone.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.isShortcutDown() && e.getCode() == KeyCode.V) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                if (clipboard.hasFiles()) {
                    List<File> files = clipboard.getFiles();
                    if (files.size() > 1) {
                        showAlert("一次只能发送一个文件");
                        return;
                    }
                    selectedFile = files.get(0);
                    filePathLabel.setText(selectedFile.getName() + " (" + formatSize(selectedFile.length()) + ")");
                }
                e.consume();
            }
        });
        dropZone.setFocusTraversable(true);

        // 按钮区（居中）
        Button chooseBtn = new Button("选择文件");
        chooseBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 24;");
        chooseBtn.setPrefHeight(40);
        chooseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("选择文件");
            File file = fc.showOpenDialog(getScene().getWindow());
            if (file != null) {
                selectedFile = file;
                filePathLabel.setText(selectedFile.getName() + " (" + formatSize(selectedFile.length()) + ")");
            }
        });

        Button sendBtn = new Button("发送文件");
        sendBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 24;");
        sendBtn.setPrefHeight(40);
        sendBtn.setOnAction(e -> {
            if (selectedFile == null) {
                logArea.appendText("[错误] 请先选择文件\n");
                return;
            }
            String targetIp = ipField.getText().trim();
            int targetPort = Integer.parseInt(portField.getText().trim());

            logArea.appendText("[发送] " + selectedFile.getName() + " → " + targetIp + ":" + targetPort + "\n");

            SocketClient sender = new SocketClient();
            sender.sendFileTo(selectedFile, targetIp, targetPort);
        });

        HBox btnBar = new HBox(15, chooseBtn, sendBtn);
        btnBar.setAlignment(Pos.CENTER);

        // 日志区
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px;");
        logArea.setPrefHeight(120);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        layout.getChildren().addAll(targetBar, dropZone, btnBar, logArea);
        getChildren().add(layout);

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

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
