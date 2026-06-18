package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import listener.IDeviceDiscoveryListener;
import network.SocketClient;
import udp.UdpService;
import utils.AppConfig;

public class MessagePage extends StackPane {
    private final TextField ipField = new TextField();
    private final TextField portField = new TextField();
    private final TextFlow chatFlow = new TextFlow();
    private final DeviceDiscoveryPanel devicePanel;

    public MessagePage() {
        this.setFocusTraversable(true);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white;");

        // 目标地址栏
        ipField.setId("msgIpField");
        ipField.setPromptText("目标 IP");
        ipField.setText(AppConfig.getTargetIp());
        ipField.textProperty().addListener((obs, oldV, newV) -> AppConfig.setTargetIp(newV));
        ipField.setPrefWidth(140);

        portField.setId("msgPortField");
        portField.setPromptText("端口");
        portField.setText(String.valueOf(AppConfig.getListenPort()));
        portField.setPrefWidth(70);

        HBox targetBar = new HBox(10, new Label("目标:"), ipField, portField);
        targetBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // 设备发现面板
        devicePanel = new DeviceDiscoveryPanel(info -> {
            ipField.setText(info.getIp());
            portField.setText(String.valueOf(info.getTcpPort()));
        });

        // 输入栏（多行输入框，自动换行可滚动）
        TextArea inputArea = new TextArea();
        inputArea.setPromptText("输入消息...");
        inputArea.setWrapText(true);
        inputArea.setPrefHeight(60);
        inputArea.setMaxHeight(120);
        HBox.setHgrow(inputArea, Priority.ALWAYS);

        Button sendBtn = new Button("发送");
        sendBtn.setPrefHeight(60);
        sendBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 0 20;");
        sendBtn.setOnAction(e -> {
            String msg = inputArea.getText().trim();
            if (msg.isEmpty()) return;

            String targetIp = ipField.getText().trim();
            int targetPort = Integer.parseInt(portField.getText().trim());
            inputArea.clear();

            SocketClient sender = new SocketClient();
            sender.sendTextTo(msg, targetIp, targetPort, () ->
                Platform.runLater(() -> appendText("[我] " + msg, Color.BLACK))
            );
        });

        HBox sendBar = new HBox(10, inputArea, sendBtn);
        sendBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox.setVgrow(sendBar, Priority.NEVER);

        // 消息显示区
        chatFlow.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px; -fx-padding: 5;");
        ScrollPane scrollPane = new ScrollPane(chatFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        layout.getChildren().addAll(targetBar, devicePanel, sendBar, scrollPane);
        getChildren().add(layout);

        Platform.runLater(() -> this.requestFocus());
    }

    private void appendText(String msg, Color color) {
        Text text = new Text(msg + "\n");
        text.setFill(color);
        chatFlow.getChildren().add(text);
    }

    public void appendReceivedText(String text) {
        appendText("[对方] " + text, Color.BLACK);
    }

    public void appendError(String msg) {
        appendText("[错误] " + msg, Color.RED);
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
