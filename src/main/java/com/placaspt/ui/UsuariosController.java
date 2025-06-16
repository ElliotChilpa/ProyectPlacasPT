// src/main/java/com/placaspt/ui/UsuariosController.java
package com.placaspt.ui;

import com.placaspt.database.UsuarioDAO;
// import com.placaspt.logic.UsuarioService;
// import com.placaspt.logic.models.Usuario;
import com.placaspt.database.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


public class UsuariosController {

    // @FXML private TableView<Usuario> tablaUsuarios;
    // @FXML private TableColumn<Usuario, Integer> colId;
    // @FXML private TableColumn<Usuario, String> colUsername;
    // @FXML private TableColumn<Usuario, String> colRole;
    @FXML private Label usuarioBD;
    //private final UsuarioService usuarioService = new UsuarioService();

    @FXML
    public void initialize() {
        // Configurar columnas
        // colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        // colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        // colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Cargar datos
        // tablaUsuarios.getItems().setAll(usuarioService.obtenerTodos());
    }

    @FXML
    void leerUsuarioDB() {
        usuarioBD.setText("Este es un ejemplo 1111");
        UsuarioDAO.listarUsuarios();
    }

    // Métodos para manejar botones: agregar, editar, eliminar...
}
