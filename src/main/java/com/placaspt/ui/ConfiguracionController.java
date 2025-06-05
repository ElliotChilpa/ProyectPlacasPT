package com.placaspt.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

public class ConfiguracionController {

    @FXML
    private TextField horarioField;

    @FXML
    private TextField idTemporalField;

    @FXML
    private TextField placasField;

    @FXML
    private Button editarButton;

    @FXML
    private Button buscarButton;

    @FXML
    private Button guardarButton;

    @FXML
    private Button cancelarButton;

    @FXML
    private void editarHorario() {
        horarioField.setDisable(false);
        horarioField.requestFocus();
    }

    @FXML
    private void buscarUsuario() {
        String id = idTemporalField.getText();
        System.out.println("Buscando usuario con ID: " + id);
        horarioField.setText("12:00pm - 3:00pm");
        placasField.setText("NJM-5505");
    }

    @FXML
    private void guardar() {
        String id = idTemporalField.getText();
        String horario = horarioField.getText();
        String placas = placasField.getText();

        System.out.println("Guardado:");
        System.out.println("ID: " + id);
        System.out.println("Horario: " + horario);
        System.out.println("Placas: " + placas);

        horarioField.setDisable(true);
    }

    @FXML
    private void cancelar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) cancelarButton.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
