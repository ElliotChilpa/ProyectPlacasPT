package com.placaspt.model;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EventoPlacaDTO {
    private LocalDateTime time;
    private String plate;

    @JsonProperty("gate")
    private EventoEstado gate;

    public EventoPlacaDTO() {}

    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }

    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }

    public EventoEstado getGate() { return gate; }
    public void setGate(EventoEstado gate) { this.gate = gate; }
}
