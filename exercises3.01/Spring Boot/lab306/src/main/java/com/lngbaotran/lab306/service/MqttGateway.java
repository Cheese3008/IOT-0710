package com.lngbaotran.lab306.service;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;

@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttGateway {

    // Publish với default topic (cấu hình trong MqttConfig)
    void sendToMqtt(String data);

    // Publish với topic chỉ định
    void sendToMqtt(String data, @Header("mqtt_topic") String topic);
}
