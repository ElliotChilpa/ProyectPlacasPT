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

import com.placaspt.logic.UsuariosPOJO;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UsuariosDAO {

    /**
     * Inserta un nuevo usuario en la tabla Usuario.
     * @return el ID generado (AUTO_INCREMENT) o -1 en error.
     */
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

            int affected = ps.executeUpdate();
            if (affected == 0) return -1;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Inserta un registro en UsuarioFijo para el usuario dado.
     */
    public boolean insertarUsuarioFijo(int idUsuario) {
        String sql = "INSERT INTO UsuarioFijo (FK_Usuario) VALUES (?)";
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
     * Inserta un registro en UsuarioTemporal con media-blobs y fechas.
     * @param idUsuario       FK al Usuario creado
     * @param actividad       texto de la actividad a realizar
     * @param fechaInicio     fecha de inicio de visita
     * @param fechaFin        fecha de fin de visita
     * @param documentoBLOB   la foto del DNI como Blob
     * @return true si insertó, false si falló
     */
    public boolean insertarUsuarioTemporal(int idUsuario,
                                           String actividad,
                                           LocalDate fechaInicio,
                                           LocalDate fechaFin,
                                           Blob documentoBLOB) {
        String sql = """
            INSERT INTO UsuarioTemporal
              (Actividad_Realizar,
               Documento_DNI,
               Fecha_Inicio,
               Fecha_Fin,
               FK_Usuario)
            VALUES (?, ?, ?, ?, ?)""";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, actividad);
            ps.setBlob(2, documentoBLOB);
            ps.setDate(3, Date.valueOf(fechaInicio));
            ps.setDate(4, Date.valueOf(fechaFin));
            ps.setInt   (5, idUsuario);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lee todos los usuarios.
     */
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

    // Podrías agregar métodos como buscarPorId(int id), actualizarUsuario(...), eliminarUsuario(int id)
}
