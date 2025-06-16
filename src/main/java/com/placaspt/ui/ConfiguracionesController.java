package com.placaspt.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class ConfiguracionesController implements MainAware {
    // ESTO ES PARA ABRIR SUB-CONTROLADORES
    private MainController mainController;
    @FXML private Label estadoRFID;

    // ESTO ES PARA ABRIR SUB-Controladores
    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }


    @FXML
    private void abrirRFID() {
        // Ahora puedo recargar el contentArea sin cerrar la ventana principal:
        mainController.loadView("/com/placaspt/ui/rfid.fxml");
    }

    /*
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
    }*/
}
