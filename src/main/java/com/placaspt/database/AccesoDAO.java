package com.placaspt.database;

import com.placaspt.model.AccesoViewDTO;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccesoDAO {

    /**
     * Inserta un registro en la tabla acceso y devuelve su nuevo ID, o -1 en error.
     */
    public int insertarAcceso(LocalDateTime fechaHora,
                              String tipoAcceso,
                              Integer fkUsuario,
                              Integer fkRegistroPlaca,
                              Integer fkRegistroRFID) {
        String sql = """
            INSERT INTO acceso
              (FechaHora, Tipo_Acceso, FK_ID_Usuario,
               FK_ID_RegistroPlaca, FK_ID_RegistroRFID)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(fechaHora));
            ps.setString(2, tipoAcceso);
            if (fkUsuario != null)       ps.setInt(3, fkUsuario);
            else                         ps.setNull(3, Types.INTEGER);
            if (fkRegistroPlaca != null) ps.setInt(4, fkRegistroPlaca);
            else                         ps.setNull(4, Types.INTEGER);
            if (fkRegistroRFID != null)  ps.setInt(5, fkRegistroRFID);
            else                         ps.setNull(5, Types.INTEGER);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Último ingreso por placa (solo para gate=PLACA), retorna ID_Acceso o null.
     */
    public Integer obtenerUltimoIngresoPorPlaca(String placa) {
        String sql = """
            SELECT a.ID_Acceso
              FROM acceso a
              JOIN registroplaca r ON a.FK_ID_RegistroPlaca = r.ID_RegistroPlaca
             WHERE a.Tipo_Acceso = 'PLACA'
               AND r.FK_ID_Placa = ?
               AND r.Estado_Evento = 'INGRESO'
             ORDER BY a.FechaHora DESC
             LIMIT 1
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("ID_Acceso") : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lista todos los accesos de una fecha dada, enriquecido con usuario RFID o propietario de placa.
     */
    public List<AccesoViewDTO> listarAccesosPorFecha(LocalDate fecha) {
        String sql = """
        SELECT
          a.ID_Acceso,
          a.FechaHora,
          -- Usuario: primero RFID, si no, dueño de placa
          COALESCE(
            CONCAT(u1.Nombres,' ',u1.Apellidos),
            CONCAT(u2.Nombres,' ',u2.Apellidos),
            '—'
          ) AS usuario,
          -- Aquí elegimos la “placa”:
          COALESCE(
            rpla.FK_ID_Placa,
            rpla.Placa_Escaneada,
            rfid.Tag_Escaneado,
            rfid.FK_ID_RFID,
            '—'
          ) AS placa,
          a.Tipo_Acceso AS metodo,
          COALESCE(rpla.Estado_Evento, rfid.Estado_Evento, 'DENEGADO') AS estado
        FROM acceso a
        LEFT JOIN registrorfid   rfid ON a.FK_ID_RegistroRFID  = rfid.ID_Registro_RFID
        LEFT JOIN usuario        u1   ON a.FK_ID_Usuario        = u1.ID_Usuario
        LEFT JOIN registroplaca  rpla ON a.FK_ID_RegistroPlaca = rpla.ID_RegistroPlaca
        LEFT JOIN vehiculo       v    ON rpla.FK_ID_Placa       = v.FK_ID_Placa
        LEFT JOIN usuario        u2   ON v.FK_ID_Usuario_Fijo   = u2.ID_Usuario
        WHERE DATE(a.FechaHora) = ?
        ORDER BY a.FechaHora ASC
    """;
        List<AccesoViewDTO> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new AccesoViewDTO(
                            rs.getInt("ID_Acceso"),
                            rs.getTimestamp("FechaHora").toLocalDateTime(),
                            rs.getString("usuario"),
                            rs.getString("placa"),
                            rs.getString("metodo"),
                            rs.getString("estado")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean cerrarSalida(int idAcceso) {
        return true;
    }


    /**
     * Devuelve el ID del último ingreso abierto para un usuario,
     * buscando el Estado_Evento='INGRESO' en el registro hijo correspondiente.
     */
    public Integer obtenerUltimoIngresoPorUsuario(int idUsuario) {
        String sql = """
            SELECT a.ID_Acceso
              FROM acceso a
         LEFT JOIN registroplaca  rp ON a.FK_ID_RegistroPlaca = rp.ID_RegistroPlaca
         LEFT JOIN registrorfid   rf ON a.FK_ID_RegistroRFID  = rf.ID_Registro_RFID
             WHERE a.FK_ID_Usuario = ?
               AND (
                     (a.Tipo_Acceso='PLACA' AND rp.Estado_Evento='INGRESO')
                  OR (a.Tipo_Acceso='RFID'   AND rf.Estado_Evento='INGRESO')
                   )
             ORDER BY a.FechaHora DESC
             LIMIT 1
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("ID_Acceso") : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Devuelve el último Estado_Evento para un usuario,
     * ya sea de registroplaca o registrorfid.
     */
    public String obtenerUltimoEstadoPorUsuario(int idUsuario) {
        String sql = """
            SELECT COALESCE(rp.Estado_Evento, rf.Estado_Evento) AS estado
              FROM acceso a
         LEFT JOIN registroplaca  rp ON a.FK_ID_RegistroPlaca = rp.ID_RegistroPlaca
         LEFT JOIN registrorfid   rf ON a.FK_ID_RegistroRFID  = rf.ID_Registro_RFID
             WHERE a.FK_ID_Usuario = ?
             ORDER BY a.FechaHora DESC
             LIMIT 1
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("estado") : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Cuenta cuántos vehículos están dentro hoy (solo INGRESO sin SALIDA).
     */
    public int contarOcupadosHoy() {
        String sql = """
            SELECT COUNT(*) FROM (
              SELECT FK_ID_Usuario, FK_ID_RegistroRFID
                FROM acceso
               WHERE DATE(FechaHora)=CURDATE()
                 AND Tipo_Acceso='RFID'
                 AND FK_ID_RegistroRFID IS NOT NULL
             UNION ALL
              SELECT FK_ID_Usuario, FK_ID_RegistroPlaca
                FROM acceso
               WHERE DATE(FechaHora)=CURDATE()
                 AND Tipo_Acceso='PLACA'
                 AND FK_ID_RegistroPlaca IS NOT NULL
            ) sub
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Cuenta cuántos vehículos están actualmente dentro del estacionamiento,
     * sumando +1 por cada INGRESO y −1 por cada SALIDA, tanto de PLACA como de RFID.
     */
    public int contarOcupadosActuales() {
        String sql = """
        SELECT COALESCE(SUM(delta), 0) AS ocupados
          FROM (
            -- Eventos de placa
            SELECT CASE
                     WHEN rp.Estado_Evento = 'INGRESO' THEN  1
                     WHEN rp.Estado_Evento = 'SALIDA'  THEN -1
                     ELSE 0
                   END AS delta
              FROM acceso a
              JOIN registroplaca rp
                ON a.FK_ID_RegistroPlaca = rp.ID_RegistroPlaca
             WHERE a.Tipo_Acceso = 'PLACA'
            
            UNION ALL
            
            -- Eventos de RFID
            SELECT CASE
                     WHEN rf.Estado_Evento = 'INGRESO' THEN  1
                     WHEN rf.Estado_Evento = 'SALIDA'  THEN -1
                     ELSE 0
                   END AS delta
              FROM acceso a
              JOIN registrorfid rf
                ON a.FK_ID_RegistroRFID = rf.ID_Registro_RFID
             WHERE a.Tipo_Acceso = 'RFID'
          ) sub;
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("ocupados");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
