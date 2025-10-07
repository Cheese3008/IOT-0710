package com.lngbaotran.lab306.repository;

import com.lngbaotran.lab306.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByStatus(String status);

    // Thêm phương thức tìm theo tên
    Optional<Device> findByName(String name);
}
