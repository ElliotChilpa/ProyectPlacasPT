package com.placaspt.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TarjetaRFIDDAO {

    /**
     * Comprueba si el tag existe en la tabla TarjetaRFID.
     * @param tag El valor del ID_RFID (VARCHAR).
     * @return true si existe, false si no.
     */
    public static boolean existeTag(String tag) {
        String sql = "SELECT 1 FROM TarjetaRFID WHERE ID_RFID = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tag);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
