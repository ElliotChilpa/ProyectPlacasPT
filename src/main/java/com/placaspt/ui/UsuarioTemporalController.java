package com.placaspt.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class UsuarioTemporalController {

    @FXML private TextField idField;
    @FXML private TextField nombreField;
    @FXML private TextField telefonoField;
    @FXML private TextField correoField;
    @FXML private TextField placasField;
    @FXML private TextField documentoField;

    private UsuariosController parentController;

    public void setParentController(UsuariosController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleGuardar() {
        System.out.println("Usuario temporal guardado: " + nombreField.getText());
    }

    @FXML
    private void handleCancelar() {
        if (parentController != null) {
            parentController.mostrarSeleccionTipoUsuario();
        } else {
            System.out.println("No se pudo volver: controlador principal no asignado.");
        }
    }
}
