package Domain.Dtos.maintenance;

import java.util.List;

public class ListMaintenanceResponseDto {
    private List<MaintenanceResponseDto> maintenances;

    public ListMaintenanceResponseDto() {}

    public ListMaintenanceResponseDto(List<MaintenanceResponseDto> maintenances) {
        this.maintenances = maintenances;
    }

    public List<MaintenanceResponseDto> getMaintenances() { return maintenances; }
    public void setMaintenances(List<MaintenanceResponseDto> maintenances) { this.maintenances = maintenances; }
}
