package com.lngbaotran.lab306.service;

import com.lngbaotran.lab306.model.Telemetry;
import com.lngbaotran.lab306.repository.TelemetryRepository;
import com.lngbaotran.lab306.ws.TelemetryWebSocketHandler;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class MqttMessageHandlerService {

    private final TelemetryRepository telemetryRepository;
    private final TelemetryWebSocketHandler wsHandler;

    public MqttMessageHandlerService(TelemetryRepository telemetryRepository,
                                     TelemetryWebSocketHandler wsHandler) {
        this.telemetryRepository = telemetryRepository;
        this.wsHandler = wsHandler;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        String payload = message.getPayload().toString();
        System.out.println("📥 Received telemetry: " + payload);

        // Lưu DB
        Telemetry telemetry = new Telemetry();
        telemetry.setPayload(payload);
        telemetryRepository.save(telemetry);

        // Gửi realtime tới tất cả client WebSocket
        // wsHandler.sendToAll(payload);
        wsHandler.sendTelemetry(payload);
    }
}
