package com.placaspt.model;

import java.time.LocalDate;

public class TarjetaRFIDPOJO {
    private String idRfid;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activa;         // mapeado de tinyint(1)
    private int fkAdministrador;
    private int fkUsuarioFijo;

    // Constructor sin ID_RFID (para insertar)
    public TarjetaRFIDPOJO(LocalDate fechaInicio,
                           LocalDate fechaFin,
                           boolean activa,
                           int fkAdministrador,
                           int fkUsuarioFijo) {
        this.fechaInicio    = fechaInicio;
        this.fechaFin       = fechaFin;
        this.activa         = activa;
        this.fkAdministrador= fkAdministrador;
        this.fkUsuarioFijo  = fkUsuarioFijo;
    }

    // Constructor completo (para leer de BD)
    public TarjetaRFIDPOJO(String idRfid,
                           LocalDate fechaInicio,
                           LocalDate fechaFin,
                           boolean activa,
                           int fkAdministrador,
                           int fkUsuarioFijo) {
        this(fechaInicio, fechaFin, activa, fkAdministrador, fkUsuarioFijo);
        this.idRfid = idRfid;
    }

    // Getters y setters
    public String getIdRfid() { return idRfid; }
    public void setIdRfid(String idRfid) { this.idRfid = idRfid; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public int getFkAdministrador() { return fkAdministrador; }
    public void setFkAdministrador(int fkAdministrador) { this.fkAdministrador = fkAdministrador; }

    public int getFkUsuarioFijo() { return fkUsuarioFijo; }
    public void setFkUsuarioFijo(int fkUsuarioFijo) { this.fkUsuarioFijo = fkUsuarioFijo; }
}
