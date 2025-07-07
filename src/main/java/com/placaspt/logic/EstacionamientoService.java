package com.placaspt.logic;

import com.placaspt.database.*;
import com.placaspt.model.EventoEstado;
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
     * @param tag el identificador del tag leído
     * @return el estado final del evento (INGRESO, SALIDA o DENEGADO)
     */
    public EventoEstado procesarRfid(String tag) {
        LocalDateTime ahora = LocalDateTime.now();
        EventoEstado resultado;

        // 1) ¿Ese tag está asignado a un usuario fijo?
        int idUsuarioFijo = tarjetaDAO.obtenerIdUsuarioFijoPorTag(tag);
        if (idUsuarioFijo <= 0) {
            resultado = EventoEstado.DENEGADO;
            int idReg = regRfidDAO.insertarRegistroRFID(
                    tag,
                    resultado.name(),
                    "Tag no registrado",
                    tag
            );
            accesoDAO.insertarAcceso(ahora, "RFID", null, null, idReg);
            return resultado;
        }

        // 2) Convertir a ID_Usuario “base”
        Integer idUsuario = usuariosDAO.obtenerUsuarioBasePorFijo(idUsuarioFijo);
        if (idUsuario == null || idUsuario < 0) {
            resultado = EventoEstado.DENEGADO;
            int idReg = regRfidDAO.insertarRegistroRFID(
                    tag,
                    resultado.name(),
                    "Usuario no encontrado",
                    tag
            );
            accesoDAO.insertarAcceso(ahora, "RFID", null, null, idReg);
            return resultado;
        }

        // 3) Último estado de este usuario
        String ultimo = accesoDAO.obtenerUltimoEstadoPorUsuario(idUsuario);
        boolean estabaDentro = "INGRESO".equalsIgnoreCase(ultimo);

        // 4) Definimos INGRESO vs SALIDA
        resultado = estabaDentro ? EventoEstado.SALIDA : EventoEstado.INGRESO;
        String descripcion = (resultado == EventoEstado.SALIDA)
                ? "Salida detección RFID"
                : "Detección RFID";

        // 5) Insertamos en registrorfid (con Tag_Escaneado)
        int idRegRfid = regRfidDAO.insertarRegistroRFID(
                tag,
                resultado.name(),
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

        return resultado;
    }

    /**
     * Procesa un evento de placa llegado por JSON y devuelve el estado final.
     */
    public EventoEstado procesarEvento(EventoPlacaDTO ev) {
        String placa = ev.getPlate();
        LocalDateTime ahora = LocalDateTime.now();

        // 0) ¿A qué usuario fijo pertenece esta placa?
        Integer idUsuarioFijo = vehDAO.obtenerIdUsuarioFijoPorPlaca(placa);
        Integer idUsuario = (idUsuarioFijo != null)
                ? usuariosDAO.obtenerUsuarioBasePorFijo(idUsuarioFijo)
                : null;

        EventoEstado resultado = null;

        switch (ev.getGate()) {
            case INGRESO -> {
                if (idUsuario == null || !placasDAO.existePlacaActivaYAsignada(placa)) {
                    resultado = EventoEstado.DENEGADO;
                    int idReg = regPlaDAO.insertarRegistroPlaca(
                            null,
                            resultado.name(),
                            idUsuario == null
                                    ? "Placa no asignada a usuario fijo"
                                    : "Placa inactiva o no válida",
                            placa
                    );
                    accesoDAO.insertarAcceso(ahora, "PLACA", null, idReg, null);
                } else {
                    resultado = EventoEstado.INGRESO;
                    int idReg = regPlaDAO.insertarRegistroPlaca(
                            placa,
                            resultado.name(),
                            "Detección cámara",
                            placa
                    );
                    accesoDAO.insertarAcceso(ahora, "PLACA", idUsuario, idReg, null);
                }
            }
            case SALIDA -> {
                if (idUsuario == null) {
                    resultado = EventoEstado.DENEGADO;
                    int idReg = regPlaDAO.insertarRegistroPlaca(
                            null,
                            resultado.name(),
                            "Salida sin ingreso (placa desconocida)",
                            placa
                    );
                    accesoDAO.insertarAcceso(ahora, "PLACA", null, idReg, null);
                } else {
                    Integer idAccesoAbierto = accesoDAO.obtenerUltimoIngresoPorUsuario(idUsuario);
                    if (idAccesoAbierto != null) {
                        resultado = EventoEstado.SALIDA;
                        int idReg = regPlaDAO.insertarRegistroPlaca(
                                placa,
                                resultado.name(),
                                "Detección cámara",
                                placa
                        );
                        accesoDAO.insertarAcceso(ahora, "PLACA", idUsuario, idReg, null);
                        accesoDAO.cerrarSalida(idAccesoAbierto);
                    } else {
                        resultado = EventoEstado.DENEGADO;
                        int idReg = regPlaDAO.insertarRegistroPlaca(
                                null,
                                resultado.name(),
                                "Salida sin ingreso abierto",
                                placa
                        );
                        accesoDAO.insertarAcceso(ahora, "PLACA", null, idReg, null);
                    }
                }
            }
            default -> {
                resultado = EventoEstado.DENEGADO;
            }
        }

        // Actualizamos el DTO por si alguien lo lee después
        ev.setGate(resultado);
        return resultado;
    }

}
