package com.lngbaotran.lab306.service;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;
import com.lngbaotran.lab306.websocket.WebSocketHandler;

@Service
public class MqttService {

    private final String brokerUrl = "tcp://localhost:1883"; // đổi thành EMQX broker URL
    private final String clientId = "spring-backend";
    private MqttClient client;
    private WebSocketHandler webSocketHandler;

    public MqttService() {
        try {
            client = new MqttClient(brokerUrl, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            client.connect(options);

            // Subscribe topic telemetry từ ESP32
            client.subscribe("esp32/telemetry", (topic, msg) -> {
                String payload = new String(msg.getPayload());
                System.out.println("Telemetry from ESP32: " + payload);
                if (webSocketHandler != null) {
                    webSocketHandler.broadcast(payload); // gửi realtime về React
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void setWebSocketHandler(WebSocketHandler handler) {
        this.webSocketHandler = handler;
    }

    public void publishToDevice(String payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            client.publish("esp32/commands", message); // ESP32 subscribe topic này
            System.out.println("Sent command to ESP32: " + payload);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
