package org.example.Domain.dtos.maintenance;

import org.example.Domain.models.MaintenanceType;

public class AddMaintenanceRequestDto {
    private String description;
    private MaintenanceType type;
    private Long carId;

    public AddMaintenanceRequestDto() {}

    public AddMaintenanceRequestDto(String description, MaintenanceType type, Long carId) {
        this.description = description;
        this.type = type;
        this.carId = carId;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public MaintenanceType getType() { return type; }
    public void setType(MaintenanceType type) { this.type = type; }

    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }
}
