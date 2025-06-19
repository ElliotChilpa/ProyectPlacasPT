package com.placaspt.logic;

import java.time.LocalDate;

public class UsuariosPOJO {
    private int id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private LocalDate fechaRegistro;
    private int fkAdministrador;

    // Constructor para insertar (sin id)
    public UsuariosPOJO(String nombres, String apellidos, String correo,
                       String telefono, LocalDate fechaRegistro, int fkAdministrador) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.telefono = telefono;
        this.fechaRegistro = fechaRegistro;
        this.fkAdministrador = fkAdministrador;
    }

    // Constructor completo (para lecturas)
    public UsuariosPOJO(int id, String nombres, String apellidos,
                       String correo, String telefono,
                       LocalDate fechaRegistro, int fkAdministrador) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.telefono = telefono;
        this.fechaRegistro = fechaRegistro;
        this.fkAdministrador = fkAdministrador;
    }

    // Getters/Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombres() { return nombres; }
    public void setNombres(String n) { this.nombres = n; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String a) { this.apellidos = a; }
    public String getCorreo() { return correo; }
    public void setCorreo(String c) { this.correo = c; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String t) { this.telefono = t; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate f) { this.fechaRegistro = f; }
    public int getFkAdministrador() { return fkAdministrador; }
    public void setFkAdministrador(int fk) { this.fkAdministrador = fk; }
}
