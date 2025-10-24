package org.example.Domain.models;

import jakarta.persistence.*;

@Entity
@Table(name = "maintenance")
public class Maintenance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String description;

    @Enumerated(EnumType.STRING)
    private MaintenanceType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false, foreignKey = @ForeignKey(name = "fk_car_maintenance"))
    private Car carMaintenance;

    public Maintenance() {
    }

    public Maintenance(Long id, String description, MaintenanceType type, Car carMaintenance) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.carMaintenance = carMaintenance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MaintenanceType getType() {
        return type;
    }

    public void setType(MaintenanceType type) {
        this.type = type;
    }

    public Car getCarMaintenance() {
        return carMaintenance;
    }

    public void setCarMaintenance(Car carMaintenance) {
        this.carMaintenance = carMaintenance;
    }

    @Override
    public String toString() {
        return "Maintenance{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", type=" + type +
                '}';
    }
}
