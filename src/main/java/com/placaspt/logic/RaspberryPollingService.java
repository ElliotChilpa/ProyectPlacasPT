/*package com.placaspt.logic;

import com.placaspt.model.EventoPlacaDTO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RaspberryPollingService {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();
    private final ObjectMapper mapper;
    private final String host, user, pass, remotoArchivo;
    private final long periodo;
    private final TimeUnit unidad;
    private final Consumer<EventoPlacaDTO> callback;

    public RaspberryPollingService(String host,
                                   String user,
                                   String pass,
                                   String remotoArchivo,
                                   long periodo,
                                   TimeUnit unidad,
                                   Consumer<EventoPlacaDTO> callback) {
        this.host          = host;
        this.user          = user;
        this.pass          = pass;
        this.remotoArchivo = remotoArchivo;
        this.periodo       = periodo;
        this.unidad        = unidad;
        this.callback      = callback;

        // Configuramos Jackson para LocalDateTime y que ignore campos desconocidos
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /** Arranca el polling cada periodo/unidad * /
    public void start() {
        scheduler.scheduleAtFixedRate(this::poll, 0, periodo, unidad);
    }

    /** Para inmediatamente el polling * /
    public void stop() {
        scheduler.shutdownNow();
    }

    /** Tarea que se ejecuta periódicamente * /
    private void poll() {
        try {
            // 1) Leer el JSON remoto
            String json = RaspSSH.leerArchivoRasp(host, user, pass, remotoArchivo);

            // 2) Si hay contenido, parsearlo a tu DTO
            if (json != null && !json.isBlank()) {
                EventoPlacaDTO ev = mapper.readValue(json, EventoPlacaDTO.class);

                // 3) Invocar el callback en el hilo de JavaFX
                Platform.runLater(() -> callback.accept(ev));

                // 4) Renombrar el archivo en la RPi para no volver a procesarlo
                String cmd = "mv " + remotoArchivo + " " + remotoArchivo + ".leido";
                RaspSSH.ejecutarComando(host, user, pass, cmd);
            }
        } catch (Exception ex) {
            // Aquí podrías notificar a la UI o hacer retry lógico
            ex.printStackTrace();
        }
    }
}
*/
package com.placaspt.logic;

import com.placaspt.model.EventoPlacaDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RaspberryPollingService {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();
    private final ObjectMapper mapper;
    private final String host, user, pass, remotoArchivo;
    private final long periodo;
    private final TimeUnit unidad;
    private final Consumer<EventoPlacaDTO> callback;

    public RaspberryPollingService(String host,
                                   String user,
                                   String pass,
                                   String remotoArchivo,
                                   long periodo,
                                   TimeUnit unidad,
                                   Consumer<EventoPlacaDTO> callback) {
        this.host          = host;
        this.user          = user;
        this.pass          = pass;
        this.remotoArchivo = remotoArchivo;
        this.periodo       = periodo;
        this.unidad        = unidad;
        this.callback      = callback;

        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::poll, 0, periodo, unidad);
    }

    public void stop() {
        scheduler.shutdownNow();
    }


    private void poll() {
        try {
            String json = RaspSSH.leerArchivoRasp(host, user, pass, remotoArchivo);
            if (json != null && !json.isBlank()) {
                // 1) Normalizar keys y valores (tu código actual)
                String fixed = json
                        .replaceAll("([\\[{,]\\s*)([a-zA-Z0-9_]+)\\s*:", "$1\"$2\":")
                        .replaceAll(":(\\s*)([A-Za-z0-9\\-:]+)(?=\\s*[,\\}])", ":$1\"$2\"");

                // 2) Deserializar lista de eventos
                List<EventoPlacaDTO> eventos = mapper.readValue(
                        fixed,
                        new TypeReference<List<EventoPlacaDTO>>() {}
                );

                // 3) Solo si hay eventos que procesar…
                if (!eventos.isEmpty()) {
                    // Procesar cada evento en el hilo de JavaFX
                    for (EventoPlacaDTO ev : eventos) {
                        Platform.runLater(() -> callback.accept(ev));
                    }

                    // 4) Eliminar el JSON remoto para liberar el nombre
                    String cmd = "rm " + remotoArchivo;
                    RaspSSH.ejecutarComando(host, user, pass, cmd);
                }
                // Si 'eventos' está vacío, no borramos nada y esperamos al siguiente ciclo
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
