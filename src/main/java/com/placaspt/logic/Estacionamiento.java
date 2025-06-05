package com.placaspt.logic;

public class Estacionamiento {
    private String idUsuario;
    private String nombre;
    private String estado;
    private String placas;
    private String tipo;
    private String horario;

    public Estacionamiento(String idUsuario, String nombre, String estado, String placas, String tipo, String horario) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.estado = estado;
        this.placas = placas;
        this.tipo = tipo;
        this.horario = horario;
    }

    public String getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getEstado() { return estado; }
    public String getPlacas() { return placas; }
    public String getTipo() { return tipo; }
    public String getHorario() { return horario; }
}
