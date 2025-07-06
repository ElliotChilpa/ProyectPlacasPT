package com.placaspt.database;

import java.sql.*;

public class RegistroPlacaDAO {

    /**
     * Inserta un evento en registroplaca y devuelve el nuevo ID_RegistroPlaca,
     * o -1 si hubo un error.
     */
    public int insertarRegistroPlaca(String idPlaca, String estado, String descripcion, String placaEscaneada) {
        String sql = """
            INSERT INTO registroplaca
              (Fecha_Registro, Estado_Evento, Descripcion_Evento, FK_ID_Placa, Placa_Escaneada)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setString   (2, estado);
            ps.setString   (3, descripcion);
            ps.setString   (4, idPlaca);
            /*
            if (idPlaca != null) {
                ps.setString(4, idPlaca);
            } else {
                ps.setNull(4, Types.VARCHAR);
            }*/
            ps.setString(5, placaEscaneada);

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
     * Dado un ID_RegistroPlaca devuelve la placa (FK_ID_Placa) almacenada,
     * o null si no lo encuentra.
     */
    public String buscarPlacaPorId(int idRegistroPlaca) {
        String sql = "SELECT FK_ID_Placa FROM registroplaca WHERE ID_RegistroPlaca = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRegistroPlaca);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("FK_ID_Placa");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
