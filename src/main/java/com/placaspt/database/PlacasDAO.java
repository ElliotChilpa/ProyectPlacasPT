// src/main/java/com/placaspt/database/PlacasDAO.java
package com.placaspt.database;

import com.placaspt.model.PlacasPOJO;
import java.sql.*;

public class PlacasDAO {

    /**
     * Inserta una nueva placa en la tabla placavehicular.
     */
    public boolean insertarPlaca(PlacasPOJO p) {
        String sql = """
            INSERT INTO placavehicular
              (ID_Placa, Entidad_Federativa, Estado_Vigencia)
            VALUES (?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getIdPlaca());
            ps.setString(2, p.getEntidadFederativa());
            // Guardamos "true"/"false" como texto en la columna VARCHAR
            ps.setString(3, p.isEstadoVigencia() ? "true" : "false");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca una placa por su ID. Retorna null si no existe.
     */
    public PlacasPOJO buscarPorId(String idPlaca) {
        String sql = "SELECT * FROM placavehicular WHERE ID_Placa = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idPlaca);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String entidad = rs.getString("Entidad_Federativa");
                    String ev      = rs.getString("Estado_Vigencia");
                    // Convertimos texto a boolean
                    boolean vigente = ev != null && (
                            ev.equalsIgnoreCase("true") || ev.equals("1")
                    );
                    return new PlacasPOJO(idPlaca, entidad, vigente);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Renombra una placa (PK) y actualiza el FK en vehiculo.
     * @param viejaId el ID antiguo de la placa
     * @param nuevaId el nuevo ID que quieres asignar
     * @return true si ambas actualizaciones tuvieron éxito
     */
    public boolean renombrarPlaca(String viejaId, String nuevaId) {
        String sqlPlaca = """
            UPDATE placavehicular
               SET ID_Placa = ?
             WHERE ID_Placa = ?
        """;
        String sqlVeh = """
            UPDATE vehiculo
               SET FK_ID_Placa = ?
             WHERE FK_ID_Placa = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlPlaca);
                 PreparedStatement ps2 = conn.prepareStatement(sqlVeh)) {

                // 1) Actualiza la PK en placavehicular
                ps1.setString(1, nuevaId);
                ps1.setString(2, viejaId);
                ps1.executeUpdate();

                // 2) Actualiza el FK en vehiculo
                ps2.setString(1, nuevaId);
                ps2.setString(2, viejaId);
                ps2.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza datos de una placa existente.
     */
    public boolean actualizarPlaca(PlacasPOJO p) {
        String sql = """
            UPDATE placavehicular SET
              Entidad_Federativa = ?, Estado_Vigencia = ?
            WHERE ID_Placa = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getEntidadFederativa());
            ps.setString(2, p.isEstadoVigencia() ? "true" : "false");
            ps.setString(3, p.getIdPlaca());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Comprueba si una placa activa existe en la tabla placavehicular.
     */
    public boolean existePlaca(String idPlaca) {
        String sql = """
            SELECT 1
              FROM placavehicular
             WHERE ID_Placa = ?
               AND Estado_Vigencia = 'true'
             LIMIT 1
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idPlaca);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
