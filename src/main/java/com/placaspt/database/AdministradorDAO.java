package com.placaspt.database;

import com.placaspt.logic.AdministradorPOJO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AdministradorDAO {

    public static boolean insertarAdministrador(AdministradorPOJO admin) {
        String sql = "INSERT INTO Administrador (Nombre, Apellido, Correo, Telefono, Clave) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, admin.getNombre());
            stmt.setString(2, admin.getApellido());
            stmt.setString(3, admin.getCorreo());
            stmt.setString(4, admin.getTelefono());
            stmt.setString(5, admin.getClave());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
