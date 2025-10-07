package com.lngbaotran.lab306.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Telemetry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ tới thiết bị
    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    private Double temperature;
    private Double humidity;
    private Integer lightLevel;

    private String payload; // dữ liệu thô (JSON nếu cần)
    private LocalDateTime timestamp = LocalDateTime.now();

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }

    public Integer getLightLevel() { return lightLevel; }
    public void setLightLevel(Integer lightLevel) { this.lightLevel = lightLevel; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
