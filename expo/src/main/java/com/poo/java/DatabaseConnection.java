package com.poo.java;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Clase que gestiona la conexión a la base de datos PostgreSQL
 * Implementa un patrón Singleton para mantener una única conexión
 */
public class DatabaseConnection {
    /** URL de conexión a la base de datos PostgreSQL */
    private static final String URL = "jdbc:postgresql://localhost:5432/escuela_alumnos_db";
    /** Usuario de la base de datos */
    private static final String USER = "postgres";
    /** Contraseña de la base de datos */
    private static final String PASSWORD = "123456";
    /** Instancia única de la conexión */
    private static Connection connection = null;

    /**
     * Obtiene una conexión a la base de datos
     * Si no existe una conexión, la crea; si existe, retorna la existente
     * @return Connection objeto de conexión a la base de datos
     * @throws RuntimeException si hay error al conectar
     */
    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                throw new RuntimeException("Error al conectar con la base de datos: " + e.getMessage(), e);
            }
        }
        return connection;
    }

    /**
     * Cierra la conexión a la base de datos si está abierta
     * y libera los recursos
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
    /**
     * Método de prueba para verificar la conexión a la base de datos
     * @param args argumentos de línea de comando (no utilizados)
     */
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Conexión exitosa a la base de datos.");
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }
}
