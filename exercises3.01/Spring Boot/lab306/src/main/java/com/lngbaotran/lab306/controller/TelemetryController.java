package com.lngbaotran.lab306.controller;

import com.lngbaotran.lab306.model.Device;
import com.lngbaotran.lab306.model.Telemetry;
import com.lngbaotran.lab306.repository.DeviceRepository;
import com.lngbaotran.lab306.repository.TelemetryRepository;
import com.lngbaotran.lab306.service.TelemetryStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/telemetry")
public class TelemetryController {

    @Autowired
    private TelemetryRepository telemetryRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private TelemetryStreamService streamService;

    // --- Endpoint SSE cho React/Flutter subscribe ---
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamTelemetry() {
        return streamService.getStream();
    }

    // --- Lấy tất cả telemetry ---
    @GetMapping
    public List<Telemetry> getAllTelemetry() {
        return telemetryRepository.findAll();
    }

    // --- Lấy telemetry theo device ---
    @GetMapping("/device/{deviceId}")
    public List<Telemetry> getTelemetryByDevice(@PathVariable Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("❌ Device not found: ID " + deviceId));
        return telemetryRepository.findByDeviceOrderByTimestampDesc(device);
    }

    // --- Thêm telemetry mới cho device ---
    @PostMapping("/device/{deviceId}")
    public Telemetry addTelemetry(@PathVariable Long deviceId, @RequestBody Telemetry telemetryRequest) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("❌ Device not found: ID " + deviceId));

        Telemetry telemetry = new Telemetry();
        telemetry.setDevice(device);
        telemetry.setTemperature(telemetryRequest.getTemperature());
        telemetry.setHumidity(telemetryRequest.getHumidity());
        telemetry.setLightLevel(telemetryRequest.getLightLevel());
        telemetry.setPayload(telemetryRequest.getPayload());
        telemetry.setTimestamp(LocalDateTime.now());

        return telemetryRepository.save(telemetry);
    }
}
