/*package com.placaspt.logic;

import com.placaspt.database.*;
import com.placaspt.model.EventoPlacaDTO;

import java.time.LocalDateTime;

/**
 * Servicio de negocio para gestionar accesos por RFID o por placa.
 * /
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
     * /
    public void procesarRfid(String tag) {
        LocalDateTime ahora = LocalDateTime.now();

        // 1) ¿Ese tag está asignado a un usuario fijo?
        int idUsuarioFijo = tarjetaDAO.obtenerIdUsuarioFijoPorTag(tag);
        if (idUsuarioFijo <= 0) {
            int idReg = regRfidDAO.insertarRegistroRFID(tag, "DENEGADO", "Tag no registrado");
            accesoDAO.insertarAcceso(ahora, "RFID", null, null, idReg);
            return;
        }

        // 2) Convertir a usuario base
        Integer idUsuario = usuariosDAO.obtenerUsuarioBasePorFijo(idUsuarioFijo);
        if (idUsuario == null || idUsuario < 0) {
            int idReg = regRfidDAO.insertarRegistroRFID(tag, "DENEGADO", "Usuario no encontrado");
            accesoDAO.insertarAcceso(ahora, "RFID", null, null, idReg);
            return;
        }

        // 3) ¿Cuál fue su último estado?
        String ultimo = accesoDAO.obtenerUltimoEstadoPorUsuario(idUsuario);
        boolean estabaDentro = "INGRESO".equalsIgnoreCase(ultimo);

        // 4) Decidir nuevo estado
        String nuevoEstado = estabaDentro ? "SALIDA" : "INGRESO";
        String descripcion = estabaDentro
                ? "Salida detección RFID"
                : "Detección RFID";

        // 5) Guardar en registrorfid
        int idRegRfid = regRfidDAO.insertarRegistroRFID(tag, nuevoEstado, descripcion);

        // 6) Guardar en acceso
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
     * /
    public void procesarEvento(EventoPlacaDTO ev) {
        String placa = ev.getPlate();
        //  <-- sustituimos ev.getTime() por ahora local
        LocalDateTime ts = LocalDateTime.now();

        // 1) ¿A qué usuario fijo pertenece esta placa?
        Integer idUsuarioFijo = vehDAO.obtenerIdUsuarioFijoPorPlaca(placa);
        if (idUsuarioFijo == null) {
            // placa sin asignar → DENEGADO
            int idRegPla = regPlaDAO.insertarRegistroPlaca(
                    placa, "DENEGADO", "Placa no asignada a usuario fijo"
            );
            accesoDAO.insertarAcceso(ts, "PLACA", null, idRegPla, null);
            return;
        }

        // 2) Convertir a usuario base
        Integer idUsuario = usuariosDAO.obtenerUsuarioBasePorFijo(idUsuarioFijo);
        if (idUsuario == null || idUsuario < 0) {
            int idRegPla = regPlaDAO.insertarRegistroPlaca(
                    placa, "DENEGADO", "Usuario fijo no encontrado"
            );
            //accesoDAO.insertarAcceso(ts, "PLACA", null, idRegPla, null);
            // placa desconocida → sólo “acceso DENEGADO”
            accesoDAO.insertarAcceso(
                    LocalDateTime.now(),
                    "PLACA",
                    null,   // sin FK_ID_Usuario
                    null,   // sin FK_ID_RegistroPlaca
                    null    // sin FK_ID_RegistroRFID
            );
            return;
        }

        // 3) ¿Tiene ya un ingreso abierto?
        Integer idAccesoAbierto = accesoDAO.obtenerUltimoIngresoPorUsuario(idUsuario);
        if (idAccesoAbierto != null) {
            // === SALIDA automática ===
            int idRegPla = regPlaDAO.insertarRegistroPlaca(
                    placa, "SALIDA", "Detección cámara (auto-salida)"
            );
            // aquí cerramos el acceso anterior y al mismo tiempo INSERTAMOS
            // un nuevo "SALIDA" en la tabla acceso para que aparezca en tu tabla:
            accesoDAO.insertarAcceso(ts, "PLACA", idUsuario, idRegPla, null);
            accesoDAO.cerrarSalida(idAccesoAbierto);
        } else {
            // === INGRESO o DENEGADO ===
            if (!placasDAO.existePlacaActivaYAsignada(placa)) {
                // DENEGADO por placa inactiva o inexistente
                int idRegPla = regPlaDAO.insertarRegistroPlaca(
                        placa, "DENEGADO", "Placa no válida o inactiva"
                );
                accesoDAO.insertarAcceso(ts, "PLACA", null, idRegPla, null);
            } else {
                // INGRESO válido
                int idRegPla = regPlaDAO.insertarRegistroPlaca(
                        placa, "INGRESO", "Detección cámara"
                );
                accesoDAO.insertarAcceso(ts, "PLACA", idUsuario, idRegPla, null);
            }
        }
    }

}*/
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
            // tag desconocido → sólo acceso DENEGADO
            int idReg = regRfidDAO.insertarRegistroRFID(tag, "DENEGADO", "Tag no registrado");
            accesoDAO.insertarAcceso(ahora, "RFID", null, null, idReg);
            return;
        }

        // 2) Convertir a usuario base
        Integer idUsuario = usuariosDAO.obtenerUsuarioBasePorFijo(idUsuarioFijo);
        if (idUsuario == null || idUsuario < 0) {
            int idReg = regRfidDAO.insertarRegistroRFID(tag, "DENEGADO", "Usuario no encontrado");
            accesoDAO.insertarAcceso(ahora, "RFID", null, null, idReg);
            return;
        }

        // 3) ¿Cuál fue su último estado?
        String ultimo = accesoDAO.obtenerUltimoEstadoPorUsuario(idUsuario);
        boolean estabaDentro = "INGRESO".equalsIgnoreCase(ultimo);

        // 4) Decidir nuevo estado
        String nuevoEstado  = estabaDentro ? "SALIDA" : "INGRESO";
        String descripcion  = estabaDentro
                ? "Salida detección RFID"
                : "Detección RFID";

        // 5) Guardar en registrorfid
        int idRegRfid = regRfidDAO.insertarRegistroRFID(tag, nuevoEstado, descripcion);

        // 6) Guardar en acceso
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
        LocalDateTime ts = LocalDateTime.now();  // usamos hora local

        // 1) ¿A qué usuario fijo pertenece esta placa?
        Integer idUsuarioFijo = vehDAO.obtenerIdUsuarioFijoPorPlaca(placa);
        if (idUsuarioFijo == null) {
            // placa desconocida → sólo acceso DENEGADO
            accesoDAO.insertarAcceso(
                    ts,
                    "PLACA",
                    null,  // sin FK_ID_Usuario
                    null,  // sin FK_ID_RegistroPlaca
                    null   // sin FK_ID_RegistroRFID
            );
            return;
        }

        // 2) Convertir a usuario base
        Integer idUsuario = usuariosDAO.obtenerUsuarioBasePorFijo(idUsuarioFijo);
        if (idUsuario == null || idUsuario < 0) {
            // usuario fijo roto → sólo acceso DENEGADO
            accesoDAO.insertarAcceso(
                    ts,
                    "PLACA",
                    null,
                    null,
                    null
            );
            return;
        }

        // 3) ¿Tiene ya un ingreso abierto?
        Integer idAccesoAbierto = accesoDAO.obtenerUltimoIngresoPorUsuario(idUsuario);
        if (idAccesoAbierto != null) {
            // === SALIDA automática ===
            int idRegPla = regPlaDAO.insertarRegistroPlaca(
                    placa, "SALIDA", "Detección cámara (auto-salida)"
            );
            // insertamos la SALIDA en acceso
            accesoDAO.insertarAcceso(ts, "PLACA", idUsuario, idRegPla, null);
        } else {
            // === INGRESO o DENEGADO ===
            if (!placasDAO.existePlacaActivaYAsignada(placa)) {
                // placa inactiva → acceso DENEGADO
                accesoDAO.insertarAcceso(
                        ts,
                        "PLACA",
                        null,
                        null,
                        null
                );
            } else {
                // INGRESO válido
                int idRegPla = regPlaDAO.insertarRegistroPlaca(
                        placa, "INGRESO", "Detección cámara"
                );
                accesoDAO.insertarAcceso(
                        ts,
                        "PLACA",
                        idUsuario,
                        idRegPla,
                        null
                );
            }
        }
    }
}

