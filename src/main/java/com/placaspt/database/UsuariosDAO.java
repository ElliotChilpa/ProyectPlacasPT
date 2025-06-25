/**package com.placaspt.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UsuariosDAO {

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
*/
package com.placaspt.database;

import com.placaspt.model.UsuarioTemporalPOJO;
import com.placaspt.model.UsuariosPOJO;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UsuariosDAO {

    /** Inserta un nuevo Usuario y devuelve su ID o -1 en error. */
    public int insertarUsuario(UsuariosPOJO u) {
        String sql = """
            INSERT INTO Usuario 
              (Nombres, Apellidos, Correo, Telefono, Fecha_Registro, FK_ID_Administrador)
            VALUES (?, ?, ?, ?, ?, ?)""";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getNombres());
            ps.setString(2, u.getApellidos());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getTelefono());
            ps.setDate(5, Date.valueOf(u.getFechaRegistro()));
            ps.setInt(6, u.getFkAdministrador());

            if (ps.executeUpdate() == 0) return -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Inserta un UsuarioFijo solo si no existía ya (se asume índice único en FK_Usuario).
     * Devuelve true solo si realmente se insertó.
     */
    public boolean insertarUsuarioFijo(int idUsuario) {
        String sql = """
            INSERT IGNORE INTO UsuarioFijo (FK_Usuario)
            VALUES (?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inserta un UsuarioTemporal.
     */
    public boolean insertarUsuarioTemporal(int idUsuario,
                                           String actividad,
                                           LocalDate fechaInicio,
                                           LocalDate fechaFin,
                                           Blob documentoBLOB) {
        String sql = """
            INSERT INTO UsuarioTemporal
              (Actividad_Realizar, Documento_DNI,
               Fecha_Inicio, Fecha_Fin, FK_Usuario)
            VALUES (?, ?, ?, ?, ?)""";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, actividad);
            ps.setBlob(2, documentoBLOB);
            ps.setDate(3, Date.valueOf(fechaInicio));
            ps.setDate(4, Date.valueOf(fechaFin));
            ps.setInt(5, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Lista todos los Usuarios. */
    public List<UsuariosPOJO> listarUsuarios() {
        List<UsuariosPOJO> lista = new ArrayList<>();
        String sql = "SELECT * FROM Usuario";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new UsuariosPOJO(
                        rs.getInt("ID_Usuario"),
                        rs.getString("Nombres"),
                        rs.getString("Apellidos"),
                        rs.getString("Correo"),
                        rs.getString("Telefono"),
                        rs.getDate("Fecha_Registro").toLocalDate(),
                        rs.getInt("FK_ID_Administrador")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** Devuelve true si existe un registro en UsuarioFijo para este Usuario. */
    public boolean existeUsuarioFijo(int idUsuario) {
        String sql = "SELECT 1 FROM UsuarioFijo WHERE FK_Usuario = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Devuelve el PK de UsuarioFijo para un Usuario dado,
     * o -1 si no existe.
     */
    public int obtenerIdUsuarioFijo(int idUsuario) {
        String sql = "SELECT ID_Usuario_Fijo FROM UsuarioFijo WHERE FK_Usuario = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID_Usuario_Fijo");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int obtenerIdUsuarioTemporal(int idUsuario) {
        String sql = "SELECT ID_Usuario_Temporal FROM UsuarioTemporal WHERE FK_Usuario = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("ID_Usuario_Temporal");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    // en com.placaspt.database.UsuariosDAO
    public UsuariosPOJO buscarPorId(int idUsuario) {
        String sql = "SELECT * FROM Usuario WHERE ID_Usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UsuariosPOJO(
                            rs.getInt("ID_Usuario"),
                            rs.getString("Nombres"),
                            rs.getString("Apellidos"),
                            rs.getString("Correo"),
                            rs.getString("Telefono"),
                            rs.getDate("Fecha_Registro").toLocalDate(),
                            rs.getInt("FK_ID_Administrador")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean actualizarUsuario(UsuariosPOJO u) {
        String sql = """
        UPDATE Usuario SET
          Nombres = ?, Apellidos = ?, Correo = ?, Telefono = ?
        WHERE ID_Usuario = ?
    """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNombres());
            ps.setString(2, u.getApellidos());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getTelefono());
            ps.setInt(5, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Devuelve el subregistro de UsuarioTemporal para un usuario dado,
     * o null si no existe.
     */

    public UsuarioTemporalPOJO buscarTemporalPorUsuario(int idUsuario) {
        String sql = """
        SELECT * FROM UsuarioTemporal
         WHERE FK_Usuario = ? LIMIT 1
    """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UsuarioTemporalPOJO(
                            rs.getInt("ID_Usuario_Temporal"),
                            rs.getString("Actividad_Realizar"),
                            rs.getBlob("Documento_DNI"),
                            rs.getDate("Fecha_Inicio").toLocalDate(),
                            rs.getDate("Fecha_Fin").toLocalDate(),
                            rs.getInt("FK_Usuario")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Actualiza los campos de UsuarioTemporal (actividad, fechas, blob).
     */
    public boolean actualizarUsuarioTemporal(int idUsuarioTemporal,
                                             String actividad,
                                             LocalDate inicio,
                                             LocalDate fin,
                                             Blob documentoBLOB) {
        String sql = """
        UPDATE UsuarioTemporal SET
          Actividad_Realizar = ?,
          Documento_DNI      = ?,
          Fecha_Inicio       = ?,
          Fecha_Fin          = ?
        WHERE ID_Usuario_Temporal = ?
    """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, actividad);
            ps.setBlob(2, documentoBLOB);
            ps.setDate(3, Date.valueOf(inicio));
            ps.setDate(4, Date.valueOf(fin));
            ps.setInt(5, idUsuarioTemporal);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Dado un ID_Usuario_Fijo devuelve el ID_Usuario (FK_Usuario),
     * o -1 si no existe.
     */
    public int obtenerUsuarioBasePorFijo(int idUsuarioFijo) {
        String sql = """
          SELECT FK_Usuario
            FROM UsuarioFijo
           WHERE ID_Usuario_Fijo = ?
           LIMIT 1
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuarioFijo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("FK_Usuario");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Opcional: buscarPorId, actualizarUsuario, eliminarUsuario...
}
