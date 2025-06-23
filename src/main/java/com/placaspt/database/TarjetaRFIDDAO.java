/*package com.placaspt.database;

import com.placaspt.model.TarjetaRFIDPOJO;
import java.sql.*;
import java.time.LocalDate;

public class TarjetaRFIDDAO {

    /**
     * Comprueba si existe una tarjeta con ese ID_RFID.
     * /
    public boolean existeTag(String tag) {
        String sql = "SELECT 1 FROM TarjetaRFID WHERE ID_RFID = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tag);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inserta una nueva tarjeta RFID en la base.
     * /
    public boolean insertarTarjeta(TarjetaRFIDPOJO t) {
        String sql = """
            INSERT INTO TarjetaRFID
              (ID_RFID, Fecha_Inicio, Fecha_Fin, Estado_Vigencia,
               FK_Administrador, FK_Usuario_Fijo)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getIdRfid());
            ps.setDate  (2, Date.valueOf(t.getFechaInicio()));
            ps.setDate  (3, Date.valueOf(t.getFechaFin()));
            ps.setBoolean(4, t.isActiva());
            ps.setInt   (5, t.getFkAdministrador());
            ps.setInt   (6, t.getFkUsuarioFijo());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza el rango de fechas y estado de una tarjeta existente.
     * /
    public boolean actualizarTarjeta(TarjetaRFIDPOJO t) {
        String sql = """
            UPDATE TarjetaRFID
               SET Fecha_Inicio   = ?,
                   Fecha_Fin      = ?,
                   Estado_Vigencia= ?
             WHERE ID_RFID = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate   (1, Date.valueOf(t.getFechaInicio()));
            ps.setDate   (2, Date.valueOf(t.getFechaFin()));
            ps.setBoolean(3, t.isActiva());
            ps.setString (4, t.getIdRfid());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca la tarjeta RFID asignada a un usuario fijo.
     * @return POJO o null si no existe.
     * /
    public TarjetaRFIDPOJO buscarPorUsuarioFijo(int idUsuarioFijo) {
        String sql = "SELECT * FROM TarjetaRFID WHERE FK_Usuario_Fijo = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuarioFijo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TarjetaRFIDPOJO(
                            rs.getString("ID_RFID"),
                            rs.getDate("Fecha_Inicio").toLocalDate(),
                            rs.getDate("Fecha_Fin").toLocalDate(),
                            rs.getBoolean("Estado_Vigencia"),
                            rs.getInt("FK_Administrador"),
                            idUsuarioFijo
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}*/



package com.placaspt.database;

import com.placaspt.model.TarjetaRFIDPOJO;
import java.sql.*;

public class TarjetaRFIDDAO {

    /** Comprueba si existe una tarjeta con ese ID_RFID. */
    public boolean existeTag(String tag) {
        String sql = "SELECT 1 FROM TarjetaRFID WHERE ID_RFID = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tag);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Inserta una nueva tarjeta RFID. */
    public boolean insertarTarjeta(TarjetaRFIDPOJO t) {
        String sql = """
            INSERT INTO TarjetaRFID
              (ID_RFID, Fecha_Inicio, Fecha_Fin, Estado_Vigencia,
               FK_Administrador, FK_Usuario_Fijo)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getIdRfid());
            ps.setDate  (2, Date.valueOf(t.getFechaInicio()));
            ps.setDate  (3, Date.valueOf(t.getFechaFin()));
            ps.setBoolean(4, t.isActiva());
            ps.setInt   (5, t.getFkAdministrador());
            ps.setInt   (6, t.getFkUsuarioFijo());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza todos los campos de la tarjeta, **incluyendo el ID_RFID**.
     * @param oldId la clave primaria antigua (ID_RFID previo)
     * @param t     el nuevo objeto con ID_RFID, fechas, estado y FKs
     */
    public boolean actualizarTarjeta(String oldId, TarjetaRFIDPOJO t) {
        String sql = """
            UPDATE TarjetaRFID
               SET ID_RFID         = ?,
                   Fecha_Inicio    = ?,
                   Fecha_Fin       = ?,
                   Estado_Vigencia = ?,
                   FK_Administrador = ?,
                   FK_Usuario_Fijo  = ?
             WHERE ID_RFID = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getIdRfid());
            ps.setDate  (2, Date.valueOf(t.getFechaInicio()));
            ps.setDate  (3, Date.valueOf(t.getFechaFin()));
            ps.setBoolean(4, t.isActiva());
            ps.setInt   (5, t.getFkAdministrador());
            ps.setInt   (6, t.getFkUsuarioFijo());
            ps.setString(7, oldId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza únicamente fechas y estado de una tarjeta existente,
     * sin tocar el ID_RFID ni las FKs.
     */
    public boolean actualizarFechasYEstado(TarjetaRFIDPOJO t) {
        String sql = """
            UPDATE TarjetaRFID
               SET Fecha_Inicio    = ?,
                   Fecha_Fin       = ?,
                   Estado_Vigencia = ?
             WHERE ID_RFID = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate   (1, Date.valueOf(t.getFechaInicio()));
            ps.setDate   (2, Date.valueOf(t.getFechaFin()));
            ps.setBoolean(3, t.isActiva());
            ps.setString (4, t.getIdRfid());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca la tarjeta RFID asignada a un usuario fijo.
     * @return un POJO con todos los campos, o null si no existe.
     */
    public TarjetaRFIDPOJO buscarPorUsuarioFijo(int idUsuarioFijo) {
        String sql = "SELECT * FROM TarjetaRFID WHERE FK_Usuario_Fijo = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuarioFijo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TarjetaRFIDPOJO(
                            rs.getString("ID_RFID"),
                            rs.getDate("Fecha_Inicio").toLocalDate(),
                            rs.getDate("Fecha_Fin").toLocalDate(),
                            rs.getBoolean("Estado_Vigencia"),
                            rs.getInt("FK_Administrador"),
                            idUsuarioFijo
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
