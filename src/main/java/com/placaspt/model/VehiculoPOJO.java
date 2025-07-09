package com.placaspt.model;

import java.util.Objects;

public class VehiculoPOJO {
    private Integer idVehiculo;
    private String  marca;
    private String  modelo;
    private int     anio;
    private String  color;
    private String  fkIdPlaca;
    private Integer fkIdUsuarioFijo;
    private Integer fkIdUsuarioTemporal;
    private int     fkIdAdministrador;

    public VehiculoPOJO() { }

    public VehiculoPOJO(String marca, String modelo, int anio, String color,
                        String fkIdPlaca, Integer fkIdUsuarioFijo,
                        Integer fkIdUsuarioTemporal, int fkIdAdministrador) {
        this.marca               = marca;
        this.modelo              = modelo;
        this.anio                = anio;
        this.color               = color;
        this.fkIdPlaca           = fkIdPlaca;
        this.fkIdUsuarioFijo     = fkIdUsuarioFijo;
        this.fkIdUsuarioTemporal = fkIdUsuarioTemporal;
        this.fkIdAdministrador   = fkIdAdministrador;
    }

    public VehiculoPOJO(int idVehiculo, String marca, String modelo, int anio, String color,
                        String fkIdPlaca, Integer fkIdUsuarioFijo,
                        Integer fkIdUsuarioTemporal, int fkIdAdministrador) {
        this(marca, modelo, anio, color, fkIdPlaca, fkIdUsuarioFijo, fkIdUsuarioTemporal, fkIdAdministrador);
        this.idVehiculo = idVehiculo;
    }

    public Integer getIdVehiculo()             { return idVehiculo; }
    public void    setIdVehiculo(Integer id)   { this.idVehiculo = id; }
    public String  getMarca()                  { return marca; }
    public void    setMarca(String m)          { this.marca = m; }
    public String  getModelo()                 { return modelo; }
    public void    setModelo(String m)         { this.modelo = m; }
    public int     getAnio()                   { return anio; }
    public void    setAnio(int a)              { this.anio = a; }
    public String  getColor()                  { return color; }
    public void    setColor(String c)          { this.color = c; }
    public String  getFkIdPlaca()              { return fkIdPlaca; }
    public void    setFkIdPlaca(String p)      { this.fkIdPlaca = p; }
    public Integer getFkIdUsuarioFijo()        { return fkIdUsuarioFijo; }
    public void    setFkIdUsuarioFijo(Integer u){ this.fkIdUsuarioFijo = u; }
    public Integer getFkIdUsuarioTemporal()    { return fkIdUsuarioTemporal; }
    public void    setFkIdUsuarioTemporal(Integer u){ this.fkIdUsuarioTemporal = u; }
    public int     getFkIdAdministrador()      { return fkIdAdministrador; }
    public void    setFkIdAdministrador(int a) { this.fkIdAdministrador = a; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehiculoPOJO)) return false;
        VehiculoPOJO that = (VehiculoPOJO) o;
        return Objects.equals(idVehiculo, that.idVehiculo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idVehiculo);
    }

    @Override
    public String toString() {
        return "VehiculoPOJO{" +
                "idVehiculo=" + idVehiculo +
                ", placa='" + fkIdPlaca + '\'' +
                ", marca='" + marca + '\'' +
                ", modelo='" + modelo + '\'' +
                ", anio=" + anio +
                '}';
    }
}

