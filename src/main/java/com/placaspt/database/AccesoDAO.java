/*package com.placaspt.database;

import com.placaspt.model.AccesoViewDTO;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccesoDAO {

    /**
     * Inserta un registro en la tabla acceso y devuelve su nuevo ID, o -1 en error.
     * /
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
            ps.setString   (2, tipoAcceso);

            if (fkUsuario != null) {
                ps.setInt(3, fkUsuario);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            if (fkRegistroPlaca != null) {
                ps.setInt(4, fkRegistroPlaca);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (fkRegistroRFID != null) {
                ps.setInt(5, fkRegistroRFID);
            } else {
                ps.setNull(5, Types.INTEGER);
            }

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
     * Lista todos los accesos de una fecha dada, en orden cronológico.
     * /
    public List<AccesoViewDTO> listarAccesosPorFecha(LocalDate fecha) {
        String sql = """
            SELECT a.ID_Acceso,
                   a.FechaHora,
                   CONCAT(u.Nombres, ' ', u.Apellidos) AS usuario,
                   COALESCE(rpla.FK_ID_Placa, rfid.FK_ID_RFID) AS placa,
                   a.Tipo_Acceso AS metodo,
                   COALESCE(rpla.Estado_Evento, rfid.Estado_Evento) AS estado
              FROM acceso a
         LEFT JOIN registroplaca  rpla ON a.FK_ID_RegistroPlaca = rpla.ID_RegistroPlaca
         LEFT JOIN registrorfid   rfid ON a.FK_ID_RegistroRFID  = rfid.ID_Registro_RFID
         LEFT JOIN usuario        u    ON a.FK_ID_Usuario        = u.ID_Usuario
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

    /**
     * Cierra la salida de un acceso existente:
     *  - Crea un nuevo evento SALIDA en registroRFID o registroplaca
     *  - Inserta un nuevo registro en acceso con el mismo usuario y el nuevo FK_Registro
     * /
    public boolean cerrarSalida(int idAcceso) {
        // 1) Recuperar datos del acceso original
        String sqlFetch = "SELECT * FROM acceso WHERE ID_Acceso = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psFetch = conn.prepareStatement(sqlFetch)) {

            psFetch.setInt(1, idAcceso);
            try (ResultSet rs = psFetch.executeQuery()) {
                if (!rs.next()) return false;

                String tipo       = rs.getString("Tipo_Acceso");
                Integer fkUsuario = rs.getObject("FK_ID_Usuario", Integer.class);
                Integer fkRegPla  = rs.getObject("FK_ID_RegistroPlaca", Integer.class);
                Integer fkRegRfid = rs.getObject("FK_ID_RegistroRFID", Integer.class);

                // 2) Registrar evento de SALIDA
                int newReg;
                if ("RFID".equals(tipo) && fkRegRfid != null) {
                    // obtenemos el tag para pasarlo a la descripción (opcional)
                    String tag = new RegistroRFIDDAO().buscarTagPorId(fkRegRfid);
                    newReg = new RegistroRFIDDAO()
                            .insertarRegistroRFID(tag, "SALIDA", "Cierre manual");
                    fkRegPla = null;
                } else if ("PLACA".equals(tipo) && fkRegPla != null) {
                    String placa = new RegistroPlacaDAO().buscarPlacaPorId(fkRegPla);
                    newReg = new RegistroPlacaDAO()
                            .insertarRegistroPlaca(placa, "SALIDA", "Cierre manual");
                    fkRegRfid = null;
                } else {
                    return false;
                }

                if (newReg < 0) return false;

                // 3) Insertar nuevo acceso
                int idNewAcceso = insertarAcceso(
                        LocalDateTime.now(),
                        tipo,
                        fkUsuario,
                        fkRegPla,
                        fkRegRfid != null ? newReg : null
                );
                return idNewAcceso > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cuenta cuántos vehículos están dentro en una fecha (solo INGRESO sin SALIDA).
     * /
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
}
*/
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
            ps.setString   (2, tipoAcceso);

            if (fkUsuario != null) {
                ps.setInt(3, fkUsuario);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            if (fkRegistroPlaca != null) {
                ps.setInt(4, fkRegistroPlaca);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (fkRegistroRFID != null) {
                ps.setInt(5, fkRegistroRFID);
            } else {
                ps.setNull(5, Types.INTEGER);
            }

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
     * Devuelve el ID del último acceso de tipo PLACA cuya placa coincide
     * y cuyo evento en registroplaca fue INGRESO. Retorna null si no existe.
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
     * Lista todos los accesos de una fecha dada, en orden cronológico.
     */
    public List<AccesoViewDTO> listarAccesosPorFecha(LocalDate fecha) {
        String sql = """
            SELECT a.ID_Acceso,
                   a.FechaHora,
                   CONCAT(u.Nombres, ' ', u.Apellidos) AS usuario,
                   COALESCE(rpla.FK_ID_Placa, rfid.FK_ID_RFID) AS placa,
                   a.Tipo_Acceso AS metodo,
                   COALESCE(rpla.Estado_Evento, rfid.Estado_Evento) AS estado
              FROM acceso a
         LEFT JOIN registroplaca  rpla ON a.FK_ID_RegistroPlaca = rpla.ID_RegistroPlaca
         LEFT JOIN registrorfid   rfid ON a.FK_ID_RegistroRFID  = rfid.ID_Registro_RFID
         LEFT JOIN usuario        u    ON a.FK_ID_Usuario        = u.ID_Usuario
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

    /**
     * Cierra la salida de un acceso existente:
     *  - Crea un nuevo evento SALIDA en registroRFID o registroplaca
     *  - Inserta un nuevo registro en acceso con el mismo usuario y el nuevo FK_Registro
     */
    public boolean cerrarSalida(int idAcceso) {
        // 1) Recuperar datos del acceso original
        String sqlFetch = "SELECT * FROM acceso WHERE ID_Acceso = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psFetch = conn.prepareStatement(sqlFetch)) {

            psFetch.setInt(1, idAcceso);
            try (ResultSet rs = psFetch.executeQuery()) {
                if (!rs.next()) return false;

                String tipo       = rs.getString("Tipo_Acceso");
                Integer fkUsuario = rs.getObject("FK_ID_Usuario", Integer.class);
                Integer fkRegPla  = rs.getObject("FK_ID_RegistroPlaca", Integer.class);
                Integer fkRegRfid = rs.getObject("FK_ID_RegistroRFID", Integer.class);

                // 2) Registrar evento de SALIDA
                int newReg;
                if ("RFID".equals(tipo) && fkRegRfid != null) {
                    // Tag y registro de SALIDA en RFID
                    String tag = new RegistroRFIDDAO().buscarTagPorId(fkRegRfid);
                    newReg = new RegistroRFIDDAO()
                            .insertarRegistroRFID(tag, "SALIDA", "Cierre manual");
                    fkRegPla = null;
                } else if ("PLACA".equals(tipo) && fkRegPla != null) {
                    // Placa y registro de SALIDA en placa
                    String placa = new RegistroPlacaDAO().buscarPlacaPorId(fkRegPla);
                    newReg = new RegistroPlacaDAO()
                            .insertarRegistroPlaca(placa, "SALIDA", "Cierre manual");
                    fkRegRfid = null;
                } else {
                    return false;
                }

                if (newReg < 0) return false;

                // 3) Insertar nuevo acceso con el registro de SALIDA
                int idNewAcceso = insertarAcceso(
                        LocalDateTime.now(),
                        tipo,
                        fkUsuario,
                        fkRegPla,
                        fkRegRfid != null ? newReg : null
                );
                return idNewAcceso > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cuenta cuántos vehículos están dentro en una fecha (solo INGRESO sin SALIDA).
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
}
