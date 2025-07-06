/*package com.placaspt.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class RegistroRFIDDAO {

    public static void insertarRegistro(String idRFID, int idUsuario, String estado, String descripcion) {
        String sql = "INSERT INTO RegistroRFID (Fecha_Registro, Estado_Evento, Descripcion_Evento, FK_Usuario_Fijo, FK_ID_RFID) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, estado);
            stmt.setString(3, descripcion);
            stmt.setInt(4, idUsuario);
            //stmt.setInt(5, idRFID);
            stmt.setString(5, idRFID);


            stmt.executeUpdate();

            System.out.println("Registro RFID insertado correctamente");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}*/
package com.placaspt.database;

import java.sql.*;

public class RegistroRFIDDAO {

    /**
     * Inserta un evento en registrorfid y devuelve el nuevo ID_Registro_RFID,
     * o -1 si hubo un error.
     */
    public int insertarRegistroRFID(String idRfid, String estado, String descripcion, String tagEscaneado) {
        String sql = """
            INSERT INTO registrorfid
              (Fecha_Registro, Estado_Evento, Descripcion_Evento, FK_ID_RFID, Tag_Escaneado)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setString   (2, estado);
            ps.setString   (3, descripcion);
            ps.setString   (4, idRfid);
            ps.setString(5, tagEscaneado);    // <-- asignamos el nuevo campo

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
     */
    public String buscarTagPorId(int idRegistroRfid) {
        String sql = "SELECT FK_ID_RFID FROM registrorfid WHERE ID_Registro_RFID = ?";
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
}
