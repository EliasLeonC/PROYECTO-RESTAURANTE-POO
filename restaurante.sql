-- Database: Restataurante

-- DROP DATABASE IF EXISTS "Restataurante";

CREATE DATABASE "Restataurante"
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Spanish_Mexico.1252'
    LC_CTYPE = 'Spanish_Mexico.1252'
    LOCALE_PROVIDER = 'libc'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;

	-- Ver todos los clientes
SELECT id, nombre, correo
FROM clientes
ORDER BY id;

-- Buscar cliente por ID
SELECT id, nombre, correo
FROM clientes
WHERE id = 1;

-- Buscar cliente por nombre (parcial, usando LIKE)
SELECT id, nombre, correo
FROM clientes
WHERE nombre ILIKE '%chino%';

-- Ver todos los platillos
SELECT id, nombre, precio
FROM platillos
ORDER BY id;

-- Buscar platillo por nombre
SELECT id, nombre, precio
FROM platillos
WHERE nombre ILIKE '%Agua chile%';

-- Platillos con precio mayor a $100
SELECT id, nombre, precio
FROM platillos
WHERE precio > 100
ORDER BY precio DESC;

-- Ver todos los pedidos con nombre del cliente
SELECT p.id, p.fecha, c.nombre AS cliente, p.total
FROM pedidos p
JOIN clientes c ON p.cliente_id = c.id
ORDER BY p.fecha DESC;

-- Ver pedidos de un cliente específico
SELECT p.id, p.fecha, p.total
FROM pedidos p
WHERE p.cliente_id = 1
ORDER BY p.fecha DESC;

-- Total de todos los pedidos
SELECT SUM(total) AS total_ventas
FROM pedidos;

-- Ver detalle de un pedido específico
SELECT pl.nombre AS platillo,
       pp.cantidad,
       pp.precio_unitario,
       (pp.cantidad * pp.precio_unitario) AS subtotal
FROM pedidos_platillos pp
JOIN platillos pl ON pp.platillo_id = pl.id
WHERE pp.pedido_id = 1;

-- Total de platillos vendidos por pedido
SELECT pedido_id, SUM(cantidad) AS total_platillos
FROM pedidos_platillos
GROUP BY pedido_id;

-- Clientes con más pedidos
SELECT c.nombre, COUNT(p.id) AS num_pedidos
FROM clientes c
JOIN pedidos p ON c.id = p.cliente_id
GROUP BY c.nombre
ORDER BY num_pedidos DESC;

-- Platillos más vendidos
SELECT pl.nombre, SUM(pp.cantidad) AS total_vendidos
FROM platillos pl
JOIN pedidos_platillos pp ON pl.id = pp.platillo_id
GROUP BY pl.nombre
ORDER BY total_vendidos DESC;

SELECT  p.id        AS pedido_id,
        p.fecha,
        c.id        AS cliente_id,
        c.nombre    AS cliente,
        pl.id       AS platillo_id,
        pl.nombre   AS platillo,
        pp.cantidad,
        pp.precio_unitario,
        (pp.cantidad * pp.precio_unitario) AS subtotal,
        p.total
FROM pedidos p
JOIN clientes c           ON p.cliente_id = c.id
JOIN pedidos_platillos pp ON pp.pedido_id = p.id
JOIN platillos pl         ON pl.id = pp.platillo_id
ORDER BY p.fecha DESC, p.id, pl.nombre;

