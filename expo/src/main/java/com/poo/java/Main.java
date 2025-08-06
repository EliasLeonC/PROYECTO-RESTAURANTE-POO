package com.poo.java;

import java.sql.*;
import javax.swing.JOptionPane;

/**
 * Clase principal que gestiona un sistema de registro escolar
 * con operaciones CRUD para escuelas y alumnos
 */
public class Main {

    /**
     * Método principal que muestra un menú interactivo para gestionar
     * escuelas y alumnos en la base de datos
     */
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            int opcion;

            do {
                String input = JOptionPane.showInputDialog(
                    "MENÚ PRINCIPAL\n1. Registrar Escuela\n2. Ver Escuelas\n3. Registrar Alumno\n4. Ver Alumnos\n5. Editar Alumno\n6. Eliminar Alumno\n0. Salir");
                if (input == null) {
                    DatabaseConnection.closeConnection();
                    return;
                }
                try {
                    opcion = Integer.parseInt(input);
                    switch (opcion) {
                        case 1 -> registrarEscuela(conn);
                        case 2 -> verEscuelas(conn);
                        case 3 -> registrarAlumno(conn);
                        case 4 -> verAlumnos(conn);
                        case 5 -> editarAlumno(conn);
                        case 6 -> eliminarAlumno(conn);
                        case 0 -> DatabaseConnection.closeConnection();
                        default -> JOptionPane.showMessageDialog(null, "Opción no válida");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Por favor, ingrese un número válido");
                    opcion = -1;
                }
            } while (opcion != 0);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage());
        } finally {
            try {
                DatabaseConnection.closeConnection();
            } catch (Exception e) {
                // Silently handle any closing errors
            }
                DatabaseConnection.closeConnection();
            }
        }

    /**
     * Registra una nueva escuela en la base de datos
     * @param conn Conexión a la base de datos
     */
    static void registrarEscuela(Connection conn) throws SQLException {
        String nombre = JOptionPane.showInputDialog("Nombre de la escuela:");
        String direccion = JOptionPane.showInputDialog("Dirección de la escuela:");

        String sql = "INSERT INTO escuelas(nombre, direccion) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setString(2, direccion);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Escuela registrada correctamente.");
        }
    }

    /**
     * Muestra todas las escuelas registradas en la base de datos
     * @param conn Conexión a la base de datos
     */
    static void verEscuelas(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder("--- ESCUELAS ---\n");
        String sql = "SELECT * FROM escuelas";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sb.append(String.format("ID: %d | Nombre: %s | Dirección: %s\n",
                        rs.getInt("id"), rs.getString("nombre"), rs.getString("direccion")));
            }
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    /**
     * Registra un nuevo alumno asociándolo a una escuela existente
     * @param conn Conexión a la base de datos
     */
    static void registrarAlumno(Connection conn) throws SQLException {
        String nombre = JOptionPane.showInputDialog("Nombre del alumno:");
        int edad = Integer.parseInt(JOptionPane.showInputDialog("Edad del alumno:"));
        int escuelaId = Integer.parseInt(JOptionPane.showInputDialog("ID de la escuela:"));

        String sql = "INSERT INTO alumnos(nombre, edad, escuela_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setInt(2, edad);
            stmt.setInt(3, escuelaId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Alumno registrado correctamente.");
        }
    }

    /**
     * Muestra todos los alumnos con su información y escuela asociada
     * @param conn Conexión a la base de datos
     */
    static void verAlumnos(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder("--- ALUMNOS ---\n");
        String sql = "SELECT a.id, a.nombre, a.edad, e.nombre AS escuela FROM alumnos a JOIN escuelas e ON a.escuela_id = e.id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sb.append(String.format("ID: %d | Nombre: %s | Edad: %d | Escuela: %s\n",
                        rs.getInt("id"), rs.getString("nombre"), rs.getInt("edad"), rs.getString("escuela")));
            }
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    /**
     * Actualiza la información de un alumno existente
     * @param conn Conexión a la base de datos
     */
    static void editarAlumno(Connection conn) throws SQLException {
        int id = Integer.parseInt(JOptionPane.showInputDialog("ID del alumno a editar:"));
        String nombre = JOptionPane.showInputDialog("Nuevo nombre:");
        int edad = Integer.parseInt(JOptionPane.showInputDialog("Nueva edad:"));
        int escuelaId = Integer.parseInt(JOptionPane.showInputDialog("Nuevo ID de escuela:"));

        String sql = "UPDATE alumnos SET nombre = ?, edad = ?, escuela_id = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setInt(2, edad);
            stmt.setInt(3, escuelaId);
            stmt.setInt(4, id);
            int rows = stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, rows > 0 ? "Alumno actualizado correctamente." : "Alumno no encontrado.");
        }
    }

    /**
     * Elimina un alumno de la base de datos por su ID
     * @param conn Conexión a la base de datos
     */
    static void eliminarAlumno(Connection conn) throws SQLException {
        int id = Integer.parseInt(JOptionPane.showInputDialog("ID del alumno a eliminar:"));

        String sql = "DELETE FROM alumnos WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, rows > 0 ? "Alumno eliminado correctamente." : "Alumno no encontrado.");
        }
    }
}