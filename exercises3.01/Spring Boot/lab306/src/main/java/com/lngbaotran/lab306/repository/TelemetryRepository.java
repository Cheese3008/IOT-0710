package com.lngbaotran.lab306.repository;

import com.lngbaotran.lab306.model.Device;
import com.lngbaotran.lab306.model.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {
    List<Telemetry> findByDeviceOrderByTimestampDesc(Device device);
}
