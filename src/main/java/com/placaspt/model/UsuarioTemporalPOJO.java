package com.placaspt.model;

import java.sql.Blob;
import java.time.LocalDate;

public class UsuarioTemporalPOJO {
    private int     idUsuarioTemporal;  // PK
    private String  actividadRealizar;
    private Blob    documentoDNI;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int     fkUsuario;          // FK al usuario en Usuario

    // Constructor para lectura completa
    public UsuarioTemporalPOJO(int idUsuarioTemporal,
                               String actividadRealizar,
                               Blob documentoDNI,
                               LocalDate fechaInicio,
                               LocalDate fechaFin,
                               int fkUsuario) {
        this.idUsuarioTemporal = idUsuarioTemporal;
        this.actividadRealizar = actividadRealizar;
        this.documentoDNI      = documentoDNI;
        this.fechaInicio       = fechaInicio;
        this.fechaFin          = fechaFin;
        this.fkUsuario         = fkUsuario;
    }

    // Getters y setters
    public int getIdUsuarioTemporal()       { return idUsuarioTemporal; }
    public String getActividadRealizar()    { return actividadRealizar; }
    public Blob getDocumentoDNI()           { return documentoDNI; }
    public LocalDate getFechaInicio()       { return fechaInicio; }
    public LocalDate getFechaFin()          { return fechaFin; }
    public int getFkUsuario()               { return fkUsuario; }

    public void setActividadRealizar(String actividadRealizar) {
        this.actividadRealizar = actividadRealizar;
    }
    public void setDocumentoDNI(Blob documentoDNI) {
        this.documentoDNI = documentoDNI;
    }
    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }
}
