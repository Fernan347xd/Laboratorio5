package org.example.DataAccess.services;

import org.example.Domain.models.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class AuthService {
    private final SessionFactory sessionFactory;

    // Configurable constants for password hashing
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    public AuthService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // -------------------------
    // User Registration
    // -------------------------
    public User register(String username, String email, String password, String role) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                // Comprobar existencia dentro de la misma sesión/tx
                User existingByUsername = session.createQuery("FROM User WHERE username = :username", User.class)
                        .setParameter("username", username)
                        .uniqueResult();
                if (existingByUsername != null) {
                    System.out.println("[AuthService] register skipped: username already in use: " + username);
                    tx.rollback();
                    return null;
                }

                User existingByEmail = session.createQuery("FROM User WHERE email = :email", User.class)
                        .setParameter("email", email)
                        .uniqueResult();
                if (existingByEmail != null) {
                    System.out.println("[AuthService] register skipped: email already in use: " + email);
                    tx.rollback();
                    return null;
                }

                // Normalizar role y asignar por defecto si falta
                String normalizedRole = (role == null || role.trim().isEmpty()) ? "USER" : role.trim().toUpperCase();

                // Guardar el nuevo usuario
                String salt = generateSalt();
                String hashedPassword = hashPassword(password, salt);

                User user = new User();
                user.setUsername(username);
                user.setEmail(email);
                user.setSalt(salt);
                user.setPasswordHash(hashedPassword);
                user.setRole(normalizedRole);

                session.persist(user);
                session.flush();
                tx.commit();

                System.out.println("[AuthService] register success: " + username + " role=" + normalizedRole);
                return user;
            } catch (Exception inner) {
                if (tx != null) {
                    try { tx.rollback(); } catch (Exception ignore) {}
                }
                String message = String.format("An error occurred when processing: %s. Details: %s", "register", inner);
                System.out.println(message);
                throw inner;
            }
        }
    }

    // -------------------------
    // User Login (returns User on success, null on failure)
    // -------------------------
    public User login(String usernameOrEmail, String password) {
        try {
            User user = getUserByUsername(usernameOrEmail);

            if (user == null) {
                user = getUserByEmail(usernameOrEmail);
            }

            if (user == null) {
                return null;
            }

            String hashedInput = hashPassword(password, user.getSalt());
            boolean ok = hashedInput.equals(user.getPasswordHash()); // Comparar hashes
            if (ok) {
                // Initialize or detach as needed before returning if you want to avoid lazy issues in controller
                return user;
            } else {
                return null;
            }

        } catch (Exception e) {
            String message = String.format("An error occurred when processing: %s. Details: %s", "login", e);
            System.out.println(message);
            throw e;
        }
    }

    // -------------------------
    // Helper Queries
    // -------------------------
    public User getUserByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM User WHERE username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();
        } catch (Exception e) {
            String message = String.format("An error occurred when processing: %s. Details: %s", "getUserByUsername", e);
            System.out.println(message);
            throw e;
        }
    }

    public User getUserByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM User WHERE email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResult();
        } catch (Exception e) {
            String message = String.format("An error occurred when processing: %s. Details: %s", "getUserByEmail", e);
            System.out.println(message);
            throw e;
        }
    }

    // -------------------------
    // Password Hashing Utilities
    // -------------------------

    /**
     * Crear la salt para la contransena del usuario
     * @return String aleatorio de SALT_LENGTH bytes codificado en Base64
     */
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[SALT_LENGTH];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    /**
     * Funcion para hacer el hash de la contrasena + salt.
     * @param password El password del usuario
     * @param salt The Base64-encoded salt
     * @return Retorna un string de tipo Hash que representa la contrasena del usuario + la salt
     */
    private String hashPassword(String password, String salt) {
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); // SHA256

            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error inesperado al intentar crear el hash del usuario.", e);
        }
    }
}
