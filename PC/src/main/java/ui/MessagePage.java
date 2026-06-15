package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import network.SocketClient;
import utils.AppConfig;

public class MessagePage extends StackPane {
    private final TextFlow chatFlow = new TextFlow();

    public MessagePage() {
        this.setFocusTraversable(true);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white;");

        // 目标地址栏
        TextField ipField = new TextField();
        ipField.setPromptText("目标 IP");
        ipField.setText(AppConfig.getTargetIp());
        ipField.textProperty().addListener((obs, oldV, newV) -> AppConfig.setTargetIp(newV));
        ipField.setPrefWidth(140);

        TextField portField = new TextField();
        portField.setPromptText("端口");
        portField.setText(String.valueOf(AppConfig.getSendPort()));
        portField.setPrefWidth(70);

        HBox targetBar = new HBox(10, new Label("目标:"), ipField, portField);
        targetBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

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

            appendText("[我] " + msg, Color.BLACK);
            inputArea.clear();

            SocketClient sender = new SocketClient();
            sender.sendTextTo(msg, targetIp, targetPort);
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

        layout.getChildren().addAll(targetBar, sendBar, scrollPane);
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
}
