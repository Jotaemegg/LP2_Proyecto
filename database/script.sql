CREATE DATABASE IF NOT EXISTS db_cafeorigen;
USE db_cafeorigen;

DROP TABLE IF EXISTS consumo_detalle;
DROP TABLE IF EXISTS reserva_detalle;
DROP TABLE IF EXISTS reserva;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS espacio;
DROP TABLE IF EXISTS empleado;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS rol;

CREATE TABLE rol (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    id_rol INT NOT NULL,
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);

CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    direccion VARCHAR(200),
    dni VARCHAR(15) NOT NULL UNIQUE,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

CREATE TABLE empleado (
    id_empleado INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    cargo VARCHAR(100) NOT NULL,
    sueldo DECIMAL(10,2) NOT NULL,
    fecha_contratacion DATE NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

CREATE TABLE espacio (
    id_espacio INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    precio_hora DECIMAL(10,2) NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'Disponible'
);

CREATE TABLE producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    categoria VARCHAR(50) NOT NULL
);

CREATE TABLE reserva (
    id_reserva INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_pago DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
);

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

CREATE TABLE consumo_detalle (
    id_consumo INT AUTO_INCREMENT PRIMARY KEY,
    id_reserva INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_reserva) REFERENCES reserva(id_reserva) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

INSERT INTO rol (nombre) VALUES ('ADMINISTRADOR');
INSERT INTO rol (nombre) VALUES ('RECEPCIONISTA');
INSERT INTO rol (nombre) VALUES ('CLIENTE');
INSERT INTO rol (nombre) VALUES ('BARISTA');
INSERT INTO rol (nombre) VALUES ('GERENTE');

INSERT INTO usuario (email, password, id_rol) VALUES 
('admin@cafeorigen.pe', 'admin123', 1),
('juan.barista@cafeorigen.pe', 'barista123', 2),
('sofia.recepcion@cafeorigen.pe', 'recepcion123', 2),
('pedro.recepcion@cafeorigen.pe', 'recepcion123', 2),
('maria.barista@cafeorigen.pe', 'barista123', 2),
('luis.barista@cafeorigen.pe', 'barista123', 2),
('carlos@gmail.com', 'cliente123', 3),
('ana@gmail.com', 'cliente123', 3),
('jorge@gmail.com', 'cliente123', 3),
('elena@gmail.com', 'cliente123', 3),
('miguel@gmail.com', 'cliente123', 3),
('lucia@gmail.com', 'cliente123', 3);

INSERT INTO empleado (id_usuario, nombre, cargo, sueldo, fecha_contratacion) VALUES 
(1, 'Administrador Origen', 'Gerente General', 5500.00, '2025-01-15'),
(2, 'Juan Barista', 'Barista Principal', 1600.00, '2025-03-20'),
(3, 'Sofia Recepcion', 'Recepcionista Turno Manana', 1500.00, '2025-04-10'),
(4, 'Pedro Recepcion', 'Recepcionista Turno Tarde', 1500.00, '2025-04-12'),
(5, 'Maria Barista', 'Barista Asistente', 1400.00, '2025-05-01'),
(6, 'Luis Barista', 'Barista de Apoyo', 1400.00, '2025-05-05');

INSERT INTO cliente (id_usuario, nombre, telefono, direccion, dni) VALUES 
(7, 'Carlos Mendoza', '987654321', 'Av. Larco 123, Miraflores', '12345678'),
(8, 'Ana Torres', '912345678', 'Calle Las Flores 456, San Isidro', '87654321'),
(9, 'Jorge Ramirez', '923456789', 'Av. Arequipa 1020, Lince', '45678912'),
(10, 'Elena Gomez', '934567890', 'Calle Los Pinos 789, Surco', '56789123'),
(11, 'Miguel Benitez', '945678901', 'Jr. Junin 432, Centro de Lima', '67891234'),
(12, 'Lucia Herrera', '956789012', 'Av. Javier Prado 2200, San Borja', '78912345');

INSERT INTO espacio (nombre, tipo, precio_hora, estado) VALUES 
('Mesa Individual 01 - Zona Silenciosa', 'Escritorio', 8.50, 'Disponible'),
('Mesa Individual 02 - Zona Silenciosa', 'Escritorio', 8.50, 'Disponible'),
('Mesa Individual 03 - Conexion Alta Velocidad', 'Escritorio', 10.00, 'Disponible'),
('Sala de Cata & Reuniones - Cusco', 'Sala de reunion corporativa', 45.00, 'Disponible'),
('Sala de Cata & Reuniones - Jaen', 'Sala de reunion corporativa', 40.00, 'Disponible'),
('Salon Coworking Premium - Origen', 'Salon', 75.00, 'Disponible');

INSERT INTO producto (nombre, precio, stock, categoria) VALUES 
('Espresso Doble (Origen Cusco)', 7.50, 100, 'Bebidas Calientes'),
('Capuccino Vainilla', 9.50, 80, 'Bebidas Calientes'),
('Cold Brew Especial', 11.00, 50, 'Bebidas Frías'),
('Café en Grano Villa Rica 250g', 32.00, 30, 'Café en Grano'),
('Café en Grano Cusco Premium 250g', 38.00, 25, 'Café en Grano'),
('Torta de Chocolate de la Casa', 12.50, 15, 'Postres'),
('Muffin de Arándanos', 6.50, 20, 'Postres');

INSERT INTO reserva (id_cliente, total_pago) VALUES
(1, 17.00),
(2, 105.00),
(3, 181.50),
(4, 91.00),
(5, 33.00),
(6, 45.00);

INSERT INTO reserva_detalle (id_reserva, id_espacio, fecha_reserva, hora_inicio, horas_uso, subtotal) VALUES
(1, 1, '2026-06-25', 9, 2, 17.00),
(2, 4, '2026-06-26', 14, 2, 90.00),
(3, 6, '2026-06-27', 16, 2, 150.00),
(4, 5, '2026-06-28', 18, 2, 80.00),
(5, 3, '2026-06-29', 10, 2, 20.00),
(6, 4, '2026-06-30', 9, 1, 45.00);

INSERT INTO consumo_detalle (id_reserva, id_producto, cantidad, subtotal) VALUES
(2, 1, 2, 15.00),
(3, 6, 1, 12.50),
(3, 2, 2, 19.00),
(4, 3, 1, 11.00),
(5, 7, 2, 13.00);
