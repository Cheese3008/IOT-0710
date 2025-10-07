package com.lngbaotran.lab306.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;

@Service
public class MqttPublisherService {

    @Autowired
    @Qualifier("mqttOutboundChannel") // đảm bảo đúng channel
    private MessageChannel mqttOutboundChannel;

    public void publish(String topic, String payload) {
        mqttOutboundChannel.send(
                MessageBuilder.withPayload(payload)
                        .setHeader("mqtt_topic", topic) // Spring Integration MQTT sẽ đọc header này
                        .build()
        );
    }
}


