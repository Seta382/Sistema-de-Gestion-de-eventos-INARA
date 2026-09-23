-- ==========================================
-- 1. MÓDULO DE USUARIOS Y ROLES
-- ==========================================

-- Tabla de Usuarios generales
CREATE TABLE IF NOT EXISTS public.usuario (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(150) NOT NULL,
    rol VARCHAR(50) DEFAULT 'CLIENTE', -- CLIENTE, ORGANIZADOR, ADMIN
    telefono VARCHAR(20),
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'ACTIVO'
);

-- Tabla de Clientes
CREATE TABLE IF NOT EXISTS public.cliente (
    id_cliente SERIAL PRIMARY KEY,
    id_usuario INTEGER UNIQUE NOT NULL,
    dni VARCHAR(20),
    direccion VARCHAR(150),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario)
        REFERENCES public.usuario(id) ON DELETE CASCADE
);

-- Tabla de Empleados / Organizadores
CREATE TABLE IF NOT EXISTS public.empleado (
    id_empleado SERIAL PRIMARY KEY,
    id_usuario INTEGER UNIQUE NOT NULL,
    dni VARCHAR(20),
    cargo VARCHAR(100),
    area VARCHAR(100),
    especialidad VARCHAR(100),
    CONSTRAINT fk_empleado_usuario FOREIGN KEY (id_usuario)
        REFERENCES public.usuario(id) ON DELETE CASCADE
);

-- Tabla de Proveedores
CREATE TABLE IF NOT EXISTS public.proveedor (
    id_proveedor SERIAL PRIMARY KEY,
    razon_social VARCHAR(150) NOT NULL,
    ruc VARCHAR(20) NOT NULL UNIQUE,
    correo VARCHAR(150),
    telefono VARCHAR(20),
    direccion VARCHAR(150),
    estado VARCHAR(20) DEFAULT 'ACTIVO'
);

-- ==========================================
-- 2. MÓDULO DE CITAS Y DISPONIBILIDAD
-- ==========================================

CREATE TABLE IF NOT EXISTS public.cita (
    id_cita SERIAL PRIMARY KEY,
    id_cliente INTEGER NOT NULL,
    id_empleado INTEGER,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    modalidad VARCHAR(50) DEFAULT 'PRESENCIAL', -- PRESENCIAL, VIRTUAL
    lugar VARCHAR(150),
    motivo VARCHAR(255),
    estado VARCHAR(50) DEFAULT 'PENDIENTE', -- PENDIENTE, CONFIRMADA, REPROGRAMADA, CANCELADA, FINALIZADA
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cita_cliente FOREIGN KEY (id_cliente)
        REFERENCES public.cliente(id_cliente) ON DELETE CASCADE,
    CONSTRAINT fk_cita_empleado FOREIGN KEY (id_empleado)
        REFERENCES public.empleado(id_empleado) ON DELETE SET NULL
);

-- ==========================================
-- 3. MÓDULO DE EVENTOS
-- ==========================================

CREATE TABLE IF NOT EXISTS public.tipo_evento (
    id_tipo_evento SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE, -- Boda, Quinceañero, Baby Shower, Corporativo, etc.
    descripcion TEXT
);

CREATE TABLE IF NOT EXISTS public.evento (
    id_evento SERIAL PRIMARY KEY,
    id_cliente INTEGER NOT NULL,
    id_empleado INTEGER,
    id_tipo_evento INTEGER NOT NULL,
    id_cita INTEGER UNIQUE, -- Cita origen (opcional)
    fecha_evento DATE NOT NULL,
    hora_evento TIME,
    lugar VARCHAR(200),
    num_invitados INTEGER DEFAULT 0,
    descripcion TEXT,
    estado VARCHAR(50) DEFAULT 'SOLICITADO', -- SOLICITADO, COTIZADO, CONFIRMADO, EN_PROCESO, FINALIZADO, CANCELADO
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_evento_cliente FOREIGN KEY (id_cliente)
        REFERENCES public.cliente(id_cliente) ON DELETE CASCADE,
    CONSTRAINT fk_evento_empleado FOREIGN KEY (id_empleado)
        REFERENCES public.empleado(id_empleado) ON DELETE SET NULL,
    CONSTRAINT fk_evento_tipo FOREIGN KEY (id_tipo_evento)
        REFERENCES public.tipo_evento(id_tipo_evento),
    CONSTRAINT fk_evento_cita FOREIGN KEY (id_cita)
        REFERENCES public.cita(id_cita) ON DELETE SET NULL
);

