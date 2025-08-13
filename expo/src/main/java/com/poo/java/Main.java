// Declaración del paquete donde se encuentra esta clase
package com.poo.java;

// ====== Importaciones ======
// Swing para cuadros de diálogo (UI sencilla)
import javax.swing.JOptionPane;
// Números decimales precisos para manejar dinero sin errores de coma flotante
import java.math.BigDecimal;
// Estrategias de redondeo (usamos HALF_UP)
import java.math.RoundingMode;
// API JDBC para conexión, consultas y resultados con la BD
import java.sql.*;
// Formateo de fecha/hora para mostrar en reportes y listados
import java.text.SimpleDateFormat;
// Estructuras de datos para listas dinámicas
import java.util.ArrayList;
import java.util.List;

/**
 * Sistema de Gestión de Restaurante "Delicias Gourmet".
 * <p>
 * Esta clase implementa una aplicación de escritorio simple basada en diálogos
 * para gestionar clientes, platillos y pedidos, con reportes básicos.
 * 
 * <h2>Buenas prácticas aplicadas</h2>
 * <ul>
 *   <li>Uso de BigDecimal para importes monetarios (precisión).</li>
 *   <li>Validaciones centralizadas para entrada numérica, emails y texto.</li>
 *   <li>Transacciones en la creación de pedidos (todo-o-nada).</li>
 *   <li>try-with-resources en todo acceso a BD para liberar recursos.</li>
 *   <li>SQL parametrizado (PreparedStatement), evitando inyección SQL.</li>
 *   <li>Métodos utilitarios para evitar repetición y mejorar la legibilidad.</li>
 * </ul>
 */
public class Main {

    // ========= Constantes y utilidades comunes =========

    /** Formateador de fecha/hora para imprimir valores legibles al usuario. */
    private static final SimpleDateFormat FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    /** Patrón básico para validar correos electrónicos desde la UI. */
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    // ========= Utilidades de UI =========

    /**
     * Muestra un diálogo de información con un mensaje simple.
     * @param msg texto a mostrar
     */
    private static void info(String msg) { JOptionPane.showMessageDialog(null, msg); }

