package com.placaspt.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class ConfiguracionesController {
    @FXML private Label estadoRFID;

    @FXML
    void abrirRFID() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/rfid.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Lector RFID - COM4");
            stage.setScene(new Scene(root));
            stage.show();

            // Cerrar esta ventana (main.fxml)
            Stage thisStage = (Stage) estadoRFID.getScene().getWindow();
            thisStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
