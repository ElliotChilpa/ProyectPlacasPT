package com.placaspt.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class UsuarioFijoEliminarController {

    @FXML private TextField idUsuarioField;
    @FXML private TextField nombreField;
    @FXML private TextField telefonoField;
    @FXML private TextField placasField;
    @FXML private TextField modeloField;
    @FXML private TextField rfidField;

    private UsuariosController parentController;

    public void setParentController(UsuariosController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleBuscar() {
        String id = idUsuarioField.getText();
        // Simular búsqueda
        if (id.equals("456")) {
            nombreField.setText("María López");
            telefonoField.setText("555-987654");
            placasField.setText("XYZ-456");
            modeloField.setText("Nissan Versa");
            rfidField.setText("RFID456ABC");
        } else {
            System.out.println("Usuario no encontrado");
        }
    }

    @FXML
    private void handleEliminar() {
        // Simula eliminación
        System.out.println("Usuario eliminado: " + nombreField.getText());
        clearFields();
    }

    @FXML
    private void handleCancelar() {
        if (parentController != null) {
            parentController.mostrarMenuUsuarioFijo();
        }
    }

    private void clearFields() {
        idUsuarioField.clear();
        nombreField.clear();
        telefonoField.clear();
        placasField.clear();
        modeloField.clear();
        rfidField.clear();
    }
}
