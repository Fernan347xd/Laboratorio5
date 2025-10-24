package org.example.Domain.dtos.maintenance;

public class DeleteMaintenanceRequestDto {
    private Long id;

    public DeleteMaintenanceRequestDto() {}

    public DeleteMaintenanceRequestDto(Long id) { this.id = id; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
