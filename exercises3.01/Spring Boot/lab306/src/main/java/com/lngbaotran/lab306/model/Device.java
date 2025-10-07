package com.lngbaotran.lab306.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String topic;
    private String status; // ON/OFF

    // --- DỮ LIỆU ĐIỀU KHIỂN & HỆ THỐNG ---
    private String lightStatus = "OFF";
    private String fanStatus = "OFF";
    private String firmwareVersion = "v1.0.0";
    private Integer wifiSignal;
    private LocalDateTime lastSeen;

    // --- Quan hệ với Telemetry ---
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Telemetry> telemetryList;

    // --- Getters và Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLightStatus() { return lightStatus; }
    public void setLightStatus(String lightStatus) { this.lightStatus = lightStatus; }

    public String getFanStatus() { return fanStatus; }
    public void setFanStatus(String fanStatus) { this.fanStatus = fanStatus; }

    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }

    public Integer getWifiSignal() { return wifiSignal; }
    public void setWifiSignal(Integer wifiSignal) { this.wifiSignal = wifiSignal; }

    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }

    public List<Telemetry> getTelemetryList() { return telemetryList; }
    public void setTelemetryList(List<Telemetry> telemetryList) { this.telemetryList = telemetryList; }
}
