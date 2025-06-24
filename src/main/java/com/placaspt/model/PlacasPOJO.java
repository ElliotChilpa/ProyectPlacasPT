// src/main/java/com/placaspt/model/PlacasPOJO.java
package com.placaspt.model;

public class PlacasPOJO {
    private String idPlaca;
    private String entidadFederativa;
    private boolean estadoVigencia;

    // Constructor para inserción / edición
    public PlacasPOJO(String idPlaca, String entidadFederativa, boolean estadoVigencia) {
        this.idPlaca = idPlaca;
        this.entidadFederativa = entidadFederativa;
        this.estadoVigencia = estadoVigencia;
    }

    // Getters & Setters

    public String getIdPlaca() {
        return idPlaca;
    }

    public void setIdPlaca(String idPlaca) {
        this.idPlaca = idPlaca;
    }

    public String getEntidadFederativa() {
        return entidadFederativa;
    }

    public void setEntidadFederativa(String entidadFederativa) {
        this.entidadFederativa = entidadFederativa;
    }

    public boolean isEstadoVigencia() {
        return estadoVigencia;
    }

    public void setEstadoVigencia(boolean estadoVigencia) {
        this.estadoVigencia = estadoVigencia;
    }
}
