package com.placaspt.logic;

import com.placaspt.database.*;
import com.placaspt.model.EventoEstado;
import com.placaspt.model.EventoPlacaDTO;

import java.time.LocalDate;
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
    /*
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
    }*/

    /**
     * Procesa un evento de placa llegado por JSON y devuelve el estado final.
     */
    /*
    public EventoEstado procesarEvento(EventoPlacaDTO ev) {
        String placa = ev.getPlate();
        LocalDateTime ahora = LocalDateTime.now();

        // 0) ¿A qué usuario fijo pertenece esta placa?
        Integer idUsuarioFijo = vehDAO.obtenerIdUsuarioFijoPorPlaca(placa);
        Integer idUsuario = (idUsuarioFijo != null)
                ? usuariosDAO.obtenerUsuarioBasePorFijo(idUsuarioFijo)
                : null;

        EventoEstado resultado;

        switch (ev.getGate()) {
            case INGRESO -> {
                // —— INGRESO ——
                if (idUsuario == null || !placasDAO.existePlacaActivaYAsignada(placa)) {
                    // placa inválida o sin asignar → DENEGADO
                    resultado = EventoEstado.DENEGADO;
                    int idReg = regPlaDAO.insertarRegistroPlaca(
                            null,                                // FK_ID_Placa = NULL
                            resultado.name(),                    // "DENEGADO"
                            idUsuario == null
                                    ? "Placa no asignada a usuario fijo"
                                    : "Placa inactiva o no válida",
                            placa                                // Placa_Escaneada
                    );
                    accesoDAO.insertarAcceso(ahora, "PLACA", null, idReg, null);

                } else {
                    // ingreso válido → INGRESO
                    resultado = EventoEstado.INGRESO;
                    int idReg = regPlaDAO.insertarRegistroPlaca(
                            placa,                               // FK_ID_Placa
                            resultado.name(),                    // "INGRESO"
                            "Detección cámara",
                            placa                                // Placa_Escaneada
                    );
                    accesoDAO.insertarAcceso(ahora, "PLACA", idUsuario, idReg, null);
                }
            }
            case SALIDA -> {
                // —— SALIDA ——
                if (idUsuario == null) {
                    // ni siquiera existe la placa → DENEGADO
                    resultado = EventoEstado.DENEGADO;
                    int idReg = regPlaDAO.insertarRegistroPlaca(
                            null,                                // FK_ID_Placa = NULL
                            resultado.name(),                    // "DENEGADO"
                            "Salida sin ingreso (placa desconocida)",
                            placa                                // Placa_Escaneada
                    );
                    accesoDAO.insertarAcceso(ahora, "PLACA", null, idReg, null);

                } else {
                    Integer idAccesoAbierto = accesoDAO.obtenerUltimoIngresoPorUsuario(idUsuario);
                    if (idAccesoAbierto != null) {
                        // salida legítima → SALIDA
                        resultado = EventoEstado.SALIDA;
                        int idReg = regPlaDAO.insertarRegistroPlaca(
                                placa,                           // FK_ID_Placa
                                resultado.name(),                // "SALIDA"
                                "Detección cámara",
                                placa                            // Placa_Escaneada
                        );
                        accesoDAO.insertarAcceso(ahora, "PLACA", idUsuario, idReg, null);
                        accesoDAO.cerrarSalida(idAccesoAbierto);

                    } else {
                        // nunca ingresó → DENEGADO
                        resultado = EventoEstado.DENEGADO;
                        int idReg = regPlaDAO.insertarRegistroPlaca(
                                null,                            // FK_ID_Placa = NULL
                                resultado.name(),                // "DENEGADO"
                                "Salida sin ingreso abierto",
                                placa                            // Placa_Escaneada
                        );
                        accesoDAO.insertarAcceso(ahora, "PLACA", null, idReg, null);
                    }
                }
            }
            default -> {
                // por seguridad, cualquier otro valor se trata como DENEGADO
                resultado = EventoEstado.DENEGADO;
            }
        }

        // Actualizamos el DTO por si alguien lo lee después
        ev.setGate(resultado);
        return resultado;
    }*/

    /**
     * Procesa un evento de placa llegado por JSON y devuelve el estado final.
     * Ahora soporta:
     *  - Usuarios fijos (como antes).
     *  - Usuarios temporales, sólo si están dentro de su rango de fechas.
     */
    // En EstacionamientoService.java

    public EventoEstado procesarEvento(EventoPlacaDTO ev) {
        String placa = ev.getPlate();
        LocalDateTime ahora = LocalDateTime.now();
        LocalDate hoy = ahora.toLocalDate();

        // 0) Intentamos usuario fijo
        Integer idUsuarioFijo = vehDAO.obtenerIdUsuarioFijoPorPlaca(placa);
        Integer idUsuarioBase = null;

        if (idUsuarioFijo != null) {
            idUsuarioBase = usuariosDAO.obtenerUsuarioBasePorFijo(idUsuarioFijo);

        } else {
            // 0b) Si no hay fijo, intentamos **temporal** usando VehiculoDAO
            Integer idUsuarioTemp = vehDAO.obtenerIdUsuarioTemporalPorPlaca(placa);
            if (idUsuarioTemp != null) {
                // comprobamos fechas
                LocalDate inicio = usuariosDAO.obtenerFechaInicioTemporal(idUsuarioTemp);
                LocalDate fin    = usuariosDAO.obtenerFechaFinTemporal(   idUsuarioTemp);
                if ((inicio == null || !hoy.isBefore(inicio))
                        && (fin    == null || !hoy.isAfter(fin))) {
                    // dentro del periodo, mapeamos a usuario base
                    idUsuarioBase = usuariosDAO.obtenerUsuarioBasePorTemporal(idUsuarioTemp);
                }
                // si está fuera del periodo, idUsuarioBase queda null → será DENEGADO
            }
        }

        EventoEstado resultado;

        switch (ev.getGate()) {
            case INGRESO -> {
                if (idUsuarioBase == null || !placasDAO.existePlacaActivaYAsignada(placa)) {
                    resultado = EventoEstado.DENEGADO;
                    int idReg = regPlaDAO.insertarRegistroPlaca(
                            null,
                            resultado.name(),
                            idUsuarioBase == null
                                    ? "Placa no asignada o fuera de periodo"
                                    : "Placa inactiva",
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
                    accesoDAO.insertarAcceso(ahora, "PLACA", idUsuarioBase, idReg, null);
                }
            }
            case SALIDA -> {
                if (idUsuarioBase == null) {
                    resultado = EventoEstado.DENEGADO;
                    int idReg = regPlaDAO.insertarRegistroPlaca(
                            null,
                            resultado.name(),
                            "Salida inválida (usuario desconocido o fuera de periodo)",
                            placa
                    );
                    accesoDAO.insertarAcceso(ahora, "PLACA", null, idReg, null);

                } else {
                    Integer idAccAbierto = accesoDAO.obtenerUltimoIngresoPorUsuario(idUsuarioBase);
                    if (idAccAbierto != null) {
                        resultado = EventoEstado.SALIDA;
                        int idReg = regPlaDAO.insertarRegistroPlaca(
                                placa,
                                resultado.name(),
                                "Detección cámara",
                                placa
                        );
                        accesoDAO.insertarAcceso(ahora, "PLACA", idUsuarioBase, idReg, null);
                        accesoDAO.cerrarSalida(idAccAbierto);
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

        ev.setGate(resultado);
        return resultado;
    }


}
