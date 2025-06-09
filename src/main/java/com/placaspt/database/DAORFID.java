package com.placaspt.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DAORFID {

    public static void insertarLectura(String tag) {
        String sql = "INSERT INTO registrorfid (tag, fecha_hora) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tag);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));

            stmt.executeUpdate();

            System.out.println("Lectura RFID registrada correctamente");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
