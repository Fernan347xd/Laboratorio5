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

    // -------------------------
    // CREATE
    // -------------------------
    public Maintenance createMaintenance(String description, MaintenanceType type, Car carMaintenance) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            Maintenance maintenance = new Maintenance();
            maintenance.setDescription(description);
            maintenance.setType(type);
            maintenance.setCarMaintenance(carMaintenance);

            session.persist(maintenance);
            tx.commit();
            return maintenance;
        }
    }

    // -------------------------
    // READ
    // -------------------------
    public Maintenance getMaintenanceById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.find(Maintenance.class, id);
        }
    }

    public List<Maintenance> getAllMaintenanceByCarId(Long carId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "FROM Maintenance m WHERE m.carMaintenance.id = :carId", Maintenance.class)
                    .setParameter("carId", carId)
                    .list();
        }
    }

    // -------------------------
    // UPDATE
    // -------------------------
    public Maintenance updateMaintenance(int maintenanceId, String description, MaintenanceType type, Car carMaintenance) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            Maintenance maintenance = session.find(Maintenance.class, maintenanceId);
            if (maintenance != null) {
                maintenance.setDescription(description);
                maintenance.setType(type);
                session.merge(maintenance);
            }

            tx.commit();
            return maintenance;
        }
    }

    // -------------------------
    // DELETE
    // -------------------------
    public boolean deleteMaintenance(Long maintenanceId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            Maintenance maintenance = session.find(Maintenance.class, maintenanceId);
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
