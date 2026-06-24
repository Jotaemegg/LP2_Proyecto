CREATE DATABASE IF NOT EXISTS db_cafeorigen;
USE db_cafeorigen;

CREATE TABLE rol (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    estado INT NOT NULL DEFAULT 1,
    id_rol INT NOT NULL,
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);

CREATE TABLE empleado (
    id_empleado INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    cargo VARCHAR(100) NOT NULL,
    sueldo DECIMAL(10,2) NOT NULL,
    fecha_contratacion DATE NOT NULL,
    estado INT NOT NULL DEFAULT 1,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

CREATE TABLE categoria (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(80) NOT NULL,
    estado INT NOT NULL DEFAULT 1
);

CREATE TABLE proveedor (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    razon_social VARCHAR(120) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    estado INT NOT NULL DEFAULT 1
);

CREATE TABLE producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    id_categoria INT,
    id_proveedor INT,
    estado INT NOT NULL DEFAULT 1,
    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria),
    FOREIGN KEY (id_proveedor) REFERENCES proveedor(id_proveedor)
);

INSERT INTO rol (nombre) VALUES
('ADMINISTRADOR'),
('RECEPCIONISTA'),
('BARISTA'),
('GERENTE');

INSERT INTO usuario (email, password, estado, id_rol) VALUES
('admin@cafeorigen.pe', 'admin123', 1, 1),
('sofia.recepcion@cafeorigen.pe', 'recepcion123', 1, 2),
('juan.barista@cafeorigen.pe', 'barista123', 1, 3),
('maria.barista@cafeorigen.pe', 'barista123', 1, 3),
('pedro.recepcion@cafeorigen.pe', 'recepcion123', 1, 2);

INSERT INTO empleado (id_usuario, nombre, cargo, sueldo, fecha_contratacion, estado) VALUES
(1, 'Administrador Origen', 'Gerente General', 5500.00, '2025-01-15', 1),
(2, 'Sofia Recepcion', 'Recepcionista', 1500.00, '2025-04-10', 1),
(3, 'Juan Barista', 'Barista Principal', 1600.00, '2025-03-20', 1),
(4, 'Maria Barista', 'Barista', 1400.00, '2025-05-01', 1),
(5, 'Pedro Recepcion', 'Recepcionista', 1500.00, '2025-04-12', 1);

INSERT INTO categoria (descripcion) VALUES
('Bebidas Calientes'),
('Bebidas Frías'),
('Café en Grano'),
('Postres');

INSERT INTO proveedor (razon_social, telefono, email) VALUES
('Cafés del Valle S.A.C.', '987111222', 'ventas@cafesdelvalle.pe'),
('Distribuidora Andina E.I.R.L.', '987333444', 'contacto@andina.pe'),
('Insumos Premium S.A.', '987555666', 'info@insumospremium.pe');

INSERT INTO producto (nombre, precio, stock, id_categoria, id_proveedor, estado) VALUES
('Espresso Doble (Origen Cusco)', 7.50, 100, 1, 1, 1),
('Capuccino Vainilla', 9.50, 80, 1, 1, 1),
('Cold Brew Especial', 11.00, 50, 2, 2, 1),
('Café en Grano Villa Rica 250g', 32.00, 30, 3, 1, 1),
('Café en Grano Cusco Premium 250g', 38.00, 25, 3, 1, 1),
('Torta de Chocolate de la Casa', 12.50, 15, 4, 3, 1),
('Muffin de Arándanos', 6.50, 20, 4, 3, 1);
