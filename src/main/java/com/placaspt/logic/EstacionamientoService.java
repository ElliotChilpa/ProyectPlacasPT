package com.placaspt.logic;

import com.placaspt.database.*;
import com.placaspt.model.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.time.LocalDateTime;

public class EstacionamientoService {

    private final PlacasDAO placasDAO = new PlacasDAO();
    private final RegistroPlacaDAO regPlaDAO = new RegistroPlacaDAO();
    private final AccesoDAO accesoDAO       = new AccesoDAO();

    /** Llamado desde el polling cuando llega un evento JSON */
    public void procesarEvento(EventoPlacaDTO ev) {
        String placa = ev.getPlate();
        LocalDateTime ts = ev.getTime();
        switch (ev.getGate()) {
            case INGRESO   -> procesarIngreso(placa, ts);
            case SALIDA    -> procesarSalida(placa, ts);
            default        -> System.err.println("Gate desconocido: " + ev.getGate());
        }
    }

    private void procesarIngreso(String placa, LocalDateTime ts) {
        if (!placasDAO.existePlaca(placa)) {
            accesoDAO.insertarAcceso(ts, TipoAcceso.PLACA.name(), null, null, null);
            Platform.runLater(() ->
                    new Alert(Alert.AlertType.WARNING,
                            "Acceso DENEGADO. Placa no activa: " + placa
                    ).showAndWait()
            );
        } else {
            int reg = regPlaDAO.insertarRegistroPlaca(
                    placa,
                    EventoEstado.INGRESO.name(),
                    "Detección cámara"
            );
            accesoDAO.insertarAcceso(ts, TipoAcceso.PLACA.name(), null, reg, null);
            Platform.runLater(() ->
                    new Alert(Alert.AlertType.INFORMATION,
                            "Ingreso PERMITIDO: " + placa
                    ).showAndWait()
            );
        }
        // recarga UI… (se delega al controller)
    }

    private void procesarSalida(String placa, LocalDateTime ts) {
        Integer idAcc = accesoDAO.obtenerUltimoIngresoPorPlaca(placa);
        if (idAcc == null) {
            Platform.runLater(() ->
                    new Alert(Alert.AlertType.ERROR,
                            "No hay ingreso abierto para: " + placa
                    ).showAndWait()
            );
        } else {
            int reg = regPlaDAO.insertarRegistroPlaca(
                    placa,
                    EventoEstado.SALIDA.name(),
                    "Detección cámara"
            );
            boolean ok = accesoDAO.cerrarSalida(idAcc);
            Platform.runLater(() ->
                    new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                            (ok ? "Salida registrada: " : "Error en salida: ") + placa
                    ).showAndWait()
            );
        }
    }
}
