package org.example.DataAccess.services;

import org.example.Domain.models.Car;
import org.example.Domain.models.Maintenance;
import org.example.Domain.models.MaintenanceType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class MaintenanceService {
    private final SessionFactory sessionFactory;

    public MaintenanceService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Maintenance createMaintenance(String description, MaintenanceType type, Car carMaintenance) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Maintenance maintenance = new Maintenance();
            maintenance.setDescription(description);
            maintenance.setType(type);
            if (carMaintenance != null && carMaintenance.getId() != null) {
                Car managedCar = session.get(Car.class, carMaintenance.getId());
                maintenance.setCarMaintenance(managedCar);
            } else {
                maintenance.setCarMaintenance(null);
            }
            session.persist(maintenance);
            tx.commit();
            return maintenance;
        }
    }

    public Maintenance getMaintenanceById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "select m from Maintenance m left join fetch m.carMaintenance where m.id = :id",
                            Maintenance.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }

    public List<Maintenance> getAllMaintenanceByCarId(Long carId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "select m from Maintenance m join fetch m.carMaintenance c where c.id = :carId order by m.id asc",
                            Maintenance.class)
                    .setParameter("carId", carId)
                    .list();
        }
    }

    public Maintenance updateMaintenance(int maintenanceId, String description, MaintenanceType type, Car carMaintenance) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Maintenance maintenance = session.find(Maintenance.class, maintenanceId);
            if (maintenance != null) {
                maintenance.setDescription(description);
                maintenance.setType(type);
                if (carMaintenance != null && carMaintenance.getId() != null) {
                    Car managedCar = session.get(Car.class, carMaintenance.getId());
                    maintenance.setCarMaintenance(managedCar);
                }
                maintenance = session.merge(maintenance);
            }
            tx.commit();
            return maintenance;
        }
    }

    public boolean deleteMaintenance(Long maintenanceId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Maintenance maintenance = session.find(Maintenance.class, maintenanceId.intValue());
            if (maintenance != null) {
                session.remove(maintenance);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        }
    }
}
