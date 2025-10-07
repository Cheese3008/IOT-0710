package com.lngbaotran.lab306.repository;

import com.lngbaotran.lab306.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
}
