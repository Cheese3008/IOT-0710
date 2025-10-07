package com.lngbaotran.lab306.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lngbaotran.lab306.model.Device;
import com.lngbaotran.lab306.model.Telemetry;
import com.lngbaotran.lab306.repository.DeviceRepository;
import com.lngbaotran.lab306.repository.TelemetryRepository;
import com.lngbaotran.lab306.ws.TelemetryWebSocketHandler;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MqttMessageHandlerService {

    private final TelemetryRepository telemetryRepository;
    private final DeviceRepository deviceRepository;
    private final TelemetryWebSocketHandler wsHandler;
    private final ObjectMapper objectMapper;

    public MqttMessageHandlerService(TelemetryRepository telemetryRepository,
                                     DeviceRepository deviceRepository,
                                     TelemetryWebSocketHandler wsHandler) {
        this.telemetryRepository = telemetryRepository;
        this.deviceRepository = deviceRepository;
        this.wsHandler = wsHandler;
        this.objectMapper = new ObjectMapper();
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        String payload = message.getPayload().toString();
        String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
        System.out.println("📥 [MQTT] Received on topic [" + topic + "]: " + payload);

        try {
            JsonNode json = objectMapper.readTree(payload);

            // --- Lấy tên device từ topic, ví dụ: demo/room1/device/cmd → room1
            String[] topicParts = topic.split("/");
            if (topicParts.length < 2) {
                System.err.println("❌ Topic không hợp lệ: " + topic);
                return;
            }
            String deviceName = topicParts[1];

            Optional<Device> optDevice = deviceRepository.findAll()
                    .stream()
                    .filter(d -> d.getName().equalsIgnoreCase(deviceName))
                    .findFirst();

            Device device;
            if (optDevice.isPresent()) {
                device = optDevice.get();
            } else {
                device = new Device();
                device.setName(deviceName);
                device.setTopic(topic);
                deviceRepository.save(device);
                System.out.println("✅ Created new device: " + deviceName);
            }

            // --- Xử lý telemetry
            if (topic.endsWith("/sensor/state")) {
                Telemetry telemetry = new Telemetry();
                telemetry.setDevice(device);
                telemetry.setTemperature(json.has("temp_c") ? json.get("temp_c").asDouble() : null);
                telemetry.setHumidity(json.has("humidity") ? json.get("humidity").asDouble() : null);
                telemetry.setLightLevel(json.has("light_lux") ? json.get("light_lux").asInt() : null);
                telemetry.setPayload(payload);
                telemetry.setTimestamp(LocalDateTime.now());
                telemetryRepository.save(telemetry);

            } else if (topic.endsWith("/sys/info")) {
                if (json.has("wifi_signal")) device.setWifiSignal(json.get("wifi_signal").asInt());
                if (json.has("firmware")) device.setFirmwareVersion(json.get("firmware").asText());

            } else if (topic.endsWith("/device/cmd")) {
                // --- Cập nhật trạng thái module (LED, Fan, ESP…)
                if (json.has("light")) device.setLightStatus(json.get("light").asText().toUpperCase());
                if (json.has("fan")) device.setFanStatus(json.get("fan").asText().toUpperCase());
                if (json.has("status")) device.setStatus(json.get("status").asText().toUpperCase());
            }else if (topic.endsWith("/device/state")) {
                // --- Cập nhật trạng thái từng module
                if (json.has("light")) {
                    device.setLightStatus(json.get("light").asText().toUpperCase());
                }
                if (json.has("fan")) {
                    device.setFanStatus(json.get("fan").asText().toUpperCase());
                }
                // --- Nếu muốn, có thể thêm các module khác như ESP, Relay, ...
            }

            // --- Cập nhật lastSeen
            device.setLastSeen(LocalDateTime.now());
            deviceRepository.save(device);

            // --- Gửi realtime tới WebSocket client
            wsHandler.sendTelemetry(payload);

        } catch (Exception e) {
            System.err.println("❌ Error parsing MQTT payload:");
            e.printStackTrace();
        }
    }
}
