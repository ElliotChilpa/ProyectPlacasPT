package com.placaspt.model;

/**
 * DTO para rellenar la TableView de usuarios:
 * combina campos de Usuario + datos de RFID, Placa y Vehículo.
 */
public class UsuarioViewDTO {
    private final int    id;
    private final String nombre;
    private final String correo;
    private final String tipo;      // "Fijo" o "Temporal"
    private final String rfid;      // cadena vacía si es temporal
    private final String placa;     // cadena vacía si es temporal
    private final String vehiculo;  // marca + modelo, o cadena vacía

    public UsuarioViewDTO(int id,
                          String nombre,
                          String correo,
                          String tipo,
                          String rfid,
                          String placa,
                          String vehiculo) {
        this.id        = id;
        this.nombre    = nombre;
        this.correo    = correo;
        this.tipo      = tipo;
        this.rfid      = rfid;
        this.placa     = placa;
        this.vehiculo  = vehiculo;
    }

    public int    getId()        { return id;       }
    public String getNombre()    { return nombre;   }
    public String getCorreo()    { return correo;   }
    public String getTipo()      { return tipo;     }
    public String getRfid()      { return rfid;     }
    public String getPlaca()     { return placa;    }
    public String getVehiculo()  { return vehiculo; }
}
