package com.placaspt.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UsuarioDAO {

    public static void listarUsuarios() {
        String query = "SELECT * FROM usuario";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("ID_Usuario");  // Ajusta según los nombres reales de tus columnas
                String nombres = rs.getString("Nombres");
                String correo = rs.getString("Correo");

                System.out.println("ID: " + id + ", Nombre: " + nombres + ", Correo: " + correo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
