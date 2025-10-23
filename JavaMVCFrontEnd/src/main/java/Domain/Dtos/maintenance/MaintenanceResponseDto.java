package org.example.Domain.dtos.maintenance;

import org.example.Domain.models.MaintenanceType;

public class MaintenanceResponseDto {
    private Long id;
    private String description;
    private MaintenanceType type;
    private Long carId;
    private String carMake;
    private String carModel;

    public MaintenanceResponseDto() {}

    public MaintenanceResponseDto(Long id, String description, MaintenanceType type, Long carId, String carMake, String carModel) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.carId = carId;
        this.carMake = carMake;
        this.carModel = carModel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public MaintenanceType getType() { return type; }
    public void setType(MaintenanceType type) { this.type = type; }

    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }

    public String getCarMake() { return carMake; }
    public void setCarMake(String carMake) { this.carMake = carMake; }

    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }
}
