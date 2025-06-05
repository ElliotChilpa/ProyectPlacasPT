package com.placaspt.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class UsuarioFijoEditarController {

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
        // Aquí iría la búsqueda en base de datos.
        // Por ahora, ejemplo ficticio:
        if (id.equals("123")) {
            nombreField.setText("Juan Pérez");
            telefonoField.setText("555-123456");
            placasField.setText("ABC-123");
            modeloField.setText("Toyota Corolla");
            rfidField.setText("RFID123XYZ");
        } else {
            System.out.println("Usuario no encontrado");
        }
    }

    @FXML
    private void handleGuardar() {
        // Lógica para guardar los cambios
        System.out.println("Usuario actualizado: " + nombreField.getText());
    }

    @FXML
    private void handleCancelar() {
        if (parentController != null) {
            parentController.mostrarMenuUsuarioFijo();
        }
    }
}
