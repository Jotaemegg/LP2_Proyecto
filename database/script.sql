-- Creación de la base de datos para Café Origen (Estructura Ampliada de 9 Tablas)
CREATE DATABASE IF NOT EXISTS db_cafeorigen;
USE db_cafeorigen;

-- Borrar tablas si existen (orden correcto para evitar problemas de llaves foráneas)
DROP TABLE IF EXISTS consumo_detalle;
DROP TABLE IF EXISTS reserva_detalle;
DROP TABLE IF EXISTS reserva;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS espacio;
DROP TABLE IF EXISTS empleado;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS rol;

-- 1. Tabla: rol
CREATE TABLE rol (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Tabla: usuario (Datos de acceso común)
CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    id_rol INT NOT NULL,
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);

-- 3. Tabla: cliente (Detalle específico de clientes)
CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    direccion VARCHAR(200),
    dni VARCHAR(15) NOT NULL UNIQUE,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

-- 4. Tabla: empleado (Detalle específico del personal de la cafetería)
CREATE TABLE empleado (
    id_empleado INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    cargo VARCHAR(100) NOT NULL, -- 'Administrador', 'Barista', 'Recepcionista'
    sueldo DECIMAL(10,2) NOT NULL,
    fecha_contratacion DATE NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

-- 5. Tabla: espacio
CREATE TABLE espacio (
    id_espacio INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL, -- 'Escritorio', 'Sala de reunion corporativa', 'Salon'
    precio_hora DECIMAL(10,2) NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'Disponible' -- 'Disponible', 'Ocupado', 'Mantenimiento'
);

-- 6. Tabla: producto
CREATE TABLE producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    categoria VARCHAR(50) NOT NULL -- 'Bebidas Calientes', 'Bebidas Frías', 'Café en Grano', 'Postres'
);

-- 7. Tabla: reserva (Cabecera, asociada directamente al Cliente)
CREATE TABLE reserva (
    id_reserva INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_pago DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
);

-- 8. Tabla: reserva_detalle (Detalle del espacio)
CREATE TABLE reserva_detalle (
    id_reserva_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_reserva INT NOT NULL,
    id_espacio INT NOT NULL,
    fecha_reserva DATE NOT NULL,
    hora_inicio INT NOT NULL DEFAULT 7,
    horas_uso INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_reserva) REFERENCES reserva(id_reserva) ON DELETE CASCADE,
    FOREIGN KEY (id_espacio) REFERENCES espacio(id_espacio)
);

-- 9. Tabla: consumo_detalle (Detalle de productos consumidos)
CREATE TABLE consumo_detalle (
    id_consumo INT AUTO_INCREMENT PRIMARY KEY,
    id_reserva INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_reserva) REFERENCES reserva(id_reserva) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

-- ==========================================
-- INSERCIÓN DE DATOS DE PRUEBA (SEMILLA)
-- ==========================================

-- 1. Insertar Roles
INSERT INTO rol (nombre) VALUES ('ADMINISTRADOR');
INSERT INTO rol (nombre) VALUES ('RECEPCIONISTA');
INSERT INTO rol (nombre) VALUES ('CLIENTE');

-- 2. Insertar Usuarios
INSERT INTO usuario (email, password, id_rol) VALUES 
('admin@cafeorigen.pe', 'admin123', 1),
('juan.barista@cafeorigen.pe', 'barista123', 2),
('carlos@gmail.com', 'cliente123', 3),
('ana@gmail.com', 'cliente123', 3);

-- 3. Insertar Empleados (Asociados a sus respectivos usuarios)
INSERT INTO empleado (id_usuario, nombre, cargo, sueldo, fecha_contratacion) VALUES 
(1, 'Administrador Origen', 'Gerente General', 5500.00, '2025-01-15'),
(2, 'Juan Barista', 'Barista Principal', 1600.00, '2025-03-20');

-- 4. Insertar Clientes (Asociados a sus respectivos usuarios)
INSERT INTO cliente (id_usuario, nombre, telefono, direccion, dni) VALUES 
(3, 'Carlos Mendoza', '987654321', 'Av. Larco 123, Miraflores', '12345678'),
(4, 'Ana Torres', '912345678', 'Calle Las Flores 456, San Isidro', '87654321');

-- 5. Insertar Espacios / Salas
INSERT INTO espacio (nombre, tipo, precio_hora, estado) VALUES 
('Mesa Individual 01 - Zona Silenciosa', 'Escritorio', 8.50, 'Disponible'),
('Mesa Individual 02 - Zona Silenciosa', 'Escritorio', 8.50, 'Disponible'),
('Mesa Individual 03 - Conexion Alta Velocidad', 'Escritorio', 10.00, 'Disponible'),
('Sala de Cata & Reuniones - Cusco', 'Sala de reunion corporativa', 45.00, 'Disponible'),
('Sala de Cata & Reuniones - Jaen', 'Sala de reunion corporativa', 40.00, 'Disponible'),
('Salon Coworking Premium - Origen', 'Salon', 75.00, 'Disponible');

-- 6. Insertar Productos de Cafetería y Café en Grano
INSERT INTO producto (nombre, precio, stock, categoria) VALUES 
('Espresso Doble (Origen Cusco)', 7.50, 100, 'Bebidas Calientes'),
('Capuccino Vainilla', 9.50, 80, 'Bebidas Calientes'),
('Cold Brew Especial', 11.00, 50, 'Bebidas Frías'),
('Café en Grano Villa Rica 250g', 32.00, 30, 'Café en Grano'),
('Café en Grano Cusco Premium 250g', 38.00, 25, 'Café en Grano'),
('Torta de Chocolate de la Casa', 12.50, 15, 'Postres'),
('Muffin de Arándanos', 6.50, 20, 'Postres');
