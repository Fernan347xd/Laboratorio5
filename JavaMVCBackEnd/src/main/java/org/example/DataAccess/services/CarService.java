package org.example.DataAccess.services;

import org.example.Domain.models.Car;
import org.example.Domain.models.User;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

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
            var owner = session.find(User.class, ownerId);
            Transaction tx = session.beginTransaction();

            Car car = new Car();
            car.setMake(make);
            car.setModel(model);
            car.setYear(year);
            car.setOwner(owner);

            session.persist(car);
            tx.commit();
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
            return session.find(Car.class, id);
        } catch (Exception e) {
            String message = String.format("An error occurred when processing: %s. Details: %s", "getCarById", e);
            System.out.println(message);
            throw e;
        }
    }

    public List<Car> getAllCars() {
        try (Session session = sessionFactory.openSession()) {
            List<Car> cars = session.createQuery("FROM Car", Car.class).list();
            cars.forEach(car -> Hibernate.initialize(car.getOwner())); // Incluir tambien al dueno
            return cars;
        } catch (Exception e) {
            String message = String.format("An error occurred when processing: %s. Details: %s", "getAllCars", e);
            System.out.println(message);
            throw e;
        }
    }

    public List<Car> getCarsByUser(User user) {
        try (Session session = sessionFactory.openSession()) {
            List<Car> cars = session.createQuery("FROM Car WHERE owner = :owner", Car.class)
                    .setParameter("owner", user)
                    .list();
            cars.forEach(car -> Hibernate.initialize(car.getOwner())); // Incluir tambien al dueno
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
