package com.placaspt.logic;

import com.placaspt.database.*;
import com.placaspt.model.EventoEstado;
import com.placaspt.model.EventoPlacaDTO;
import com.placaspt.util.FuzzyMatch;     // << tu clase utilitaria
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class EstacionamientoService {

    private final TarjetaRFIDDAO   tarjetaDAO   = new TarjetaRFIDDAO();
    private final RegistroRFIDDAO  regRfidDAO   = new RegistroRFIDDAO();
    private final PlacasDAO        placasDAO    = new PlacasDAO();
    private final RegistroPlacaDAO regPlaDAO    = new RegistroPlacaDAO();
    private final AccesoDAO        accesoDAO    = new AccesoDAO();
    private final UsuariosDAO      usuariosDAO  = new UsuariosDAO();
    private final VehiculoDAO      vehDAO       = new VehiculoDAO();

    // umbral de Levenshtein máximo (≤1 carácter de diferencia)
    private static final int LEVENSHTEIN_UMBRAL = 2;


    public EventoEstado procesarEvento(EventoPlacaDTO ev) {
        // ————————————————
        // 0) CORRECCIÓN FUZZY
        // ————————————————
        String leida = ev.getPlate();
        List<String> placasActivas = placasDAO.listarTodasLasPlacasActivas();
        String corregida = null;
        for (String p : placasActivas) {
            if (FuzzyMatch.closeMatch(leida, p, LEVENSHTEIN_UMBRAL)) {
                corregida = p;
                break;
            }
        }
        if (corregida != null) {
            ev.setPlate(corregida);
        }
        String placa = ev.getPlate();

        // ————————————————
        // 1) Identificar usuario
        // ————————————————
        LocalDateTime ahora = LocalDateTime.now();
        LocalDate hoy       = ahora.toLocalDate();

        Integer idUsuarioBase = null;
        Integer idUsuarioFijo = vehDAO.obtenerIdUsuarioFijoPorPlaca(placa);
        if (idUsuarioFijo != null) {
            idUsuarioBase = usuariosDAO.obtenerUsuarioBasePorFijo(idUsuarioFijo);
        } else {
            Integer idUsuarioTemp = vehDAO.obtenerIdUsuarioTemporalPorPlaca(placa);
            if (idUsuarioTemp != null) {
                LocalDate inicio = usuariosDAO.obtenerFechaInicioTemporal(idUsuarioTemp);
                LocalDate fin    = usuariosDAO.obtenerFechaFinTemporal(idUsuarioTemp);
                if ((inicio == null || !hoy.isBefore(inicio))
                        && (fin    == null || !hoy.isAfter(fin))) {
                    idUsuarioBase = usuariosDAO.obtenerUsuarioBasePorTemporal(idUsuarioTemp);
                }
            }
        }

        // ————————————————
        // 2) Procesar segun gate
        // ————————————————
        EventoEstado resultado;
        switch (ev.getGate()) {

            case INGRESO: {
                // Primero: ausencia de usuario o placa inactiva → DENEGADO
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
                    break;
                }

                // Sólo ahora sabemos que idUsuarioBase != null, podemos chequear duplicados
                String ultimo = accesoDAO.obtenerUltimoEstadoPorUsuario(idUsuarioBase);
                if ("INGRESO".equalsIgnoreCase(ultimo)) {
                    resultado = EventoEstado.INGRESO;
                    break;
                }

                // Finalmente: ingreso válido
                resultado = EventoEstado.INGRESO;
                int idReg = regPlaDAO.insertarRegistroPlaca(
                        placa,
                        resultado.name(),
                        "Detección cámara",
                        placa
                );
                accesoDAO.insertarAcceso(ahora, "PLACA", idUsuarioBase, idReg, null);
                break;
            }

            case SALIDA: {
                // Primero: ausencia de usuario → DENEGADO
                if (idUsuarioBase == null) {
                    resultado = EventoEstado.DENEGADO;
                    int idReg = regPlaDAO.insertarRegistroPlaca(
                            null,
                            resultado.name(),
                            "Salida inválida (usuario desconocido o fuera de periodo)",
                            placa
                    );
                    accesoDAO.insertarAcceso(ahora, "PLACA", null, idReg, null);
                    break;
                }

                // Ahora sabemos que idUsuarioBase != null, checamos duplicados
                String ultimo = accesoDAO.obtenerUltimoEstadoPorUsuario(idUsuarioBase);
                if ("SALIDA".equalsIgnoreCase(ultimo)) {
                    resultado = EventoEstado.SALIDA;
                    break;
                }

                // Lógica original de SALIDA
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
                break;
            }

            default: {
                // Cualquier otro gate lo marcamos DENEGADO
                resultado = EventoEstado.DENEGADO;
                int idReg = regPlaDAO.insertarRegistroPlaca(
                        null,
                        resultado.name(),
                        "Gate inválido: " + ev.getGate(),
                        placa
                );
                accesoDAO.insertarAcceso(ahora, "PLACA", null, idReg, null);
                break;
            }
        }

        ev.setGate(resultado);
        return resultado;
    }



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
}
