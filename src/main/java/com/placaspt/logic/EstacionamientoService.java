package com.placaspt.logic;

import com.placaspt.database.*;
import com.placaspt.model.EventoPlacaDTO;

import java.time.LocalDateTime;

/**
 * Servicio de negocio para gestionar accesos por RFID o por placa.
 */
public class EstacionamientoService {

    private final TarjetaRFIDDAO   tarjetaDAO   = new TarjetaRFIDDAO();
    private final RegistroRFIDDAO  regRfidDAO   = new RegistroRFIDDAO();
    private final PlacasDAO        placasDAO    = new PlacasDAO();
    private final RegistroPlacaDAO regPlaDAO    = new RegistroPlacaDAO();
    private final AccesoDAO        accesoDAO    = new AccesoDAO();
    private final UsuariosDAO      usuariosDAO  = new UsuariosDAO();
    private final VehiculoDAO      vehDAO       = new VehiculoDAO();

    /**
     * Procesa un tag RFID recibido:
     *  - alterna INGRESO/SALIDA según último estado
     *  - registra en registrorfid y en acceso
     */
    public void procesarRfid(String tag) {
        LocalDateTime ahora = LocalDateTime.now();

        // 1) ¿Ese tag está asignado a un usuario fijo?
        int idUsuarioFijo = tarjetaDAO.obtenerIdUsuarioFijoPorTag(tag);
        if (idUsuarioFijo <= 0) {
            // tarjeta desconocida → sólo acceso DENEGADO
            int idReg = regRfidDAO.insertarRegistroRFID(
                    tag,
                    "DENEGADO",
                    "Tag no registrado",
                    tag  // guardamos el raw leído en Tag_Escaneado
            );
            accesoDAO.insertarAcceso(ahora, "RFID", null, null, idReg);
            return;
        }

        // 2) Convertir a ID_Usuario “base”
        Integer idUsuario = usuariosDAO.obtenerUsuarioBasePorFijo(idUsuarioFijo);
        if (idUsuario == null || idUsuario < 0) {
            int idReg = regRfidDAO.insertarRegistroRFID(
                    tag,
                    "DENEGADO",
                    "Usuario no encontrado",
                    tag
            );
            accesoDAO.insertarAcceso(ahora, "RFID", null, null, idReg);
            return;
        }

        // 3) Último estado de este usuario
        String ultimo = accesoDAO.obtenerUltimoEstadoPorUsuario(idUsuario);
        boolean estabaDentro = "INGRESO".equalsIgnoreCase(ultimo);

        // 4) Definimos INGRESO vs SALIDA
        String nuevoEstado = estabaDentro ? "SALIDA" : "INGRESO";
        String descripcion = estabaDentro
                ? "Salida detección RFID"
                : "Detección RFID";

        // 5) Insertamos en registrorfid (con Tag_Escaneado)
        int idRegRfid = regRfidDAO.insertarRegistroRFID(
                tag,
                nuevoEstado,
                descripcion,
                tag
        );

        // 6) Finalmente el acceso
        accesoDAO.insertarAcceso(
                ahora,
                "RFID",
                idUsuario,
                null,
                idRegRfid
        );
    }

    /**
     * Procesa un evento de placa llegado por JSON:
     *  - si ya hay un INGRESO abierto → SALIDA
     *  - si no hay INGRESO abierto → INGRESO (si placa válida) o DENEGADO
     */
    public void procesarEvento(EventoPlacaDTO ev) {
        String placa = ev.getPlate();
        LocalDateTime ahora = LocalDateTime.now();

        // 0) ¿A qué usuario fijo pertenece esta placa?
        Integer idUsuarioFijo = vehDAO.obtenerIdUsuarioFijoPorPlaca(placa);
        Integer idUsuario = (idUsuarioFijo != null)
                ? usuariosDAO.obtenerUsuarioBasePorFijo(idUsuarioFijo)
                : null;

        switch (ev.getGate()) {
            case INGRESO -> {
                if (idUsuario == null || !placasDAO.existePlacaActivaYAsignada(placa)) {
                    // — DENEGADO: no toques FK_ID_Placa, pásale null
                    int idReg = regPlaDAO.insertarRegistroPlaca(
                            /* FK_ID_Placa = */    null,
                            /* Estado */           "DENEGADO",
                            /* Descripción */      idUsuario == null
                                    ? "Placa no asignada a usuario fijo"
                                    : "Placa inactiva o no válida",
                            /* Placa_Escaneada */  placa
                    );
                    accesoDAO.insertarAcceso(ahora, "PLACA", null, idReg, null);
                } else {
                    // — INGRESO válido —
                    int idReg = regPlaDAO.insertarRegistroPlaca(
                            placa,                   // FK_ID_Placa sí válido
                            "INGRESO",
                            "Detección cámara",
                            placa
                    );
                    accesoDAO.insertarAcceso(ahora, "PLACA", idUsuario, idReg, null);
                }
            }
            case SALIDA -> {
                if (idUsuario == null) {
                    // — DENEGADO porque ni existe esa placa —
                    int idReg = regPlaDAO.insertarRegistroPlaca(
                            null,
                            "DENEGADO",
                            "Salida sin ingreso (placa desconocida)",
                            placa
                    );
                    accesoDAO.insertarAcceso(ahora, "PLACA", null, idReg, null);
                } else {
                    Integer idAccesoAbierto = accesoDAO.obtenerUltimoIngresoPorUsuario(idUsuario);
                    if (idAccesoAbierto != null) {
                        // — SALIDA válido —
                        int idReg = regPlaDAO.insertarRegistroPlaca(
                                placa,
                                "SALIDA",
                                "Detección cámara",
                                placa
                        );
                        accesoDAO.insertarAcceso(ahora, "PLACA", idUsuario, idReg, null);
                        accesoDAO.cerrarSalida(idAccesoAbierto);
                    } else {
                        // — DENEGADO porque nunca ingresó —
                        int idReg = regPlaDAO.insertarRegistroPlaca(
                                null,
                                "DENEGADO",
                                "Salida sin ingreso abierto",
                                placa
                        );
                        accesoDAO.insertarAcceso(ahora, "PLACA", null, idReg, null);
                    }
                }
            }
            default -> {
                // no hay más casos
            }
        }
    }

}
