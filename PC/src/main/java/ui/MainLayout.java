package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import listener.IDeviceDiscoveryListener;
import udp.UdpService;
import utils.AppConfig;
import utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

public class MainLayout extends BorderPane {
    private static MainLayout instance;
    private static UdpService udpService;
    private static final List<IDeviceDiscoveryListener.DeviceInfo> pendingDevices = new ArrayList<>();

    private final StackPane contentArea = new StackPane();
    private final MessagePage messagePage = new MessagePage();
    private final FileTransferPage filePage = new FileTransferPage();
    private final SettingsPage settingsPage = new SettingsPage();
    private Label activeLabel;

    public MainLayout() {
        instance = this;
        this.setStyle("-fx-background-color: white;");

        // 传递 UdpService 到包含设备面板的子页面
        if (udpService != null) {
            messagePage.setUdpService(udpService);
            filePage.setUdpService(udpService);
        }

        // 清空待处理设备列表到各个页面
        flushPendingDevices();

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

        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(8, 0, 8, 20));
        statusBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 1 0;");
        Label usernameLabel = new Label("用户名：" + AppConfig.getUsername());
        usernameLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #333; -fx-padding: 0 30 0 0;");
        Label ipLabel = new Label("我的IP：" + NetworkUtils.getLocalIpv4Address().getHostAddress());
        ipLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");
        statusBar.getChildren().addAll(usernameLabel, ipLabel);

        VBox centerBox = new VBox(statusBar, contentArea);
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        setCenter(centerBox);

        contentArea.getChildren().addAll(messagePage, filePage, settingsPage);
        selectLabel(txtItem);
        showPage(messagePage);
    }

    public static synchronized void addPendingDevice(IDeviceDiscoveryListener.DeviceInfo info) {
        MainLayout inst = instance;
        if (inst != null) {
            Platform.runLater(() -> {
                inst.messagePage.addDiscoveredDevice(info);
                inst.filePage.addDiscoveredDevice(info);
            });
        } else {
            pendingDevices.add(info);
        }
    }

    private void flushPendingDevices() {
        for (IDeviceDiscoveryListener.DeviceInfo info : pendingDevices) {
            messagePage.addDiscoveredDevice(info);
            filePage.addDiscoveredDevice(info);
        }
        pendingDevices.clear();
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
        for (Node child : contentArea.getChildren()) {
            child.setVisible(false);
        }
        page.setVisible(true);
        page.toFront();
        Platform.runLater(() -> page.requestFocus());
    }

    public static MainLayout getInstance() {
        return instance;
    }

    public static void setUdpService(UdpService svc) {
        udpService = svc;
    }

    public MessagePage getMessagePage() {
        return messagePage;
    }

    public FileTransferPage getFilePage() {
        return filePage;
    }
}
