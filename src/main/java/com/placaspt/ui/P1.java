package com.placaspt.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.io.IOException;

public class P1 {

    @FXML
    private void handleUsuarios(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/usuarios.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Gestión de Usuarios");
            newStage.setScene(new Scene(root));
            newStage.centerOnScreen();
            //newStage.setMaximized(true);
            newStage.show();

            // Cierra la ventana de dashboard
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showInfo("Error al cargar la ventana de Usuarios");
        }
    }

    @FXML
    private void handleEstacionamiento(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/estacionamiento.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Gestión del Estacionamiento");
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.show();

            // Cierra la ventana de dashboard
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showInfo("Error al cargar la ventana de Estacionamiento");
        }
    }

    @FXML
    private void handleConfiguraciones(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(P1.class.getResource("/com/placaspt/ui/configuracion.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Configuraciones");
            newStage.setScene(new Scene(root));
            //newStage.setMaximized(true);
            newStage.show();

            // Cierra la ventana de dashboard
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showInfo("Error al cargar la ventana de Configuraciones");
        }
    }

    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        showInfo("Cerrando sesión...");
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
