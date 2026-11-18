-- =====================================================================
-- INARA - Esquema completo de la base de datos (PostgreSQL / Supabase)
-- Generado a partir del esquema real exportado de Supabase.
--
-- * Se puede ejecutar completo en el SQL Editor de Supabase.
-- * Es idempotente: usa IF NOT EXISTS, así que no borra ni duplica nada
--   si las tablas ya existen. Se puede ejecutar más de una vez.
-- * Las tablas van en orden de dependencia (primero las que no
--   referencian a otras).
-- * Las columnas SERIAL crean solas las secuencias (usuario_id_seq, etc.).
-- =====================================================================

-- =====================================================================
-- 1. USUARIOS Y ROLES
-- =====================================================================

CREATE TABLE IF NOT EXISTS public.usuario (
                                              id          SERIAL PRIMARY KEY,
                                              nombre      VARCHAR NOT NULL,
                                              apellido    VARCHAR NOT NULL,
                                              correo      VARCHAR NOT NULL UNIQUE,
                                              password    VARCHAR NOT NULL,                 -- hash SHA-256 (PasswordUtil)
                                              rol         VARCHAR DEFAULT 'CLIENTE',        -- ADMIN, EMPLEADO, CLIENTE
                                              telefono    VARCHAR,
                                              creado_en   TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                              estado      VARCHAR DEFAULT 'ACTIVO'          -- ACTIVO / INACTIVO (baja lógica)
);

