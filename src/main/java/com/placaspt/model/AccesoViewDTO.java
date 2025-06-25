package com.placaspt.model;

import java.time.LocalDateTime;

public class AccesoViewDTO {
    private final int            idAcceso;
    private final LocalDateTime  fechaHora;
    private final String         usuario;
    private final String         placa;
    private final String         metodo;
    private final String         estado;

    public AccesoViewDTO(int idAcceso,
                         LocalDateTime fechaHora,
                         String usuario,
                         String placa,
                         String metodo,
                         String estado) {
        this.idAcceso  = idAcceso;
        this.fechaHora = fechaHora;
        this.usuario   = usuario;
        this.placa     = placa;
        this.metodo    = metodo;
        this.estado    = estado;
    }

    public int getIdAcceso()               { return idAcceso; }
    public LocalDateTime getFechaHora()    { return fechaHora; }
    public String getUsuario()             { return usuario; }
    public String getPlaca()               { return placa; }
    public String getMetodo()              { return metodo; }
    public String getEstado()              { return estado; }
}
