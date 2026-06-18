package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import listener.IDeviceDiscoveryListener;
import network.SocketClient;
import udp.UdpService;
import utils.AppConfig;

import java.io.File;
import java.util.List;

public class FileTransferPage extends StackPane {
    private final TextField ipField = new TextField();
    private final TextField portField = new TextField();
    private final DeviceDiscoveryPanel devicePanel;
    private final Label filePathLabel = new Label("未选择文件");
    private final TextFlow logFlow = new TextFlow();
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
        portField.setText(String.valueOf(AppConfig.getListenPort()));
        portField.setPrefWidth(70);
        deselectOnFocus(portField);

        HBox targetBar = new HBox(10, new Label("目标:"), ipField, portField);
        targetBar.setAlignment(Pos.CENTER_LEFT);

        // 设备发现面板
        devicePanel = new DeviceDiscoveryPanel(info -> {
            ipField.setText(info.getIp());
            portField.setText(String.valueOf(info.getTcpPort()));
        });

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
                appendError("请先选择文件");
                return;
            }
            String targetIp = ipField.getText().trim();
            int targetPort = Integer.parseInt(portField.getText().trim());
            File fileRef = selectedFile;

            SocketClient sender = new SocketClient();
            sender.sendFileTo(selectedFile, targetIp, targetPort, () ->
                Platform.runLater(() -> appendText("[发送] " + fileRef.getName() + " → " + targetIp + ":" + targetPort, Color.BLACK))
            );
        });

        HBox btnBar = new HBox(15, chooseBtn, sendBtn);
        btnBar.setAlignment(Pos.CENTER);

        // 日志区
        logFlow.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px; -fx-padding: 5;");
        ScrollPane scrollPane = new ScrollPane(logFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(120);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        layout.getChildren().addAll(targetBar, devicePanel, dropZone, btnBar, scrollPane);
        getChildren().add(layout);

        Platform.runLater(() -> this.requestFocus());
    }

    private void appendText(String msg, Color color) {
        Text text = new Text(msg + "\n");
        text.setFill(color);
        logFlow.getChildren().add(text);
    }

    public void appendReceivedFile(String fileName) {
        appendText("[接收] " + fileName, Color.BLACK);
    }

    public void appendError(String msg) {
        appendText("[错误] " + msg, Color.RED);
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

    public void addDiscoveredDevice(IDeviceDiscoveryListener.DeviceInfo info) {
        devicePanel.addDevice(info);
    }

    public void clearDiscoveredDevices() {
        devicePanel.clearDevices();
    }

    public void setUdpService(UdpService svc) {
        devicePanel.setUdpService(svc);
    }
}