CREATE TABLE IF NOT EXISTS public.cliente (
                                              id_cliente  SERIAL PRIMARY KEY,
                                              id_usuario  INTEGER UNIQUE,
                                              dni         VARCHAR,
                                              direccion   VARCHAR,
                                              CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario)
    REFERENCES public.usuario(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS public.empleado (
                                               id_empleado SERIAL PRIMARY KEY,
                                               id_usuario  INTEGER NOT NULL UNIQUE,
                                               dni         VARCHAR,
                                               cargo       VARCHAR,
                                               area        VARCHAR,
                                               CONSTRAINT fk_empleado_usuario FOREIGN KEY (id_usuario)
    REFERENCES public.usuario(id) ON DELETE CASCADE
    );

-- =====================================================================
-- 2. PROVEEDORES, INSUMOS Y SERVICIOS
-- =====================================================================

CREATE TABLE IF NOT EXISTS public.proveedor (
                                                id_proveedor SERIAL PRIMARY KEY,
                                                razon_social VARCHAR NOT NULL,
                                                ruc          VARCHAR NOT NULL UNIQUE,
                                                correo       VARCHAR,
                                                telefono     VARCHAR,
                                                direccion    VARCHAR,
                                                estado       VARCHAR NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE IF NOT EXISTS public.articulo (
                                               id_articulo  SERIAL PRIMARY KEY,
                                               nombre       VARCHAR NOT NULL,
                                               descripcion  TEXT,
                                               precio       NUMERIC NOT NULL,
                                               stock        INTEGER NOT NULL DEFAULT 0,
                                               id_proveedor INTEGER,
                                               estado       VARCHAR NOT NULL DEFAULT 'ACTIVO',
                                               creado_en    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                               CONSTRAINT fk_articulo_proveedor FOREIGN KEY (id_proveedor)
    REFERENCES public.proveedor(id_proveedor) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS public.servicio (
                                               id_servicio SERIAL PRIMARY KEY,
                                               nombre      VARCHAR NOT NULL,
                                               descripcion TEXT,
                                               precio      NUMERIC NOT NULL,
                                               estado      VARCHAR DEFAULT 'ACTIVO'
);

-- =====================================================================
-- 3. CITAS Y EVENTOS
-- =====================================================================

CREATE TABLE IF NOT EXISTS public.cita (
                                           id_cita     SERIAL PRIMARY KEY,
                                           id_cliente  INTEGER NOT NULL,
                                           id_empleado INTEGER,
                                           fecha       DATE NOT NULL,
                                           hora        TIME NOT NULL,
                                           modalidad   VARCHAR DEFAULT 'PRESENCIAL',
                                           lugar       VARCHAR,
                                           motivo      VARCHAR,
                                           estado      VARCHAR DEFAULT 'PENDIENTE',
                                           creado_en   TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                           CONSTRAINT fk_cita_cliente  FOREIGN KEY (id_cliente)
    REFERENCES public.cliente(id_cliente) ON DELETE CASCADE,
    CONSTRAINT fk_cita_empleado FOREIGN KEY (id_empleado)
    REFERENCES public.empleado(id_empleado) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS public.tipo_evento (
                                                  id_tipo_evento SERIAL PRIMARY KEY,
                                                  nombre         VARCHAR NOT NULL UNIQUE,
                                                  descripcion    TEXT
);

CREATE TABLE IF NOT EXISTS public.evento (
                                             id_evento      SERIAL PRIMARY KEY,
                                             id_cliente     INTEGER NOT NULL,
                                             id_empleado    INTEGER,
                                             id_tipo_evento INTEGER NOT NULL,
                                             id_cita        INTEGER UNIQUE,               -- cita de origen (opcional)
                                             fecha_evento   DATE NOT NULL,
                                             hora_evento    TIME,
                                             lugar          VARCHAR,
                                             num_invitados  INTEGER DEFAULT 0,
                                             descripcion    TEXT,
                                             estado         VARCHAR DEFAULT 'SOLICITADO', -- SOLICITADO, COTIZADO, CONFIRMADO, EN_PROCESO, FINALIZADO, CANCELADO
                                             creado_en      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                             nombre         VARCHAR,
                                             presupuesto    NUMERIC DEFAULT 0.00,
                                             CONSTRAINT fk_evento_cliente  FOREIGN KEY (id_cliente)
    REFERENCES public.cliente(id_cliente) ON DELETE CASCADE,
    CONSTRAINT fk_evento_empleado FOREIGN KEY (id_empleado)
    REFERENCES public.empleado(id_empleado) ON DELETE SET NULL,
    CONSTRAINT fk_evento_tipo     FOREIGN KEY (id_tipo_evento)
    REFERENCES public.tipo_evento(id_tipo_evento),
    CONSTRAINT fk_evento_cita     FOREIGN KEY (id_cita)
    REFERENCES public.cita(id_cita) ON DELETE SET NULL
    );

-- Insumos reservados para un evento
CREATE TABLE IF NOT EXISTS public.evento_articulo (
                                                      id_evento_articulo SERIAL PRIMARY KEY,
                                                      id_evento          INTEGER NOT NULL,
                                                      id_articulo        INTEGER NOT NULL,
                                                      cantidad           INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario    NUMERIC NOT NULL CHECK (precio_unitario >= 0),
    subtotal           NUMERIC NOT NULL CHECK (subtotal >= 0),
    CONSTRAINT fk_ev_art_evento   FOREIGN KEY (id_evento)
    REFERENCES public.evento(id_evento),
    CONSTRAINT fk_ev_art_articulo FOREIGN KEY (id_articulo)
    REFERENCES public.articulo(id_articulo)
    );

-- Personal operativo asignado a un evento
CREATE TABLE IF NOT EXISTS public.evento_empleado (
                                                      id_evento     INTEGER NOT NULL,
                                                      id_empleado   INTEGER NOT NULL,
                                                      rol_en_evento VARCHAR DEFAULT 'STAFF_OPERATIVO',
                                                      CONSTRAINT evento_empleado_pkey PRIMARY KEY (id_evento, id_empleado),
    CONSTRAINT fk_ev_emp_evento   FOREIGN KEY (id_evento)
    REFERENCES public.evento(id_evento),
    CONSTRAINT fk_ev_emp_empleado FOREIGN KEY (id_empleado)
    REFERENCES public.empleado(id_empleado)
    );

-- =====================================================================
-- 4. COTIZACIONES Y ACUERDOS
-- =====================================================================

CREATE TABLE IF NOT EXISTS public.cotizacion (
                                                 id_cotizacion SERIAL PRIMARY KEY,
                                                 id_evento     INTEGER NOT NULL,
                                                 fecha         DATE DEFAULT CURRENT_DATE,
                                                 subtotal      NUMERIC NOT NULL DEFAULT 0.00,
                                                 descuento     NUMERIC DEFAULT 0.00,
                                                 total         NUMERIC NOT NULL DEFAULT 0.00,
                                                 estado        VARCHAR DEFAULT 'PENDIENTE',
                                                 observaciones TEXT,
                                                 creado_en     TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                                 CONSTRAINT fk_cotizacion_evento FOREIGN KEY (id_evento)
    REFERENCES public.evento(id_evento) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS public.detalle_cotizacion (
                                                         id_detalle      SERIAL PRIMARY KEY,
                                                         id_cotizacion   INTEGER NOT NULL,
                                                         id_servicio     INTEGER NOT NULL,
                                                         cantidad        INTEGER DEFAULT 1,
                                                         precio_unitario NUMERIC NOT NULL,
                                                         subtotal        NUMERIC NOT NULL,
                                                         CONSTRAINT fk_detalle_cotizacion FOREIGN KEY (id_cotizacion)
    REFERENCES public.cotizacion(id_cotizacion) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_servicio   FOREIGN KEY (id_servicio)
    REFERENCES public.servicio(id_servicio)
    );

CREATE TABLE IF NOT EXISTS public.acuerdo (
                                              id_acuerdo         SERIAL PRIMARY KEY,
                                              id_evento          INTEGER NOT NULL,
                                              condiciones        TEXT NOT NULL,
                                              estado             VARCHAR DEFAULT 'PENDIENTE',
                                              fecha_registro     TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                              fecha_confirmacion TIMESTAMPTZ,
                                              CONSTRAINT fk_acuerdo_evento FOREIGN KEY (id_evento)
    REFERENCES public.evento(id_evento) ON DELETE CASCADE
    );

-- =====================================================================
-- 5. PAGOS, SEGUIMIENTO Y NOTIFICACIONES
-- =====================================================================

CREATE TABLE IF NOT EXISTS public.pago (
                                           id_pago         SERIAL PRIMARY KEY,
                                           id_evento       INTEGER NOT NULL,
                                           monto           NUMERIC NOT NULL,
                                           fecha_pago      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                           metodo_pago     VARCHAR NOT NULL,
                                           estado          VARCHAR DEFAULT 'COMPLETADO',  -- PENDIENTE, COMPLETADO, RECHAZADO
                                           comprobante_url VARCHAR,
                                           CONSTRAINT fk_pago_evento FOREIGN KEY (id_evento)
    REFERENCES public.evento(id_evento) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS public.seguimiento (
                                                  id_seguimiento SERIAL PRIMARY KEY,
                                                  id_evento      INTEGER NOT NULL,
                                                  titulo         VARCHAR NOT NULL,
                                                  descripcion    TEXT,
                                                  fecha_registro TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                                  CONSTRAINT fk_seguimiento_evento FOREIGN KEY (id_evento)
    REFERENCES public.evento(id_evento) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS public.notificacion (
                                                   id_notificacion SERIAL PRIMARY KEY,
                                                   id_usuario      INTEGER NOT NULL,
                                                   id_evento       INTEGER,
                                                   mensaje         TEXT NOT NULL,
                                                   leida           BOOLEAN DEFAULT FALSE,
                                                   fecha           TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                                   tipo            VARCHAR NOT NULL DEFAULT 'GENERAL',  -- COTIZACION, ACUERDO, PAGO, SEGUIMIENTO, GENERAL
                                                   CONSTRAINT fk_notificacion_usuario FOREIGN KEY (id_usuario)
    REFERENCES public.usuario(id) ON DELETE CASCADE,
    CONSTRAINT fk_notificacion_evento  FOREIGN KEY (id_evento)
    REFERENCES public.evento(id_evento) ON DELETE SET NULL
    );

-- =====================================================================
-- 6. ÍNDICES (aceleran las consultas más frecuentes de la aplicación)
-- =====================================================================

CREATE INDEX IF NOT EXISTS idx_notificacion_usuario_fecha ON public.notificacion (id_usuario, fecha DESC);
CREATE INDEX IF NOT EXISTS idx_evento_cliente             ON public.evento (id_cliente);
CREATE INDEX IF NOT EXISTS idx_evento_empleado            ON public.evento (id_empleado);
CREATE INDEX IF NOT EXISTS idx_cita_cliente_fecha         ON public.cita (id_cliente, fecha);
CREATE INDEX IF NOT EXISTS idx_cotizacion_evento          ON public.cotizacion (id_evento);
CREATE INDEX IF NOT EXISTS idx_pago_evento                ON public.pago (id_evento);
CREATE INDEX IF NOT EXISTS idx_seguimiento_evento         ON public.seguimiento (id_evento);

-- =====================================================================
-- 7. DATOS INICIALES (OPCIONAL - quita los "--" para usarlos)
--    La aplicación necesita al menos un tipo de evento para poder
--    registrar eventos. Ajusta los nombres a los que usa tu equipo.
-- =====================================================================
-- INSERT INTO public.tipo_evento (nombre, descripcion) VALUES
--     ('Boda',          'Bodas y matrimonios'),
--     ('Quinceañero',   'Fiestas de quince años'),
--     ('Cumpleaños',    'Cumpleaños y celebraciones familiares'),
--     ('Corporativo',   'Eventos de empresa')
-- ON CONFLICT (nombre) DO NOTHING;

-- =====================================================================
-- 8. SOLO PARA UNA BASE YA EXISTENTE (OPCIONAL)
--    El esquema exportado de Supabase trae una restricción heredada,
--    cliente_id_fkey, que obliga a que cliente.id_cliente sea también
--    un id de usuario. No tiene sentido (la relación correcta es
--    id_usuario) y puede bloquear registros. Este script no la crea;
--    si tu base la tiene, quítala con:
-- =====================================================================
-- ALTER TABLE public.cliente DROP CONSTRAINT IF EXISTS cliente_id_fkey;