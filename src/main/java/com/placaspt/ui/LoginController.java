package com.placaspt.ui;

import com.placaspt.model.AdministradorPOJO;
import com.placaspt.logic.AuthManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.placaspt.MainPlacasPT;

import java.io.IOException;

public class LoginController {
    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private Label   messageLabel;

    @FXML
    private void onLoginClick() {
        String username = userField.getText().trim();
        String password = passField.getText();

        AdministradorPOJO admin = AuthManager.login(username, password);
        if (admin != null) {
            try {
                Parent mainRoot = FXMLLoader.load(
                        getClass().getResource("/com/placaspt/ui/main.fxml")
                );
                // Actualiza el root de la escena existente
                Stage stage = MainPlacasPT.getPrimaryStage();
                stage.getScene().setRoot(mainRoot);
                stage.setTitle("Placas PT – Bienvenido " + admin.getNombre());
                // no necesitas stage.show() de nuevo
            } catch (IOException e) {
                e.printStackTrace();
                messageLabel.setText("Error cargando la pantalla principal.");
            }
        } else {
            messageLabel.setText("Usuario o contraseña incorrecta");
        }
    }

    @FXML
    private void crearAdmin() {
        try {
            Parent newAdminRoot = FXMLLoader.load(
                    getClass().getResource("/com/placaspt/ui/newUserAdmin.fxml")
            );
            Stage stage = MainPlacasPT.getPrimaryStage();
            stage.getScene().setRoot(newAdminRoot);
            stage.setTitle("Placas PT – Crear Admin");
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error abriendo formulario de registro.");
        }
    }
}
