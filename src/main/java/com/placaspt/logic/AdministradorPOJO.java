package com.placaspt.logic;

public class AdministradorPOJO {
    private int id;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private String clave;

    // Constructor sin ID (para insertar un nuevo admin)
    public AdministradorPOJO(String nombre, String apellido, String correo, String telefono, String clave) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.telefono = telefono;
        this.clave = clave;
    }

    // Constructor con ID (para leer desde base de datos)

    public AdministradorPOJO(int id, String nombre, String apellido, String correo, String telefono, String clave) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.telefono = telefono;
        this.clave = clave;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
}
