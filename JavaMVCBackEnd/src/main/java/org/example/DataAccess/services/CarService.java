package org.example.DataAccess.services;

import org.example.Domain.models.Car;
import org.example.Domain.models.User;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.Collections;
import java.util.List;

public class CarService {
    private final SessionFactory sessionFactory;

    public CarService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // -------------------------
    // CREATE
    // -------------------------
    public Car createCar(String make, String model, int year, Long ownerId) {
        try (Session session = sessionFactory.openSession()) {
            User owner = null;
            if (ownerId != null) {
                owner = session.find(User.class, ownerId);
            }

            if (owner == null) {
                String message = String.format("Owner not found for id=%s. Aborting createCar.", ownerId);
                System.out.println(message);
                throw new IllegalArgumentException("Owner not found");
            }

            Transaction tx = session.beginTransaction();

            Car car = new Car();
            car.setMake(make);
            car.setModel(model);
            car.setYear(year);
            car.setOwner(owner);

            session.persist(car);
            tx.commit();

            // Ensure owner is initialized on the returned entity
            Hibernate.initialize(car.getOwner());
            return car;
        } catch (Exception e) {
            String message = String.format("An error occurred when processing: %s. Details: %s", "createCar", e);
            System.out.println(message);
            throw e;
        }
    }

    // -------------------------
    // READ
    // -------------------------
    public Car getCarById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Car car = session.find(Car.class, id);
            if (car != null) Hibernate.initialize(car.getOwner());
            return car;
        } catch (Exception e) {
            String message = String.format("An error occurred when processing: %s. Details: %s", "getCarById", e);
            System.out.println(message);
            throw e;
        }
    }

    public List<Car> getAllCars() {
        try (Session session = sessionFactory.openSession()) {
            List<Car> cars = session.createQuery("FROM Car", Car.class).list();
            cars.forEach(car -> {
                try { Hibernate.initialize(car.getOwner()); } catch (Exception ignored) {}
            });
            return cars;
        } catch (Exception e) {
            String message = String.format("An error occurred when processing: %s. Details: %s", "getAllCars", e);
            System.out.println(message);
            throw e;
        }
    }

    // New: list cars by user id (filters by owner.id)
    public List<Car> getCarsByUserId(Long userId) {
        if (userId == null) return Collections.emptyList();
        try (Session session = sessionFactory.openSession()) {
            List<Car> cars = session.createQuery("FROM Car c WHERE c.owner.id = :uid", Car.class)
                    .setParameter("uid", userId)
                    .list();
            cars.forEach(car -> {
                try { Hibernate.initialize(car.getOwner()); } catch (Exception ignored) {}
            });
            return cars;
        } catch (Exception e) {
            String message = String.format("An error occurred when processing: %s. Details: %s", "getCarsByUserId", e);
            System.out.println(message);
            throw e;
        }
    }

    public List<Car> getCarsByUser(User user) {
        if (user == null) return Collections.emptyList();
        try (Session session = sessionFactory.openSession()) {
            List<Car> cars = session.createQuery("FROM Car WHERE owner = :owner", Car.class)
                    .setParameter("owner", user)
                    .list();
            cars.forEach(car -> {
                try { Hibernate.initialize(car.getOwner()); } catch (Exception ignored) {}
            });
            return cars;
        } catch (Exception e) {
            String message = String.format("An error occurred when processing: %s. Details: %s", "getCarsByUser", e);
            System.out.println(message);
            throw e;
        }
    }

    // -------------------------
    // UPDATE
    // -------------------------
    public Car updateCar(Long carId, String make, String model, int year) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            Car car = session.find(Car.class, carId);
            if (car != null) {
                car.setMake(make);
                car.setModel(model);
                car.setYear(year);
                session.merge(car);

                // Initialize owner
                Hibernate.initialize(car.getOwner());
            }

            tx.commit();
            return car;
        } catch (Exception e) {
            String message = String.format("An error occurred when processing: %s. Details: %s", "updateCar", e);
            System.out.println(message);
            throw e;
        }
    }

    // -------------------------
    // DELETE
    // -------------------------
    public boolean deleteCar(Long carId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            Car car = session.find(Car.class, carId);
            if (car != null) {
                session.remove(car);
                tx.commit();
                return true;
            }

            tx.rollback();
            return false;
        } catch (Exception e) {
            String message = String.format("An error occurred when processing: %s. Details: %s", "deleteCar", e);
            System.out.println(message);
            throw e;
        }
    }
}
