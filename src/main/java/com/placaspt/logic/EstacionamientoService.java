/*package com.placaspt.logic;

import com.placaspt.database.*;
import com.placaspt.model.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.time.LocalDateTime;

public class EstacionamientoService {

    private final PlacasDAO placasDAO = new PlacasDAO();
    private final RegistroPlacaDAO regPlaDAO = new RegistroPlacaDAO();
    private final AccesoDAO accesoDAO       = new AccesoDAO();

    /** Llamado desde el polling cuando llega un evento JSON * /
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
*/

package com.placaspt.logic;

import com.placaspt.database.*;
import com.placaspt.model.EventoPlacaDTO;

import java.time.LocalDateTime;

/**
 * Servicio de negocio para gestionar accesos por RFID o por placa.
 */
public class EstacionamientoService {

    private final TarjetaRFIDDAO      tarjetaDAO   = new TarjetaRFIDDAO();
    private final RegistroRFIDDAO     regRfidDAO   = new RegistroRFIDDAO();
    private final PlacasDAO           placasDAO    = new PlacasDAO();
    private final RegistroPlacaDAO    regPlaDAO    = new RegistroPlacaDAO();
    private final AccesoDAO           accesoDAO    = new AccesoDAO();
    private final UsuariosDAO         usuariosDAO  = new UsuariosDAO();

    /**
     * Procesa un tag RFID recibido:
     * 1) Valida que exista un usuario fijo con ese tag.
     * 2) Inserta un registro en RegistroRFID.
     * 3) Inserta un registro en Acceso (INGRESO o DENEGADO).
     */
    public void procesarRfid(String tag) {
        // 1) ¿Existe un usuario fijo con este tag?
        int idUsuarioFijo = tarjetaDAO.obtenerIdUsuarioFijoPorTag(tag);
        boolean permitido = idUsuarioFijo > 0;

        // 1b) Si existe, buscamos el ID_Usuario base
        Integer idUsuario = null;
        if (permitido) {
            idUsuario = usuariosDAO.obtenerUsuarioBasePorFijo(idUsuarioFijo);
            if (idUsuario < 0) {
                permitido  = false;
                idUsuario = null;
            }
        }

        // 2) Registro en historial RFID
        String estado   = permitido ? "INGRESO" : "DENEGADO";
        String descrip  = permitido ? "Tarjeta reconocida" : "Tag no registrado";
        int idRegRfid   = regRfidDAO.insertarRegistroRFID(tag, estado, descrip);

        // 3) Registro en tabla Acceso
        accesoDAO.insertarAcceso(
                LocalDateTime.now(),
                "RFID",
                idUsuario,   // puede ser null si DENEGADO
                null,
                idRegRfid
        );
    }

    /**
     * Procesa un evento de placa llegados por polling JSON:
     *  - entrada: valida placa activa y asignada, registra en RegistroPlaca y en Acceso.
     *  - salida: cierra el acceso abierto (si existe), registra sólo una vez la salida.
     *  - en caso de placa no existente o salida sin ingreso abierto, registra como DENEGADO.
     */
    /*
    public void procesarEvento(EventoPlacaDTO ev) {
        String placa = ev.getPlate();
        LocalDateTime ts = ev.getTime();
        String gate   = ev.getGate().name(); // INGRESO o SALIDA

        if (gate.equalsIgnoreCase("INGRESO")) {
            // ¿Placa activa y asignada a un vehículo?
            if (!placasDAO.existePlacaActivaYAsignada(placa)) {
                // DENEGADO por placa inválida
                regPlaDAO.insertarRegistroPlaca(placa, "DENEGADO", "Placa no válida o no asignada");
            } else {
                // Registro de ingreso normal
                int idRegPla = regPlaDAO.insertarRegistroPlaca(placa, "INGRESO", "Detección cámara");
                accesoDAO.insertarAcceso(ts, "PLACA", null, placa, idRegPla);
                accesoDAO.insertarAcceso(ts, "PLACA", null, placa, idRegPla);
            }
        }
        else if (gate.equalsIgnoreCase("SALIDA")) {
            // buscamos el último ingreso abierto
            Integer idAcceso = accesoDAO.obtenerUltimoIngresoPorPlaca(placa);
            if (idAcceso != null) {
                // registramos la salida una sola vez
                int idRegPla = regPlaDAO.insertarRegistroPlaca(placa, "SALIDA", "Detección cámara");
                accesoDAO.cerrarSalida(idAcceso);
            } else {
                // salida sin ingreso previo abierto → DENEGADO
                regPlaDAO.insertarRegistroPlaca(placa, "DENEGADO", "Salida sin ingreso abierto");
            }
        }
    }*/
    public void procesarEvento(EventoPlacaDTO ev) {
        String placa = ev.getPlate();
        LocalDateTime ts = ev.getTime();
        String gate   = ev.getGate().name(); // INGRESO o SALIDA

        if (gate.equalsIgnoreCase("INGRESO")) {
            // ¿Placa activa y asignada a un vehículo?
            if (!placasDAO.existePlacaActivaYAsignada(placa)) {
                // DENEGADO por placa inválida
                regPlaDAO.insertarRegistroPlaca(placa, "DENEGADO", "Placa no válida o no asignada");
            } else {
                // 1) Inserto el evento de INGRESO en registroplaca y obtengo su ID
                int idRegPla = regPlaDAO.insertarRegistroPlaca(placa, "INGRESO", "Detección cámara");
                // 2) Inserto el acceso usando ese ID de registroplaca
                accesoDAO.insertarAcceso(
                        ts,
                        "PLACA",
                        null,       // no hay fkUsuario en acceso por placa
                        idRegPla,   // FK_ID_RegistroPlaca
                        null        // FK_ID_RegistroRFID
                );
            }

        } else if (gate.equalsIgnoreCase("SALIDA")) {
            // buscamos el último ingreso abierto
            Integer idAcceso = accesoDAO.obtenerUltimoIngresoPorPlaca(placa);
            if (idAcceso != null) {
                // 1) Inserto el evento SALIDA en registroplaca
                int idRegPla = regPlaDAO.insertarRegistroPlaca(placa, "SALIDA", "Detección cámara");
                // 2) Cierro la salida sobre ese acceso previamente abierto
                accesoDAO.cerrarSalida(idAcceso);

            } else {
                // salida sin ingreso previo abierto → DENEGADO
                regPlaDAO.insertarRegistroPlaca(placa, "DENEGADO", "Salida sin ingreso abierto");
            }
        }
    }

}
