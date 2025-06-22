package com.placaspt.database;

import com.placaspt.model.VehiculoPOJO;
import java.sql.*;

public class VehiculoDAO {

    /**
     * Busca el vehículo asociado a un usuario fijo (si existe).
     * @return VehiculoPOJO o null si no hay ninguno.
     */
    public VehiculoPOJO buscarPorUsuarioFijo(int idUsuarioFijo) {
        String sql = """
            SELECT * 
              FROM Vehiculo 
             WHERE FK_ID_Usuario_Fijo = ? 
             LIMIT 1
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuarioFijo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new VehiculoPOJO(
                            rs.getInt("ID_Vehiculo"),
                            rs.getString("Marca"),
                            rs.getString("Modelo"),
                            rs.getInt("Anio"),
                            rs.getString("Color"),
                            rs.getInt("FK_ID_Placa"),
                            rs.getInt("FK_ID_Usuario_Fijo"),
                            (rs.getObject("FK_ID_Usuario_Temporal") != null
                                    ? rs.getInt("FK_ID_Usuario_Temporal") : null),
                            rs.getInt("FK_ID_Administrador")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Si luego quieres, puedes agregar insertarVehiculo(), actualizarVehiculo(), etc.
}
