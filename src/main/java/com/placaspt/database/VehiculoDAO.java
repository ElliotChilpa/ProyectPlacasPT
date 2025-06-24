package com.placaspt.database;

import com.placaspt.model.VehiculoPOJO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAO {

    /**
     * Inserta un nuevo vehículo asociado a un usuario fijo o temporal.
     */
    public boolean insertarVehiculo(VehiculoPOJO v) {
        String sql = """
            INSERT INTO vehiculo
              (Marca, Modelo, Año, Color,
               FK_ID_Placa, FK_ID_Usuario_Fijo,
               FK_ID_Usuario_Temporal, FK_ID_Administrador)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, v.getMarca());
            ps.setString(2, v.getModelo());
            ps.setInt(3, v.getAnio());
            ps.setString(4, v.getColor());
            ps.setString(5, v.getFkIdPlaca()); // puede ser null
            if (v.getFkIdUsuarioFijo() != null) {
                ps.setInt(6, v.getFkIdUsuarioFijo());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            if (v.getFkIdUsuarioTemporal() != null) {
                ps.setInt(7, v.getFkIdUsuarioTemporal());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setInt(8, v.getFkIdAdministrador());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Devuelve todos los vehículos de un usuario fijo.
     */
    public List<VehiculoPOJO> buscarPorUsuarioFijo(int idUsuarioFijo) {
        String sql = "SELECT * FROM vehiculo WHERE FK_ID_Usuario_Fijo = ?";
        return buscarVehiculosPorCampo(sql, idUsuarioFijo);
    }

    /**
     * Retorna el primer VehiculoPOJO (o null) para un usuario fijo.
     */
    /*
    public VehiculoPOJO buscarPrimeroPorUsuarioFijo(int idUsuarioFijo) {
        List<VehiculoPOJO> lista = buscarPorUsuarioFijo(idUsuarioFijo);
        return lista.isEmpty() ? null : lista.get(0);
    }*/

    // Esta función permite tener varios vehiculos pero solo devuelve el primer vehiculo
    public VehiculoPOJO buscarPrimeroPorUsuarioFijo(int idUsuarioFijo) {
        List<VehiculoPOJO> lista = buscarPorUsuarioFijo(idUsuarioFijo);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /**
     * Devuelve todos los vehículos de un usuario temporal.
     */
    public List<VehiculoPOJO> buscarPorUsuarioTemporal(int idUsuarioTemporal) {
        String sql = "SELECT * FROM vehiculo WHERE FK_ID_Usuario_Temporal = ?";
        return buscarVehiculosPorCampo(sql, idUsuarioTemporal);
    }

    /**
     * Busca un vehículo por su ID (clave primaria).
     */
    public VehiculoPOJO buscarPorId(int idVehiculo) {
        String sql = "SELECT * FROM vehiculo WHERE ID_Vehiculo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVehiculo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extraerVehiculo(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Actualiza un vehículo existente (no cambia el ID).
     */
    public boolean actualizarVehiculo(VehiculoPOJO v) {
        String sql = """
            UPDATE vehiculo SET
              Marca = ?, Modelo = ?, Año = ?, Color = ?,
              FK_ID_Placa = ?, FK_ID_Administrador = ?
            WHERE ID_Vehiculo = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, v.getMarca());
            ps.setString(2, v.getModelo());
            ps.setInt(3, v.getAnio());
            ps.setString(4, v.getColor());
            ps.setString(5, v.getFkIdPlaca());
            ps.setInt(6, v.getFkIdAdministrador());
            ps.setInt(7, v.getIdVehiculo());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Asigna una placa a un vehículo.
     */
    public boolean asignarPlacaAVehiculo(int idVehiculo, String idPlaca) {
        String sql = "UPDATE vehiculo SET FK_ID_Placa = ? WHERE ID_Vehiculo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idPlaca);
            ps.setInt(2, idVehiculo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════

    /** Método auxiliar para buscar múltiples vehículos. */
    private List<VehiculoPOJO> buscarVehiculosPorCampo(String sql, int valorCampo) {
        List<VehiculoPOJO> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, valorCampo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(extraerVehiculo(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** Método auxiliar para extraer un POJO desde ResultSet. */
    private VehiculoPOJO extraerVehiculo(ResultSet rs) throws SQLException {
        return new VehiculoPOJO(
                rs.getInt("ID_Vehiculo"),
                rs.getString("Marca"),
                rs.getString("Modelo"),
                rs.getInt("Año"),
                rs.getString("Color"),
                rs.getString("FK_ID_Placa"),
                (Integer) rs.getObject("FK_ID_Usuario_Fijo"),      // puede ser null
                (Integer) rs.getObject("FK_ID_Usuario_Temporal"),  // puede ser null
                rs.getInt("FK_ID_Administrador")
        );
    }
}
