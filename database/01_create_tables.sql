-- Creación de tablas en orden correcto para evitar errores de dependencias

-- 1️⃣ Tabla usuario
CREATE TABLE public.usuario (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(150) NOT NULL,
    rol VARCHAR(50) DEFAULT 'USER',
    telefono VARCHAR(20),
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'ACTIVO'
);

-- 2️⃣ Tabla cliente
CREATE TABLE public.cliente (
    id_cliente SERIAL PRIMARY KEY,
    id_usuario INTEGER UNIQUE,
    dni VARCHAR(20),
    direccion VARCHAR(150),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario)
        REFERENCES public.usuario(id)
        ON DELETE CASCADE
);

-- 3️⃣ Tabla empleado
CREATE TABLE public.empleado (
    id_empleado SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL UNIQUE,
    dni VARCHAR(20),
    cargo VARCHAR(100),
    area VARCHAR(100),
    CONSTRAINT fk_empleado_usuario FOREIGN KEY (id_usuario)
        REFERENCES public.usuario(id)
        ON DELETE CASCADE
);

-- 4️⃣ Tabla proveedor
CREATE TABLE public.proveedor (
    id_proveedor SERIAL PRIMARY KEY,
    razon_social VARCHAR(150) NOT NULL,
    ruc VARCHAR(20) NOT NULL UNIQUE,
    correo VARCHAR(150),
    telefono VARCHAR(20),
    direccion VARCHAR(150),
    estado VARCHAR(20) DEFAULT 'ACTIVO'
);

-- 5️⃣ Tabla artículo
CREATE TABLE public.articulo (
    id_articulo SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio NUMERIC(10,2) NOT NULL,
    stock INTEGER DEFAULT 0,
    id_proveedor INTEGER,
    estado VARCHAR(20) DEFAULT 'ACTIVO',
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_articulo_proveedor FOREIGN KEY (id_proveedor)
        REFERENCES public.proveedor(id_proveedor)
        ON DELETE SET NULL
);
