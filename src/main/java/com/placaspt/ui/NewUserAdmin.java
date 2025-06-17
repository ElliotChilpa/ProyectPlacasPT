package com.placaspt.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class NewUserAdmin {
    @FXML private TextField adminName;
    @FXML private TextField adminSurname;
    @FXML private TextField adminEmail;
    @FXML private TextField adminPhone;

    @FXML private TextField adminPassword;
    @FXML private Label labelTitleAdmin;
    @FXML private AnchorPane rootPaneNewUserAdmin;

    // Regresa a la vista login
    @FXML
    void volverAlLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/login.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Panel Principal");
            stage.setScene(new Scene(root));
            stage.show();

            //Cerrar esta ventana antigua
            Stage thisStage = (Stage) rootPaneNewUserAdmin.getScene().getWindow();
            thisStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
