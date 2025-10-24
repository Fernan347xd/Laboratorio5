package Domain.Dtos.maintenance;


import Utilities.MaintenanceType;

public class UpdateMaintenanceRequestDto {
    private Long id;
    private String description;
    private MaintenanceType type;
    private Long carId; // optional; if null, controller/service should keep existing association

    public UpdateMaintenanceRequestDto() {}

    public UpdateMaintenanceRequestDto(Long id, String description, MaintenanceType type, Long carId) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.carId = carId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public MaintenanceType getType() { return type; }
    public void setType(MaintenanceType type) { this.type = type; }

    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }
}
