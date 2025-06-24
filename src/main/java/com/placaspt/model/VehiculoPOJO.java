package com.placaspt.model;

public class VehiculoPOJO {

    private Integer idVehiculo;               // PK autoincremental
    private String marca;
    private String modelo;
    private int anio;
    private String color;

    private String fkIdPlaca;                 // puede ser null
    private Integer fkIdUsuarioFijo;          // puede ser null
    private Integer fkIdUsuarioTemporal;      // puede ser null
    private int fkIdAdministrador;

    // ────────────────────────────────────────────────
    // CONSTRUCTORES
    // ────────────────────────────────────────────────

    // Constructor para inserción (sin ID aún)
    public VehiculoPOJO(String marca, String modelo, int anio, String color,
                        String fkIdPlaca, Integer fkIdUsuarioFijo, Integer fkIdUsuarioTemporal,
                        int fkIdAdministrador) {
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.color = color;
        this.fkIdPlaca = fkIdPlaca;
        this.fkIdUsuarioFijo = fkIdUsuarioFijo;
        this.fkIdUsuarioTemporal = fkIdUsuarioTemporal;
        this.fkIdAdministrador = fkIdAdministrador;
    }

    // Constructor para lectura con ID
    public VehiculoPOJO(int idVehiculo, String marca, String modelo, int anio, String color,
                        String fkIdPlaca, Integer fkIdUsuarioFijo, Integer fkIdUsuarioTemporal,
                        int fkIdAdministrador) {
        this.idVehiculo = idVehiculo;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.color = color;
        this.fkIdPlaca = fkIdPlaca;
        this.fkIdUsuarioFijo = fkIdUsuarioFijo;
        this.fkIdUsuarioTemporal = fkIdUsuarioTemporal;
        this.fkIdAdministrador = fkIdAdministrador;
    }

    // ────────────────────────────────────────────────
    // GETTERS Y SETTERS
    // ────────────────────────────────────────────────

    public Integer getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(Integer idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getFkIdPlaca() {
        return fkIdPlaca;
    }

    public void setFkIdPlaca(String fkIdPlaca) {
        this.fkIdPlaca = fkIdPlaca;
    }

    public Integer getFkIdUsuarioFijo() {
        return fkIdUsuarioFijo;
    }

    public void setFkIdUsuarioFijo(Integer fkIdUsuarioFijo) {
        this.fkIdUsuarioFijo = fkIdUsuarioFijo;
    }

    public Integer getFkIdUsuarioTemporal() {
        return fkIdUsuarioTemporal;
    }

    public void setFkIdUsuarioTemporal(Integer fkIdUsuarioTemporal) {
        this.fkIdUsuarioTemporal = fkIdUsuarioTemporal;
    }

    public int getFkIdAdministrador() {
        return fkIdAdministrador;
    }

    public void setFkIdAdministrador(int fkIdAdministrador) {
        this.fkIdAdministrador = fkIdAdministrador;
    }
}
