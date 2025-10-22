package org.example.DataAccess;

import org.example.Domain.models.Car;
import org.example.Domain.models.Maintenance;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.io.IOException;
import java.util.Properties;

import org.example.Domain.models.User;

public class HibernateUtil {

    private static final SessionFactory sessionFactory;

    static {
        try {
            Properties properties = new Properties();
            properties.load(HibernateUtil.class.getClassLoader().getResourceAsStream("hibernate.properties"));

            sessionFactory = new Configuration()
                    .addProperties(properties)
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(Car.class)
                    .addAnnotatedClass(Maintenance.class)
                    // Agregar aqui otras entidades.
                    .buildSessionFactory();

        } catch (IOException e) {
            throw new ExceptionInInitializerError("Could not load hibernate.properties: " + e.getMessage());
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError("Initial SessionFactory creation failed: " + ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
