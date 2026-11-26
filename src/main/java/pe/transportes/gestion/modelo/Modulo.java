package pe.transportes.gestion.modelo;

public enum Modulo {
    PASAJEROS("Pasajeros", "Registrar y editar pasajeros", Rol.VENDEDOR),
    VENTAS("Vender pasajes y reservas", "Reservar, vender y anular asientos", Rol.VENDEDOR),
    PASAJES("Pasajes vendidos", "Consultar y anular pasajes emitidos", Rol.VENDEDOR),
    REPORTES("Reportes", "Reportes de viajes y pasajeros con exportación a CSV", Rol.VENDEDOR),
    ALERTAS("Alertas de viaje", "Ver y atender accidentes, retrasos y pausas", Rol.VENDEDOR),
    MIS_VIAJES("Mis viajes", "Ver los viajes asignados y reportar incidencias", Rol.CONDUCTOR);

    private final String etiqueta;
    private final String descripcion;
    private final Rol rolAplicable;

    Modulo(String etiqueta, String descripcion, Rol rolAplicable) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
        this.rolAplicable = rolAplicable;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Rol getRolAplicable() {
        return rolAplicable;
    }
}
