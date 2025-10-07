package com.lngbaotran.lab306.controller;

import com.lngbaotran.lab306.model.Device;
import com.lngbaotran.lab306.repository.DeviceRepository;
import com.lngbaotran.lab306.service.MqttGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MqttGateway mqttGateway;

    /** Lấy danh sách thiết bị */
    @GetMapping
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    /** Đăng ký thiết bị mới */
    @PostMapping
    public Device addDevice(@RequestBody Device device) {
        return deviceRepository.save(device);
    }

    /** Gửi lệnh điều khiển tới thiết bị qua MQTT */
    @PostMapping("/{id}/control")
    public String controlDevice(@PathVariable Long id, @RequestBody CommandRequest request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Device not found: ID " + id));

        // Cập nhật trạng thái chung (ON/OFF)
        device.setStatus(request.getCommand().toUpperCase());
        deviceRepository.save(device);

        // Xác định topic MQTT
        String topic = (device.getTopic() != null && !device.getTopic().isEmpty())
                ? device.getTopic()
                : "demo/room1/device/cmd";

        // Tạo JSON payload gửi cho ESP32
        String payload = "{\"" + request.getTarget() + "\":\"" + request.getCommand().toLowerCase() + "\"}";

        // Publish tới MQTT Broker
        mqttGateway.sendToMqtt(payload, topic);
        System.out.println("🚀 [MQTT] Sent to topic [" + topic + "]: " + payload);

        return "✅ Command sent to " + device.getName() +
               ": " + payload + " (topic: " + topic + ")";
    }

    /** DTO nhận lệnh điều khiển từ client */
    public static class CommandRequest {
        private String target;  // ví dụ: "light", "fan"
        private String command; // ví dụ: "on", "off"

        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }

        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }
    }
}