-- ==========================================
-- 4. MÓDULO DE SERVICIOS, COTIZACIONES Y ACUERDOS
-- ==========================================

-- Servicios de la promotora
CREATE TABLE IF NOT EXISTS public.servicio (
    id_servicio SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio NUMERIC(10,2) NOT NULL,
    estado VARCHAR(20) DEFAULT 'ACTIVO'
);

-- Artículos e Insumos
CREATE TABLE IF NOT EXISTS public.articulo (
    id_articulo SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio NUMERIC(10,2) NOT NULL,
    stock INTEGER DEFAULT 0,
    id_proveedor INTEGER,
    estado VARCHAR(20) DEFAULT 'ACTIVO',
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_articulo_proveedor FOREIGN KEY (id_proveedor)
        REFERENCES public.proveedor(id_proveedor) ON DELETE SET NULL
);

-- Cotización asociada a un evento
CREATE TABLE IF NOT EXISTS public.cotizacion (
    id_cotizacion SERIAL PRIMARY KEY,
    id_evento INTEGER NOT NULL,
    fecha DATE DEFAULT CURRENT_DATE,
    subtotal NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    descuento NUMERIC(10,2) DEFAULT 0.00,
    total NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(50) DEFAULT 'PENDIENTE', -- PENDIENTE, ACEPTADA, RECHAZADA, MODIFICADA
    observaciones TEXT,
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cotizacion_evento FOREIGN KEY (id_evento)
        REFERENCES public.evento(id_evento) ON DELETE CASCADE
);

-- Detalle de los servicios incluidos en la cotización
CREATE TABLE IF NOT EXISTS public.detalle_cotizacion (
    id_detalle SERIAL PRIMARY KEY,
    id_cotizacion INTEGER NOT NULL,
    id_servicio INTEGER NOT NULL,
    cantidad INTEGER DEFAULT 1,
    precio_unitario NUMERIC(10,2) NOT NULL,
    subtotal NUMERIC(10,2) NOT NULL,
    CONSTRAINT fk_detalle_cotizacion FOREIGN KEY (id_cotizacion)
        REFERENCES public.cotizacion(id_cotizacion) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_servicio FOREIGN KEY (id_servicio)
        REFERENCES public.servicio(id_servicio)
);

-- Acuerdos contractuales del evento
CREATE TABLE IF NOT EXISTS public.acuerdo (
    id_acuerdo SERIAL PRIMARY KEY,
    id_evento INTEGER NOT NULL,
    condiciones TEXT NOT NULL,
    estado VARCHAR(50) DEFAULT 'PENDIENTE', -- PENDIENTE, ACEPTADO, CANCELADO
    fecha_registro TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    fecha_confirmacion TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_acuerdo_evento FOREIGN KEY (id_evento)
        REFERENCES public.evento(id_evento) ON DELETE CASCADE
);

-- ==========================================
-- 5. MÓDULO DE PAGOS, SEGUIMIENTO Y NOTIFICACIONES
-- ==========================================

-- Gestión de pagos
CREATE TABLE IF NOT EXISTS public.pago (
    id_pago SERIAL PRIMARY KEY,
    id_evento INTEGER NOT NULL,
    monto NUMERIC(10,2) NOT NULL,
    fecha_pago TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    metodo_pago VARCHAR(50) NOT NULL, -- TRANSFERENCIA, EFECTIVO, YAPE, PLIN, TARJETA
    estado VARCHAR(50) DEFAULT 'COMPLETADO', -- PENDIENTE, COMPLETADO, RECHAZADO
    comprobante_url VARCHAR(255),
    CONSTRAINT fk_pago_evento FOREIGN KEY (id_evento)
        REFERENCES public.evento(id_evento) ON DELETE CASCADE
);

-- Bitácora de seguimiento del evento (avances)
CREATE TABLE IF NOT EXISTS public.seguimiento (
    id_seguimiento SERIAL PRIMARY KEY,
    id_evento INTEGER NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT,
    fecha_registro TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_seguimiento_evento FOREIGN KEY (id_evento)
        REFERENCES public.evento(id_evento) ON DELETE CASCADE
);

-- Notificaciones del sistema
CREATE TABLE IF NOT EXISTS public.notificacion (
    id_notificacion SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    id_evento INTEGER,
    mensaje TEXT NOT NULL,
    leida BOOLEAN DEFAULT FALSE,
    fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (id_usuario)
        REFERENCES public.usuario(id) ON DELETE CASCADE,
    CONSTRAINT fk_notificacion_evento FOREIGN KEY (id_evento)
        REFERENCES public.evento(id_evento) ON DELETE SET NULL
);