    /**
     * Muestra un diálogo de confirmación Sí/No.
     * @param msg pregunta a confirmar
     * @return true si el usuario selecciona "Sí"; false en caso contrario
     */
    private static boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(null, msg, "Confirmar", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    /**
     * Solicita al usuario una cadena no vacía.
     * Repite hasta que ingrese algo o cancele.
     * @param prompt etiqueta que se muestra en el diálogo
     * @return texto ingresado, o null si el usuario cancela
     */
    private static String readNonEmpty(String prompt) {
        while (true) {
            String s = JOptionPane.showInputDialog(prompt); // muestra input
            if (s == null) return null;                    // usuario canceló
            s = s.trim();                                  // recorta espacios
            if (!s.isEmpty()) return s;                    // si hay valor, lo devuelve
            info("El valor no puede estar vacío.");       // feedback y repetir
        }
    }

    /**
     * Solicita al usuario un email válido según EMAIL_REGEX.
     * Repite hasta que ingrese un formato correcto o cancele.
     * @param prompt etiqueta del diálogo
     * @return email válido, o null si cancela
     */
    private static String readEmail(String prompt) {
        while (true) {
            String s = JOptionPane.showInputDialog(prompt);
            if (s == null) return null;              // cancelado
            s = s.trim();
            if (s.matches(EMAIL_REGEX)) return s;    // válido
            info("Correo electrónico inválido.");   // pedir de nuevo
        }
    }

    /**
     * Solicita un entero mayor o igual que un mínimo.
     * @param prompt texto a mostrar
     * @param min valor mínimo aceptado (inclusive)
     * @return entero ingresado, o null si cancela
     */
    private static Integer readInt(String prompt, int min) {
        while (true) {
            String s = JOptionPane.showInputDialog(prompt);
            if (s == null) return null; // cancelado
            try {
                int v = Integer.parseInt(s.trim()); // intenta parsear
                if (v >= min) return v;             // valida rango
                info("Debe ser un entero >= " + min + ".");
            } catch (NumberFormatException e) {
                info("Ingrese un número entero válido.");
            }
        }
    }

    /**
     * Solicita un importe monetario > 0 y fija 2 decimales con HALF_UP.
     * @param prompt texto a mostrar
     * @return BigDecimal positivo con escala 2, o null si cancela
     */
    private static BigDecimal readMoney(String prompt) {
        while (true) {
            String s = JOptionPane.showInputDialog(prompt);
            if (s == null) return null; // cancelado
            try {
                BigDecimal v = new BigDecimal(s.trim()).setScale(2, RoundingMode.HALF_UP);
                if (v.compareTo(BigDecimal.ZERO) > 0) return v; // > 0
                info("El precio debe ser mayor que 0.");
            } catch (NumberFormatException e) {
                info("Ingrese un número válido (por ejemplo 129.90).");
            }
        }
    }

    // ========= Punto de entrada =========

    /**
     * Método principal. Abre la conexión a BD, crea tablas si faltan y
     * muestra el menú principal hasta que el usuario decida salir.
     * @param args no utilizado
     */
    public static void main(String[] args) {
        Connection conn = null; // referencia a la conexión JDBC
        try {
            // Obtiene la conexión desde una clase auxiliar (no mostrada aquí)
            conn = DatabaseConnection.getConnection();
            // Crea las tablas y restricciones si no existen
            initializeDatabase(conn);

            boolean salir = false; // bandera para cerrar la app
            while (!salir) {
                // Opciones del menú principal
                String[] opciones = {
                        "Gestionar Clientes",
                        "Gestionar Platillos",
                        "Gestionar Pedidos",
                        "Reportes",
                        "Salir"
                };
                // Muestra un diálogo de opciones y devuelve el índice elegido
                int opcion = JOptionPane.showOptionDialog(
                        null,
                        "Seleccione una opción:",
                        "Sistema de Gestión de Restaurante 'Delicias Gourmet'",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        opciones,
                        opciones[0]
                );

                // Enrutamiento según elección del usuario
                switch (opcion) {
                    case 0 -> menuClientes(conn);
                    case 1 -> menuPlatillos(conn);
                    case 2 -> menuPedidos(conn);
                    case 3 -> menuReportes(conn);
                    case 4, -1 -> { // "Salir" o cerrar diálogo (X)
                        salir = true;
                        info("¡Gracias por usar el sistema de Delicias Gourmet!");
                    }
                }
            }
        } catch (SQLException e) { // errores de BD (conexión/consultas)
            info("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {     // errores inesperados
            info("Error inesperado: " + e.getMessage());
        } finally {
            // Intenta cerrar la conexión globalmente
            try { DatabaseConnection.closeConnection(); } catch (Exception ignored) {}
        }
    }

    // ========= Inicialización de esquema/tablas =========

    /**
     * Crea tablas si no existen. Define claves, checks y relaciones básicas.
     * @param conn conexión activa a la base de datos
     * @throws SQLException si ocurre algún error SQL
     */
    private static void initializeDatabase(Connection conn) throws SQLException {
        // DDL para tabla clientes: id, nombre obligatorio y correo único validado
        String createClientes = """
                CREATE TABLE IF NOT EXISTS clientes (
                   id SERIAL PRIMARY KEY,
                   nombre VARCHAR(255) NOT NULL CHECK (length(btrim(nombre))>0),
                   correo VARCHAR(255) NOT NULL UNIQUE CHECK (correo ~ '^[A-Za-z0-9._%%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$')
                )""";

        // DDL para tabla platillos: id, nombre único y precio positivo
        String createPlatillos = """
                CREATE TABLE IF NOT EXISTS platillos (
                   id SERIAL PRIMARY KEY,
                   nombre VARCHAR(255) NOT NULL UNIQUE CHECK (length(btrim(nombre))>0),
                   precio NUMERIC(10,2) NOT NULL CHECK (precio > 0)
                )""";

        // DDL para pedidos: referencia a cliente, fecha por defecto y total
        String createPedidos = """
                CREATE TABLE IF NOT EXISTS pedidos (
                   id SERIAL PRIMARY KEY,
                   cliente_id INT NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
                   fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                   total NUMERIC(10,2) NOT NULL DEFAULT 0
                )""";

        // DDL para líneas del pedido (relación muchos-a-muchos con platillos)
        String createPedidosPlatillos = """
                CREATE TABLE IF NOT EXISTS pedidos_platillos (
                   id SERIAL PRIMARY KEY,
                   pedido_id INT NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
                   platillo_id INT NOT NULL REFERENCES platillos(id) ON DELETE CASCADE,
                   cantidad INT NOT NULL CHECK (cantidad > 0),
                   precio_unitario NUMERIC(10,2) NOT NULL CHECK (precio_unitario > 0)
                )""";

        // Ejecuta los DDL en orden
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(createClientes);
            st.executeUpdate(createPlatillos);
            st.executeUpdate(createPedidos);
            st.executeUpdate(createPedidosPlatillos);
        }
    }

    // ========= Menús secundarios =========

    /**
     * Menú de gestión de clientes (CRUD).
     */
    private static void menuClientes(Connection conn) throws SQLException {
        int opcion = -1; // valor fuera de rango para entrar al bucle
        do {
            String input = JOptionPane.showInputDialog("""
                    ===== GESTIÓN DE CLIENTES =====
                    1. Registrar Cliente
                    2. Ver Clientes
                    3. Editar Cliente
                    4. Eliminar Cliente
                    0. Volver al Menú Principal""");
            if (input == null) return; // cancelar vuelve al menú principal
            try { opcion = Integer.parseInt(input.trim()); } catch (NumberFormatException e) { opcion = -1; }
            switch (opcion) {
                case 1 -> registrarCliente(conn);
                case 2 -> verClientes(conn);
                case 3 -> editarCliente(conn);
                case 4 -> eliminarCliente(conn);
                case 0 -> info("Volviendo al menú principal");
                default -> info("Opción no válida");
            }
        } while (opcion != 0);
    }

    /**
     * Menú de gestión de platillos (CRUD).
     */
    private static void menuPlatillos(Connection conn) throws SQLException {
        int opcion = -1;
        do {
            String input = JOptionPane.showInputDialog("""
                    ===== GESTIÓN DE PLATILLOS =====
                    1. Registrar Platillo
                    2. Ver Platillos
                    3. Editar Platillo
                    4. Eliminar Platillo
                    0. Volver al Menú Principal""");
            if (input == null) return;
            try { opcion = Integer.parseInt(input.trim()); } catch (NumberFormatException e) { opcion = -1; }
            switch (opcion) {
                case 1 -> registrarPlatillo(conn);
                case 2 -> verPlatillos(conn);
                case 3 -> editarPlatillo(conn);
                case 4 -> eliminarPlatillo(conn);
                case 0 -> info("Volviendo al menú principal");
                default -> info("Opción no válida");
            }
        } while (opcion != 0);
    }

    /**
     * Menú de gestión de pedidos (crear, listar, ver detalle y eliminar).
     */
    private static void menuPedidos(Connection conn) throws SQLException {
        boolean volver = false;
        while (!volver) {
            String[] opciones = {
                    "Crear Pedido",
                    "Ver Pedidos",
                    "Ver Detalle de Pedido",
                    "Eliminar Pedido",
                    "Volver al Menú Principal"
            };
            int opcion = JOptionPane.showOptionDialog(
                    null, "Seleccione una opción:", "Gestión de Pedidos",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, opciones, opciones[0]
            );
            switch (opcion) {
                case 0 -> crearPedido(conn);
                case 1 -> verPedidos(conn);
                case 2 -> verDetallePedido(conn);
                case 3 -> eliminarPedido(conn);
                case 4, -1 -> volver = true; // salir del submenú
            }
        }
    }

    /**
     * Menú de reportes (totales y pedidos por cliente).
     */
    private static void menuReportes(Connection conn) throws SQLException {
        boolean volver = false;
        while (!volver) {
            String[] opciones = {
                    "Total a Pagar por Pedido",
                    "Pedidos por Cliente",
                    "Volver al Menú Principal"
            };
            int opcion = JOptionPane.showOptionDialog(
                    null, "Seleccione una opción:", "Reportes",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, opciones, opciones[0]
            );
            switch (opcion) {
                case 0 -> reporteTotalPedidos(conn);
                case 1 -> reportePedidosPorCliente(conn);
                case 2, -1 -> volver = true;
            }
        }
    }

    // ========= Gestión de Clientes =========

    /**
     * Inserta un cliente validando nombre y correo único.
     */
    private static void registrarCliente(Connection conn) throws SQLException {
        String nombre = readNonEmpty("Nombre del cliente:");
        if (nombre == null) return; // cancelado
        String correo = readEmail("Correo electrónico:");
        if (correo == null) return; // cancelado

        String sql = "INSERT INTO clientes(nombre, correo) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());                   // nombre normalizado
            ps.setString(2, correo.trim().toLowerCase());     // correo en minúsculas
            ps.executeUpdate();                               // ejecuta inserción
            info("Cliente registrado correctamente.");
        } catch (SQLException e) {
            // Manejo específico para violación de UNIQUE (correo repetido)
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
                info("Ya existe un cliente con ese correo.");
            } else {
                throw e; // propaga otros errores SQL
            }
        }
    }

    /**
     * Lista todos los clientes ordenados por id.
     */
    private static void verClientes(Connection conn) throws SQLException {
        String sql = "SELECT id, nombre, correo FROM clientes ORDER BY id";
        StringBuilder sb = new StringBuilder("===== CLIENTES =====\n");
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            boolean any = false; // marca si hubo filas
            while (rs.next()) {
                any = true;
                sb.append(String.format("ID: %d | Nombre: %s | Correo: %s%n",
                        rs.getInt("id"), rs.getString("nombre"), rs.getString("correo")));
            }
            if (!any) { info("No hay clientes registrados."); return; }
            info(sb.toString()); // muestra listado
        }
    }

    /**
     * Edita nombre y correo de un cliente existente.
     */
    private static void editarCliente(Connection conn) throws SQLException {
        Integer id = readInt("ID del cliente a editar:", 1);
        if (id == null) return; // cancelado
        if (!clienteExiste(conn, id)) { info("El cliente no existe."); return; }

        String nombre = readNonEmpty("Nuevo nombre:");
        if (nombre == null) return;
        String correo = readEmail("Nuevo correo electrónico:");
        if (correo == null) return;

        String sql = "UPDATE clientes SET nombre = ?, correo = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ps.setString(2, correo.trim().toLowerCase());
            ps.setInt(3, id);
            int rows = ps.executeUpdate();
            info(rows > 0 ? "Cliente actualizado correctamente." : "Cliente no encontrado.");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
                info("Ya existe un cliente con ese correo.");
            } else {
                throw e;
            }
        }
    }

    /**
     * Elimina un cliente. Si tiene pedidos, avisa que también se eliminarán (cascade).
     */
    private static void eliminarCliente(Connection conn) throws SQLException {
        Integer id = readInt("ID del cliente a eliminar:", 1);
        if (id == null) return;
        if (!clienteExiste(conn, id)) { info("El cliente no existe."); return; }

        // Chequea si el cliente tiene pedidos (solo por UX; la BD ya tiene ON DELETE CASCADE)
        String check = "SELECT COUNT(*) FROM pedidos WHERE cliente_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    if (!confirm("Este cliente tiene pedidos. Se eliminarán también. ¿Continuar?")) return;
                }
            }
        }

        String sql = "DELETE FROM clientes WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            info(rows > 0 ? "Cliente eliminado correctamente." : "Cliente no encontrado.");
        }
    }

    /**
     * Verifica existencia de un cliente por id.
     */
    private static boolean clienteExiste(Connection conn, int clienteId) throws SQLException {
        String sql = "SELECT 1 FROM clientes WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    // ========= Gestión de Platillos =========

    /**
     * Inserta un platillo validando nombre único y precio > 0.
     */
    private static void registrarPlatillo(Connection conn) throws SQLException {
        String nombre = readNonEmpty("Nombre del platillo:");
        if (nombre == null) return;

        // Valida que no exista otro con el mismo nombre (case-insensitive)
        if (platilloNombreExiste(conn, nombre, null)) {
            info("Ya existe un platillo con ese nombre.");
            return;
        }

        BigDecimal precio = readMoney("Precio del platillo:");
        if (precio == null) return;

        String sql = "INSERT INTO platillos(nombre, precio) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ps.setBigDecimal(2, precio);
            ps.executeUpdate();
            info("Platillo registrado correctamente.");
        }
    }

    /**
     * Lista todos los platillos ordenados por id.
     */
    private static void verPlatillos(Connection conn) throws SQLException {
        String sql = "SELECT id, nombre, precio FROM platillos ORDER BY id";
        StringBuilder sb = new StringBuilder("===== PLATILLOS =====\n");
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            boolean any = false;
            while (rs.next()) {
                any = true;
                sb.append(String.format("ID: %d | %s | $%s%n",
                        rs.getInt("id"), rs.getString("nombre"),
                        rs.getBigDecimal("precio").setScale(2, RoundingMode.HALF_UP).toPlainString()));
            }
            if (!any) { info("No hay platillos registrados."); return; }
            info(sb.toString());
        }
    }

    /**
     * Edita un platillo existente (nombre único y precio válido).
     */
    private static void editarPlatillo(Connection conn) throws SQLException {
        Integer id = readInt("ID del platillo a editar:", 1);
        if (id == null) return;
        if (!platilloExiste(conn, id)) { info("El platillo no existe."); return; }

        String nombre = readNonEmpty("Nuevo nombre:");
        if (nombre == null) return;

        if (platilloNombreExiste(conn, nombre, id)) {
            info("Ya existe otro platillo con ese nombre.");
            return;
        }

        BigDecimal precio = readMoney("Nuevo precio:");
        if (precio == null) return;

        String sql = "UPDATE platillos SET nombre = ?, precio = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ps.setBigDecimal(2, precio);
            ps.setInt(3, id);
            int rows = ps.executeUpdate();
            info(rows > 0 ? "Platillo actualizado correctamente." : "Platillo no encontrado.");
        }
    }

    /**
     * Elimina un platillo. Si está en pedidos, avisa que se quitará de ellos.
     */
    private static void eliminarPlatillo(Connection conn) throws SQLException {
        Integer id = readInt("ID del platillo a eliminar:", 1);
        if (id == null) return;
        if (!platilloExiste(conn, id)) { info("El platillo no existe."); return; }

        // Revisa si el platillo aparece en pedidos para advertir al usuario
        String check = "SELECT COUNT(*) FROM pedidos_platillos WHERE platillo_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    if (!confirm("Este platillo está en pedidos. Se eliminará de ellos. ¿Continuar?")) return;
                }
            }
        }

        String sql = "DELETE FROM platillos WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            info(rows > 0 ? "Platillo eliminado correctamente." : "Platillo no encontrado.");
        }
    }

    /**
     * Verifica existencia de un platillo por id.
     */
    private static boolean platilloExiste(Connection conn, int id) throws SQLException {
        String sql = "SELECT 1 FROM platillos WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    /**
     * Verifica si existe un platillo con el mismo nombre. Permite excluir un id (para edición).
     * @param nombre nombre a verificar (case-insensitive)
     * @param excluirId id del platillo a excluir (o null si es inserción)
     */
    private static boolean platilloNombreExiste(Connection conn, String nombre, Integer excluirId) throws SQLException {
        String sql = excluirId == null
                ? "SELECT 1 FROM platillos WHERE LOWER(nombre)=LOWER(?)"
                : "SELECT 1 FROM platillos WHERE LOWER(nombre)=LOWER(?) AND id<>?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            if (excluirId != null) ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    // ========= Gestión de Pedidos =========

    /**
     * Crea un pedido dentro de una transacción:
     * <ol>
     *   <li>Inserta cabecera del pedido y recupera el id generado.</li>
     *   <li>Inserta líneas de platillos seleccionadas por el usuario.</li>
     *   <li>Calcula y actualiza el total del pedido.</li>
     * </ol>
     * Si el usuario no agrega platillos, se cancela el pedido (rollback suave).
     */
    private static void crearPedido(Connection conn) throws SQLException {
        // Pre-chequeos para UX: asegurar que existan clientes y platillos
        if (count(conn, "SELECT COUNT(*) FROM clientes") == 0) { info("No hay clientes. Registra uno primero."); return; }
        if (count(conn, "SELECT COUNT(*) FROM platillos") == 0) { info("No hay platillos. Registra alguno primero."); return; }

        // Muestra listado de clientes disponibles (id y nombre)
        StringBuilder listado = new StringBuilder("Clientes disponibles:\n");
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, nombre FROM clientes ORDER BY id")) {
            while (rs.next()) listado.append(rs.getInt("id")).append(". ").append(rs.getString("nombre")).append("\n");
        }
        // Solicita el id del cliente
        Integer clienteId = readInt(listado + "\nIngrese el ID del cliente:", 1);
        if (clienteId == null) return; // cancelado
        if (!clienteExiste(conn, clienteId)) { info("El cliente no existe."); return; }

        // Inicia transacción manual
        boolean prevAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        Integer pedidoId = null;           // id de cabecera
        BigDecimal total = BigDecimal.ZERO; // total acumulado

        try {
            // Inserta cabecera del pedido y recupera el id generado (PostgreSQL)
            String insPedido = "INSERT INTO pedidos(cliente_id) VALUES (?) RETURNING id";
            try (PreparedStatement ps = conn.prepareStatement(insPedido)) {
                ps.setInt(1, clienteId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) pedidoId = rs.getInt(1);
                }
            }
            if (pedidoId == null) throw new SQLException("No se pudo crear el pedido.");

            List<Integer> yaAgregados = new ArrayList<>(); // guarda ids de platillos ya añadidos

            // Bucle para agregar líneas de platillos hasta que el usuario termine
            while (true) {
                // Construye listado de platillos con precios
                StringBuilder sbPlat = new StringBuilder("Platillos disponibles:\n");
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT id, nombre, precio FROM platillos ORDER BY id")) {
                    while (rs.next()) {
                        sbPlat.append(rs.getInt("id")).append(". ")
                              .append(rs.getString("nombre")).append(" - $")
                              .append(rs.getBigDecimal("precio").setScale(2, RoundingMode.HALF_UP)).append("\n");
                    }
                }
                // Pide id del platillo; 0 significa terminar
                Integer platilloId = readInt(sbPlat + "\nAgregados: " + yaAgregados +
                        "\nIngrese ID del platillo (0 para terminar):", 0);
                if (platilloId == null) { // cancel => aborta pedido
                    throw new SQLException("Operación cancelada por el usuario.");
                }
                if (platilloId == 0) break; // fin de selección
                if (!platilloExiste(conn, platilloId)) { info("El platillo no existe."); continue; }

                // Pide cantidad del platillo
                Integer cantidad = readInt("Cantidad:", 1);
                if (cantidad == null) { throw new SQLException("Operación cancelada por el usuario."); }

                // Obtiene precio unitario actual del platillo
                BigDecimal precio = getPlatilloPrecio(conn, platilloId);
                if (precio == null) { throw new SQLException("No se pudo obtener el precio del platillo."); }

                // Inserta línea en pedidos_platillos
                String insDet = "INSERT INTO pedidos_platillos(pedido_id, platillo_id, cantidad, precio_unitario) " +
                                "VALUES (?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(insDet)) {
                    ps.setInt(1, pedidoId);
                    ps.setInt(2, platilloId);
                    ps.setInt(3, cantidad);
                    ps.setBigDecimal(4, precio);
                    ps.executeUpdate();
                }

                yaAgregados.add(platilloId); // marca como agregado
                total = total.add(precio.multiply(BigDecimal.valueOf(cantidad))); // acumula subtotal
            }

            // Si no se agregó ningún platillo, cancela el pedido (limpia cabecera y confirma)
            if (yaAgregados.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM pedidos WHERE id=?")) {
                    ps.setInt(1, pedidoId);
                    ps.executeUpdate();
                }
                conn.commit(); // confirma limpieza
                info("El pedido debe tener al menos un platillo. Pedido cancelado.");
                return;
            }

            // Actualiza el total en la cabecera del pedido
            try (PreparedStatement ps = conn.prepareStatement("UPDATE pedidos SET total=? WHERE id=?")) {
                ps.setBigDecimal(1, total.setScale(2, RoundingMode.HALF_UP));
                ps.setInt(2, pedidoId);
                ps.executeUpdate();
            }

            conn.commit(); // confirma toda la transacción
            info(String.format("Pedido #%d creado correctamente.%nTotal: $%s",
                    pedidoId, total.setScale(2, RoundingMode.HALF_UP).toPlainString()));
        } catch (Exception ex) {
            conn.rollback(); // revierte todos los cambios en caso de error
            throw ex;        // propaga para manejo superior
        } finally {
            conn.setAutoCommit(prevAutoCommit); // restablece modo autocommit
        }
    }

    /**
     * Obtiene el precio de un platillo por id con escala 2.
     */
    private static BigDecimal getPlatilloPrecio(Connection conn, int platilloId) throws SQLException {
        String sql = "SELECT precio FROM platillos WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, platilloId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("precio").setScale(2, RoundingMode.HALF_UP) : null;
            }
        }
    }

    /**
     * Lista pedidos mostrando id, fecha, cliente y total.
     */
    private static void verPedidos(Connection conn) throws SQLException {
        String sql = """
                SELECT p.id, p.fecha, c.nombre AS cliente, p.total
                FROM pedidos p
                JOIN clientes c ON p.cliente_id = c.id
                ORDER BY p.fecha DESC""";
        StringBuilder sb = new StringBuilder("===== PEDIDOS =====\n");
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            boolean any = false;
            while (rs.next()) {
                any = true;
                Timestamp ts = rs.getTimestamp("fecha");
                sb.append(String.format("Pedido #%d | Fecha: %s | Cliente: %s | Total: $%s%n",
                        rs.getInt("id"),
                        FMT.format(ts),
                        rs.getString("cliente"),
                        rs.getBigDecimal("total").setScale(2, RoundingMode.HALF_UP).toPlainString()));
            }
            if (!any) { info("No hay pedidos registrados."); return; }
            info(sb.toString());
        }
    }

    /**
     * Muestra detalle de un pedido: cabecera + líneas con subtotales y total.
     */
    private static void verDetallePedido(Connection conn) throws SQLException {
        Integer pedidoId = readInt("Ingrese el ID del pedido:", 1);
        if (pedidoId == null) return;

        String cab = """
                SELECT p.id, p.fecha, c.nombre AS cliente, p.total
                FROM pedidos p JOIN clientes c ON p.cliente_id = c.id
                WHERE p.id = ?""";
        try (PreparedStatement ps = conn.prepareStatement(cab)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) { info("El pedido no existe."); return; }

                Timestamp ts = rs.getTimestamp("fecha");
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("===== DETALLE DE PEDIDO #%d =====%n", pedidoId));
                sb.append(String.format("Fecha: %s%n", FMT.format(ts)));
                sb.append(String.format("Cliente: %s%n", rs.getString("cliente")));
                sb.append("\n--- PLATILLOS ---\n");

                String det = """
                        SELECT pl.nombre, pp.cantidad, pp.precio_unitario,
                               (pp.cantidad * pp.precio_unitario) AS subtotal
                        FROM pedidos_platillos pp
                        JOIN platillos pl ON pp.platillo_id = pl.id
                        WHERE pp.pedido_id = ?""";
                try (PreparedStatement pd = conn.prepareStatement(det)) {
                    pd.setInt(1, pedidoId);
                    try (ResultSet rd = pd.executeQuery()) {
                        while (rd.next()) {
                            sb.append(String.format("%s - %d x $%s = $%s%n",
                                    rd.getString("nombre"),
                                    rd.getInt("cantidad"),
                                    new BigDecimal(rd.getString("precio_unitario")).setScale(2, RoundingMode.HALF_UP),
                                    new BigDecimal(rd.getString("subtotal")).setScale(2, RoundingMode.HALF_UP)));
                        }
                    }
                }
                sb.append(String.format("%nTOTAL: $%s",
                        rs.getBigDecimal("total").setScale(2, RoundingMode.HALF_UP).toPlainString()));
                info(sb.toString());
            }
        }
    }

    /**
     * Elimina un pedido por id, previa confirmación del usuario.
     */
    private static void eliminarPedido(Connection conn) throws SQLException {
        Integer pedidoId = readInt("Ingrese el ID del pedido a eliminar:", 1);
        if (pedidoId == null) return;

        if (!exists(conn, "SELECT 1 FROM pedidos WHERE id = ?", pedidoId)) {
            info("El pedido no existe."); return;
        }
        if (!confirm("¿Está seguro de eliminar el pedido #" + pedidoId + "?")) return;

        String del = "DELETE FROM pedidos WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(del)) {
            ps.setInt(1, pedidoId);
            int rows = ps.executeUpdate();
            info(rows > 0 ? "Pedido eliminado correctamente." : "Error al eliminar el pedido.");
        }
    }

    // ========= Reportes =========

    /**
     * Reporte que lista todos los pedidos ordenados por total descendente.
     */
    private static void reporteTotalPedidos(Connection conn) throws SQLException {
        String sql = """
                SELECT p.id, p.fecha, c.nombre AS cliente, p.total
                FROM pedidos p JOIN clientes c ON p.cliente_id = c.id
                ORDER BY p.total DESC""";
        StringBuilder sb = new StringBuilder("===== REPORTE: TOTAL POR PEDIDO =====\n\n");
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            boolean any = false;
            while (rs.next()) {
                any = true;
                sb.append(String.format("Pedido #%d | Fecha: %s | Cliente: %s | Total: $%s%n",
                        rs.getInt("id"),
                        FMT.format(rs.getTimestamp("fecha")),
                        rs.getString("cliente"),
                        rs.getBigDecimal("total").setScale(2, RoundingMode.HALF_UP).toPlainString()));
            }
            if (!any) { info("No hay pedidos registrados."); return; }
            info(sb.toString());
        }
    }

    /**
     * Reporte de pedidos por cliente: número de platillos por pedido y total.
     */
    private static void reportePedidosPorCliente(Connection conn) throws SQLException {
        // Construye listado de clientes para elegir
        StringBuilder listado = new StringBuilder("Clientes disponibles:\n");
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, nombre FROM clientes ORDER BY id")) {
            boolean any = false;
            while (rs.next()) { any = true; listado.append(rs.getInt("id")).append(". ").append(rs.getString("nombre")).append("\n"); }
            if (!any) { info("No hay clientes registrados."); return; }
        }
        Integer clienteId = readInt(listado + "\nIngrese el ID del cliente:", 1);
        if (clienteId == null) return;
        if (!clienteExiste(conn, clienteId)) { info("El cliente no existe."); return; }

        String nombreCliente = getString(conn, "SELECT nombre FROM clientes WHERE id = ?", clienteId);
        String sql = """
                SELECT p.id, p.fecha, p.total, COUNT(pp.id) AS num_platillos
                FROM pedidos p
                JOIN pedidos_platillos pp ON p.id = pp.pedido_id
                WHERE p.cliente_id = ?
                GROUP BY p.id, p.fecha, p.total
                ORDER BY p.fecha DESC""";
        StringBuilder sb = new StringBuilder("===== PEDIDOS DEL CLIENTE: " + nombreCliente + " =====\n\n");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    sb.append(String.format("Pedido #%d | Fecha: %s | Platillos: %d | Total: $%s%n",
                            rs.getInt("id"),
                            FMT.format(rs.getTimestamp("fecha")),
                            rs.getInt("num_platillos"),
                            rs.getBigDecimal("total").setScale(2, RoundingMode.HALF_UP).toPlainString()));
                }
                if (!any) { info("Este cliente no tiene pedidos registrados."); return; }
                info(sb.toString());
            }
        }
    }

    // ========= Helpers de BD =========

    /**
     * Ejecuta una consulta "SELECT COUNT(*)" y devuelve el entero resultante.
     */
    private static int count(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Ejecuta un "SELECT 1 ... WHERE id = ?" para verificar existencia.
     */
    private static boolean exists(Connection conn, String sql, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    /**
     * Ejecuta un SELECT que retorna una sola columna tipo String.
     */
    private static String getString(Connection conn, String sql, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getString(1) : null; }
        }
    }
}
