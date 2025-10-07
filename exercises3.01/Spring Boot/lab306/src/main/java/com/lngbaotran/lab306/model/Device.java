package com.lngbaotran.lab306.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String topic;
    private String status; // Trạng thái chung ON/OFF của thiết bị

    // --- DỮ LIỆU CẢM BIẾN ---
    private Double temperature;
    private Double humidity;
    private Integer lightLevel; // lux

    // --- DỮ LIỆU ĐIỀU KHIỂN & HỆ THỐNG ---
    private String lightStatus = "OFF"; // Trạng thái đèn
    private String fanStatus = "OFF"; // Trạng thái quạt
    private String firmwareVersion = "v1.0.0";
    private Integer wifiSignal; // dBm
    private LocalDateTime lastSeen; // Thời điểm cuối cùng nhận được dữ liệu

    // --- Getters and Setters cho tất cả các trường ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }

    public Integer getLightLevel() { return lightLevel; }
    public void setLightLevel(Integer lightLevel) { this.lightLevel = lightLevel; }

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
}
