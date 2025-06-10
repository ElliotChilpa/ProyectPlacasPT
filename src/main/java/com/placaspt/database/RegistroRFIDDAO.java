package com.placaspt.database;

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
}
