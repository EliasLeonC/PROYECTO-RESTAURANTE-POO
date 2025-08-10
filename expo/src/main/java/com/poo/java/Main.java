// Declaración del paquete donde se encuentra esta clase
package com.poo.java;

// Importaciones necesarias para trabajar con bases de datos y diálogos
import java.sql.*; // Importa todas las clases del paquete java.sql para trabajar con bases de datos
import javax.swing.JOptionPane; // Importa JOptionPane para mostrar diálogos gráficos

// Clase principal que gestiona un sistema de registro escolar
public class Main {

    // Método principal, punto de entrada del programa
    public static void main(String[] args) {
        Connection conn = null; // Se declara la conexión a la base de datos como null

        try {
            conn = DatabaseConnection.getConnection(); // Obtiene una conexión activa a la base de datos
            initializeDatabase(conn); // Llama al método para crear las tablas si no existen

            int opcion; // Variable para almacenar la opción seleccionada del menú

            do {
                String input = JOptionPane.showInputDialog( // Muestra el menú principal y guarda la entrada
                        "MENÚ PRINCIPAL\n1. Registrar Escuela\n2. Ver Escuelas\n3. Registrar Alumno\n4. Ver Alumnos\n5. Editar Alumno\n6. Eliminar Alumno\n0. Salir");
                if (input == null) return; // Si el usuario cierra el cuadro de diálogo, termina el programa

                try {
                    opcion = Integer.parseInt(input); // Convierte la entrada del usuario a entero

                    switch (opcion) {
                        case 1 -> registrarEscuela(conn); // Llama a registrarEscuela
                        case 2 -> verEscuelas(conn); // Llama a verEscuelas
                        case 3 -> registrarAlumno(conn); // Llama a registrarAlumno
                        case 4 -> verAlumnos(conn); // Llama a verAlumnos
                        case 5 -> editarAlumno(conn); // Llama a editarAlumno
                        case 6 -> eliminarAlumno(conn); // Llama a eliminarAlumno
                        case 0 -> JOptionPane.showMessageDialog(null, "Hasta luego"); // Muestra mensaje de salida
                        default -> JOptionPane.showMessageDialog(null, "Opción no válida"); // Muestra mensaje de error
                    }

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Por favor, ingrese un número válido"); // Si no es número válido
                    opcion = -1; // Fuerza la repetición del ciclo
                }

            } while (opcion != 0); // Repite mientras el usuario no elija salir

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error de base de datos: " + e.getMessage()); // Muestra errores SQL

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage()); // Muestra errores generales

        } finally {
            try {
                DatabaseConnection.closeConnection(); // Cierra la conexión con la base de datos
            } catch (Exception e) {
                // Ignora errores al cerrar la conexión
            }
        }
    }

    // Método que crea las tablas si no existen
    private static void initializeDatabase(Connection conn) throws SQLException {
        String createEscuelas = "CREATE TABLE IF NOT EXISTS escuelas (" +
                "id SERIAL PRIMARY KEY, " + // ID autoincremental como clave primaria
                "nombre VARCHAR(255) NOT NULL, " + // Campo obligatorio para nombre
                "direccion VARCHAR(255) NOT NULL)"; // Campo obligatorio para dirección

        String createAlumnos = "CREATE TABLE IF NOT EXISTS alumnos (" +
                "id SERIAL PRIMARY KEY, " + // ID autoincremental como clave primaria
                "nombre VARCHAR(255) NOT NULL, " + // Campo obligatorio para nombre del alumno
                "edad INT NOT NULL, " + // Campo obligatorio para edad
                "escuela_id INT REFERENCES escuelas(id) ON DELETE CASCADE)"; // Relación con escuela

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createEscuelas); // Ejecuta la creación de tabla escuelas
            stmt.executeUpdate(createAlumnos); // Ejecuta la creación de tabla alumnos
        }
    }

    // Método para registrar una nueva escuela
    static void registrarEscuela(Connection conn) throws SQLException {
        String nombre = JOptionPane.showInputDialog("Nombre de la escuela:"); // Solicita nombre
        String direccion = JOptionPane.showInputDialog("Dirección de la escuela:"); // Solicita dirección

        String sql = "INSERT INTO escuelas(nombre, direccion) VALUES (?, ?)"; // SQL para insertar datos
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre); // Asigna el nombre al primer parámetro
            stmt.setString(2, direccion); // Asigna la dirección al segundo parámetro
            stmt.executeUpdate(); // Ejecuta la inserción
            JOptionPane.showMessageDialog(null, "Escuela registrada correctamente."); // Muestra mensaje de éxito
        }
    }

    // Método para mostrar todas las escuelas
    static void verEscuelas(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder("--- ESCUELAS ---\n"); // Acumulador de texto
        String sql = "SELECT * FROM escuelas"; // Consulta para obtener todas las escuelas

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) { // Recorre el resultado fila por fila
                sb.append(String.format("ID: %d | Nombre: %s | Dirección: %s\n",
                        rs.getInt("id"), rs.getString("nombre"), rs.getString("direccion"))); // Añade a string
            }
        }
        JOptionPane.showMessageDialog(null, sb.toString()); // Muestra el resultado
    }

    // Método para registrar un nuevo alumno
    static void registrarAlumno(Connection conn) throws SQLException {
        String nombre = JOptionPane.showInputDialog("Nombre del alumno:"); // Solicita nombre
        int edad = Integer.parseInt(JOptionPane.showInputDialog("Edad del alumno:")); // Solicita edad
        int escuelaId = Integer.parseInt(JOptionPane.showInputDialog("ID de la escuela:")); // Solicita escuela

        if (!escuelaExiste(conn, escuelaId)) { // Verifica que la escuela exista
            JOptionPane.showMessageDialog(null, "La escuela indicada no existe."); // Muestra error
            return;
        }

        String sql = "INSERT INTO alumnos(nombre, edad, escuela_id) VALUES (?, ?, ?)"; // SQL para insertar alumno
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre); // Asigna nombre
            stmt.setInt(2, edad); // Asigna edad
            stmt.setInt(3, escuelaId); // Asigna ID de escuela
            stmt.executeUpdate(); // Ejecuta inserción
            JOptionPane.showMessageDialog(null, "Alumno registrado correctamente."); // Mensaje de éxito
        }
    }

    // Método para mostrar los alumnos
    static void verAlumnos(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder("--- ALUMNOS ---\n"); // Acumulador
        String sql = "SELECT a.id, a.nombre, a.edad, e.nombre AS escuela FROM alumnos a JOIN escuelas e ON a.escuela_id = e.id"; // SQL con JOIN

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
                            
            while (rs.next()) {
                sb.append(String.format("ID: %d | Nombre: %s | Edad: %d | Escuela: %s\n",
                        rs.getInt("id"), rs.getString("nombre"), rs.getInt("edad"), rs.getString("escuela"))); // Añade datos
            }
        }
        JOptionPane.showMessageDialog(null, sb.toString()); // Muestra los datos
    }

    // Método para editar los datos de un alumno
    static void editarAlumno(Connection conn) throws SQLException {
        int id = Integer.parseInt(JOptionPane.showInputDialog("ID del alumno a editar:")); // Solicita ID
        String nombre = JOptionPane.showInputDialog("Nuevo nombre:"); // Solicita nuevo nombre
        int edad = Integer.parseInt(JOptionPane.showInputDialog("Nueva edad:")); // Solicita nueva edad
        int escuelaId = Integer.parseInt(JOptionPane.showInputDialog("Nuevo ID de escuela:")); // Solicita nueva escuela

        if (!escuelaExiste(conn, escuelaId)) {
            JOptionPane.showMessageDialog(null, "La escuela indicada no existe."); // Verifica existencia
            return;
        }

        String sql = "UPDATE alumnos SET nombre = ?, edad = ?, escuela_id = ? WHERE id = ?"; // SQL para actualizar
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre); // Nuevo nombre
            stmt.setInt(2, edad); // Nueva edad
            stmt.setInt(3, escuelaId); // Nuevo ID de escuela
            stmt.setInt(4, id); // ID del alumno a actualizar
            int rows = stmt.executeUpdate(); // Ejecuta actualización
            JOptionPane.showMessageDialog(null, rows > 0 ? "Alumno actualizado correctamente." : "Alumno no encontrado."); // Resultado
        }
    }

    // Método para verificar si una escuela existe
    private static boolean escuelaExiste(Connection conn, int escuelaId) throws SQLException {
        String sql = "SELECT 1 FROM escuelas WHERE id = ?"; // SQL para buscar por ID
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, escuelaId); // Asigna ID
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Devuelve true si hay resultado
            }
        }
    }

    // Método para eliminar un alumno
    static void eliminarAlumno(Connection conn) throws SQLException {
        int id = Integer.parseInt(JOptionPane.showInputDialog("ID del alumno a eliminar:")); // Solicita ID

        String sql = "DELETE FROM alumnos WHERE id = ?"; // SQL para eliminar por ID
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id); // Asigna ID al parámetro
            int rows = stmt.executeUpdate(); // Ejecuta la eliminación
            JOptionPane.showMessageDialog(null, rows > 0 ? "Alumno eliminado correctamente." : "Alumno no encontrado."); // Resultado
        }
    }
}
