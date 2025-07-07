package com.placaspt.logic;

import com.placaspt.model.EventoPlacaDTO;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

public class AppService {
    private static final AppService INSTANCE = new AppService();

    // El singleton del lector RFID
    private final RS232RFID rfidReader = RS232RFID.getInstance();

    // El polling SSH de placas
    private RaspberryPollingService placaPolling;

    // Lista de listeners para ambos canales
    private final Set<AppEventListener> listeners = new CopyOnWriteArraySet<>();

    private AppService() { }

    public static AppService getInstance() {
        return INSTANCE;
    }

    /** Registra un listener para eventos RFID y de placa. */
    public void registerListener(AppEventListener l) {
        listeners.add(l);
    }

    /** Quita un listener. */
    public void unregisterListener(AppEventListener l) {
        listeners.remove(l);
    }

    /**
     * Abre el puerto COM y empieza a escuchar tags RFID.
     * Devuelve true si el puerto quedó abierto.
     */
    public boolean startRfid(String port) {
        if (!rfidReader.isOpen() && !rfidReader.iniciar(port)) {
            return false;
        }
        // Nos suscribimos una vez; RS232RFID maneja internamente una lista de callbacks
        rfidReader.escuchar(tag -> {
            for (var l : listeners) {
                l.onRfidTag(tag);
            }
        });
        return true;
    }

    /** Cierra el puerto RFID. */
    public void stopRfid() {
        if (rfidReader.isOpen()) {
            rfidReader.cerrar();
        }
    }

    /**
     * Inicia el polling SSH en la Raspberry para leer JSON de placas.
     * Sólo arranca si no está ya corriendo.
     */
    public void startPlacaPolling(String host,
                                  String user,
                                  String pass,
                                  String remotoArchivo,
                                  long periodo,
                                  TimeUnit unit) {
        if (placaPolling != null) return;
        placaPolling = new RaspberryPollingService(
                host, user, pass, remotoArchivo,
                periodo, unit,
                ev -> {
                    for (var l : listeners) {
                        l.onPlacaEvent(ev);
                    }
                }
        );
        placaPolling.start();
    }

    /** Detiene el polling SSH de placas. */
    public void stopPlacaPolling() {
        if (placaPolling != null) {
            placaPolling.stop();
            placaPolling = null;
        }
    }
}
