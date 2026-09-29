    private final EventoDAO eventoDAO;
    private final ArticuloDAO articuloDAO;
    private final ProveedorDAO proveedorDAO;
    private final EmpleadoDAO empleadoDAO;
    private final ClienteDAO clienteDAO;
    private final TipoEventoDAO tipoEventoDAO;

    public EventoService() {
        this.eventoDAO = new EventoDAOImpl();
        this.articuloDAO = new ArticuloDAOImpl();
        this.proveedorDAO = new ProveedorDAOImpl();
        this.empleadoDAO = new EmpleadoDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
        this.tipoEventoDAO = new TipoEventoDAOImpl();
    }

    public List<TipoEvento> listarTiposEvento() {
        return tipoEventoDAO.listarTodos();
    }

    public List<Cliente> listarClientes() {
        return clienteDAO.listarClientesConUsuario();
    }

    public List<Articulo> listarInsumosDisponibles() {
        return articuloDAO.listarActivosConProveedor();
    }

    public List<Empleado> listarPersonalOperativo() {
        return empleadoDAO.listarPersonalOperativo();
    }

    public List<Evento> listarEventosActivos() {
        return eventoDAO.listarEventosActivos();
    }

    /**
     * Deduce la lista de proveedores únicos involucrados en una selección de insumos.
     * Regla: No duplicar proveedores; se derivan automáticamente mediante articulo.id_proveedor.
     */
    public List<Proveedor> deducirProveedores(List<Integer> idsArticulos) {
        if (idsArticulos == null || idsArticulos.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Integer> idsProveedoresUnicos = new LinkedHashSet<>();
        for (Integer idArt : idsArticulos) {
            Articulo a = articuloDAO.buscarPorId(idArt);
            if (a != null && a.getIdProveedor() != null && a.getIdProveedor() > 0) {
                idsProveedoresUnicos.add(a.getIdProveedor());
            }
        }
        if (idsProveedoresUnicos.isEmpty()) {
            return new ArrayList<>();
        }
        return proveedorDAO.listarPorIds(new ArrayList<>(idsProveedoresUnicos));
    }

    /**
     * Valida integralmente las reglas de negocio y ejecuta la transacción atómica.
     */
    public boolean registrarEventoEnVivo(Evento evento, List<EventoArticulo> insumos, List<Integer> idEmpleados) {
        // Regla 1: Validar cabecera obligatoria
        if (evento == null) {
            throw new IllegalArgumentException("Los datos del evento no pueden ser nulos.");
        }
        if (evento.getNombre() == null || evento.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio.");
        }
        if (evento.getIdCliente() == null || evento.getIdCliente() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un Cliente Titular válido.");
        }
        if (evento.getIdTipoEvento() == null || evento.getIdTipoEvento() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un Tipo de Celebración.");
        }
        if (evento.getFechaEvento() == null) {
            throw new IllegalArgumentException("La fecha del evento es obligatoria.");
        }
        if (evento.getNumInvitados() == null || evento.getNumInvitados() <= 0) {
            throw new IllegalArgumentException("El aforo estimado debe ser mayor a 0.");
        }
        if (evento.getPresupuesto() == null || evento.getPresupuesto().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El presupuesto estimado no puede ser negativo.");
        }

        // Regla 2: Validar que se haya seleccionado al menos un insumo
        if (insumos == null || insumos.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un insumo en la pestaña 'Insumo'.");
        }
        for (EventoArticulo item : insumos) {
            if (item.getIdArticulo() == null || item.getIdArticulo() <= 0) {
                throw new IllegalArgumentException("Artículo no especificado en el detalle de insumos.");
            }
            if (item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad solicitada para cada insumo debe ser mayor a 0.");
            }
        }

        // Regla 3: Validar que exista al menos un empleado asignado
        boolean tieneCoordinador = (evento.getIdEmpleado() != null && evento.getIdEmpleado() > 0);
        boolean tieneStaff = (idEmpleados != null && !idEmpleados.isEmpty());
        if (!tieneCoordinador && !tieneStaff) {
            throw new IllegalArgumentException("Debe asignar al menos un responsable o personal en la pestaña 'Personal'.");
        }

        // Ejecución transaccional atómica en el DAO
        return eventoDAO.registrarEventoCompleto(evento, insumos, idEmpleados);
    }
