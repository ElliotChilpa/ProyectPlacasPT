/*package com.placaspt.database;

import java.sql.*;

public class RegistroRFIDDAO {

    /**
     * Inserta un evento en registrorfid (sin tagEscaneado) y devuelve el nuevo ID,
     * o -1 si hubo un error.
     * /
    public int insertarRegistroRFID(String tag, String estado, String descripcion) {
        // delega a la versión de 4 parámetros, sin tagEscaneado
        return insertarRegistroRFID(tag, estado, descripcion, null);
    }

    /**
     * Inserta un evento en registrorfid y devuelve el nuevo ID_Registro_RFID,
     * o -1 si hubo un error.
     *
     * @param tag           el valor crudo leído (se usará para buscar FK_ID_RFID)
     * @param estado        "INGRESO", "SALIDA" o "DENEGADO"
     * @param descripcion   descripción del evento
     * @param tagEscaneado  el tag tal cual llegó (puede ser null)
     * /
    public int insertarRegistroRFID(String tag,
                                    String estado,
                                    String descripcion,
                                    String tagEscaneado) {
        String sql = """
            INSERT INTO registrorfid
              (Fecha_Registro, Estado_Evento, Descripcion_Evento,
               FK_ID_RFID, Tag_Escaneado)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 1) Fecha
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            // 2) Estado_Evento
            ps.setString   (2, estado);
            // 3) Descripcion_Evento
            ps.setString   (3, descripcion);

            // 4) FK_ID_RFID: buscamos el ID de la tarjeta por su tag
            Integer idTarjeta = buscarIdTarjetaPorTag(tag);
            if (idTarjeta != null) {
                ps.setInt(4, idTarjeta);
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            // 5) Tag_Escaneado
            if (tagEscaneado != null) {
                ps.setString(5, tagEscaneado);
            } else {
                ps.setNull(5, Types.VARCHAR);
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
     * Dado un ID_Registro_RFID devuelve el tag (FK_ID_RFID) almacenado,
     * o null si no lo encuentra.
     * /
    public String buscarTagPorId(int idRegistroRfid) {
        String sql = """
            SELECT FK_ID_RFID
              FROM registrorfid
             WHERE ID_Registro_RFID = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRegistroRfid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("FK_ID_RFID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Busca el ID_RFID (PK) correspondiente a un tag de tarjeta dado.
     * Retorna null si no lo encuentra.
     * /
    private Integer buscarIdTarjetaPorTag(String tag) {
        //String sql = "SELECT ID_RFID FROM tarjetarfid WHERE Tag = ?";
        String sql = """
        SELECT FK_ID_RFID
          FROM registrorfid
-        WHERE Tag = ?
+        WHERE Tag_Escaneado = ?
         LIMIT 1
    """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tag);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("ID_RFID") : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}*/
package com.placaspt.database;

import java.sql.*;

public class RegistroRFIDDAO {

    /**
     * Inserta un evento en registrorfid (ahora con Tag_Escaneado) y devuelve el nuevo ID, o -1 en error.
     */
    public int insertarRegistroRFID(String tag,
                                    String estado,
                                    String descripcion,
                                    String tagEscaneado) {
        String sql = """
            INSERT INTO registrorfid
              (Fecha_Registro, Estado_Evento, Descripcion_Evento,
               FK_ID_RFID, Tag_Escaneado)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setString   (2, estado);
            ps.setString   (3, descripcion);

            // 4) FK_ID_RFID
            String idTarjeta = buscarIdTarjetaPorTag(tag);
            if (idTarjeta != null) {
                ps.setString(4, idTarjeta);
            } else {
                ps.setNull(4, Types.VARCHAR);
            }

            // 5) Tag_Escaneado
            if (tagEscaneado != null) {
                ps.setString(5, tagEscaneado);
            } else {
                ps.setNull(5, Types.VARCHAR);
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

    // sobrecarga para no-break con 3 params:
    public int insertarRegistroRFID(String tag, String estado, String descripcion) {
        return insertarRegistroRFID(tag, estado, descripcion, null);
    }

    /**
     * Dado un ID_Registro_RFID devuelve el Tag_Escaneado o FK_ID_RFID, según necesites.
     */
    public String buscarTagPorId(int idRegistroRfid) {
        String sql = """
            SELECT Tag_Escaneado, FK_ID_RFID
              FROM registrorfid
             WHERE ID_Registro_RFID = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRegistroRfid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // si quieres el valor “crudo” que escaneaste, devuelve Tag_Escaneado:
                    String tagEsc = rs.getString("Tag_Escaneado");
                    if (tagEsc != null) return tagEsc;
                    // si no, el FK_ID_RFID:
                    return rs.getString("FK_ID_RFID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Busca el ID_RFID (PK) correspondiente a un tag de tarjeta dado,
     * consultando la tabla tarjetarfid. Retorna null si no lo encuentra.
     */
    /**
     * Dado un tag crudo, devuelve el ID_RFID (string) si existe en tarjetarfid, o null.
     */
    private String buscarIdTarjetaPorTag(String tag) {
        String sql = """
            SELECT ID_RFID
              FROM tarjetarfid
             WHERE ID_RFID = ?
             LIMIT 1
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tag);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("ID_RFID") : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
