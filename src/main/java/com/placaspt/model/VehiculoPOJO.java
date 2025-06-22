package com.placaspt.model;

/**
 * POJO que mapea la tabla Vehiculo.
 */
public class VehiculoPOJO {
    private int idVehiculo;
    private String marca;
    private String modelo;
    private int anio;
    private String color;
    private int fkIdPlaca;
    private Integer fkUsuarioFijo;     // null si no es fijo
    private Integer fkUsuarioTemporal; // null si no es temporal
    private int fkAdministrador;

    // Constructor completo
    public VehiculoPOJO(int idVehiculo,
                        String marca,
                        String modelo,
                        int anio,
                        String color,
                        int fkIdPlaca,
                        Integer fkUsuarioFijo,
                        Integer fkUsuarioTemporal,
                        int fkAdministrador) {
        this.idVehiculo       = idVehiculo;
        this.marca            = marca;
        this.modelo           = modelo;
        this.anio             = anio;
        this.color            = color;
        this.fkIdPlaca        = fkIdPlaca;
        this.fkUsuarioFijo    = fkUsuarioFijo;
        this.fkUsuarioTemporal= fkUsuarioTemporal;
        this.fkAdministrador  = fkAdministrador;
    }

    // Getters
    public int getIdVehiculo()        { return idVehiculo; }
    public String getMarca()          { return marca;      }
    public String getModelo()         { return modelo;     }
    public int getAnio()              { return anio;       }
    public String getColor()          { return color;      }
    public int getFkIdPlaca()         { return fkIdPlaca;  }
    public Integer getFkUsuarioFijo() { return fkUsuarioFijo; }
    public Integer getFkUsuarioTemporal() { return fkUsuarioTemporal; }
    public int getFkAdministrador()   { return fkAdministrador; }

    /**
     * Conveniencia para la vista: devuelve la placa como String.
     */
    public String getPlaca() {
        return String.valueOf(fkIdPlaca);
    }
}
