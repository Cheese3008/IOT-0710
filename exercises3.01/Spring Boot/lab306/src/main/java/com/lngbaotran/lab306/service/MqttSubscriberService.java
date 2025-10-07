package com.lngbaotran.lab306.service;

import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

@Service
public class MqttSubscriberService {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Value("${mqtt.topic.sub}")
    private String subTopic;

    private final TelemetryStreamService streamService;
    private MqttClient mqttClient;

    public MqttSubscriberService(TelemetryStreamService streamService) {
        this.streamService = streamService;
    }

    @PostConstruct
    public void init() {
        try {
            mqttClient = new MqttClient(brokerUrl, clientId + "_sub", null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("❌ Mất kết nối MQTT: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    System.out.printf("📩 MQTT nhận [%s]: %s%n", topic, payload);

                    // Gửi dữ liệu này ra stream SSE cho frontend
                    streamService.publish(payload);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            mqttClient.connect(options);
            mqttClient.subscribe(subTopic);
            System.out.println("✅ Đã subscribe topic: " + subTopic);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Lỗi MQTT Subscriber init: " + e.getMessage());
        }
    }
}
