package com.placaspt.logic;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstacionamientoDAO {

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mariadb://localhost:3306/tu_basedatos",
                "usuario",
                "contraseña"
        );
    }

    public static List<Estacionamiento> buscarPorId(String idParcial) {
        List<Estacionamiento> lista = new ArrayList<>();
        String query = "SELECT * FROM estacionamiento WHERE id_usuario LIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, idParcial + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(new Estacionamiento(
                        rs.getString("id_usuario"),
                        rs.getString("nombre"),
                        rs.getString("estado"),
                        rs.getString("placas"),
                        rs.getString("tipo"),
                        rs.getString("horario")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
