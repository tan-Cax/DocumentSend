package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import listener.IDeviceDiscoveryListener.DeviceInfo;
import udp.UdpService;

import java.util.function.Consumer;

public class DeviceDiscoveryPanel extends VBox {
    private final ListView<DeviceInfo> deviceList;
    private final ObservableList<DeviceInfo> items = FXCollections.observableArrayList();
    private UdpService udpService;
    private final Consumer<DeviceInfo> onDeviceSelected;

    public DeviceDiscoveryPanel(Consumer<DeviceInfo> onDeviceSelected) {
        super(5);
        this.onDeviceSelected = onDeviceSelected;
        setPadding(new Insets(5, 0, 5, 0));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("寻找设备");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label refreshIcon = new Label("\uD83D\uDD04");
        refreshIcon.setStyle("-fx-font-size: 16px; -fx-cursor: hand;");
        refreshIcon.setOnMousePressed(e -> {
            refreshIcon.setStyle("-fx-font-size: 16px; -fx-cursor: hand; -fx-opacity: 0.5; -fx-scale-x: 0.9; -fx-scale-y: 0.9;");
        });
        refreshIcon.setOnMouseReleased(e -> {
            refreshIcon.setStyle("-fx-font-size: 16px; -fx-cursor: hand; -fx-opacity: 1; -fx-scale-x: 1; -fx-scale-y: 1;");
            refresh();
        });

        header.getChildren().addAll(title, spacer, refreshIcon);

        deviceList = new ListView<>(items);
        deviceList.setPrefHeight(120);
        deviceList.setPlaceholder(new Label("暂无设备"));
        deviceList.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(DeviceInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label icon = new Label("pc".equals(item.getDevice()) ? "\uD83D\uDCBB" : "\uD83D\uDCF1");
                icon.setStyle("-fx-font-size: 22px; -fx-padding: 0 8 0 0;");

                Label name = new Label(item.getName());
                name.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

                Label addr = new Label(item.getIp() + ":" + item.getTcpPort());
                addr.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

                VBox info = new VBox(1, name, addr);

                HBox root = new HBox(8, icon, info);
                root.setAlignment(Pos.CENTER_LEFT);
                setGraphic(root);
            }
        });

        deviceList.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null && onDeviceSelected != null) {
                onDeviceSelected.accept(selected);
            }
        });

        getChildren().addAll(header, deviceList);
    }

    public void setUdpService(UdpService svc) {
        this.udpService = svc;
    }

    public void addDevice(DeviceInfo info) {
        if (info == null) return;
        if (!items.contains(info)) {
            items.add(info);
        }
    }

    public void clearDevices() {
        items.clear();
    }

    private void refresh() {
        items.clear();
        if (udpService != null) {
            udpService.clearDevices();
            udpService.sendBroadcast();
        }
    }
}
