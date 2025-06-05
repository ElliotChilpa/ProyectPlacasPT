package com.placaspt.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class UsuarioFijoController {

    @FXML
    private TextField idUsuarioField;

    @FXML
    private TextField nombreField;

    @FXML
    private TextField telefonoField;

    @FXML
    private TextField placasField;

    @FXML
    private TextField modeloField;

    @FXML
    private TextField rfidField;

    // Referencia al controlador principal
    private UsuariosController parentController;

    public void setParentController(UsuariosController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleAgregar() {
        System.out.println("Usuario agregado: " + nombreField.getText());
        // Aquí puedes limpiar o mostrar confirmación si quieres
    }

    @FXML
    private void handleCancelar() {
        if (parentController != null) {
            parentController.mostrarMenuUsuarioFijo(); // ← actúa como volver
        } else {
            System.out.println("No se pudo volver: controlador principal no asignado.");
        }
    }
}
