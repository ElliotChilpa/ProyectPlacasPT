package com.placaspt.ui;

import com.placaspt.logic.AuthManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController_2 {

    @FXML
    private TextField userField;

    @FXML
    private PasswordField passField;

    @FXML
    private Label messageLabel;

    @FXML
    public void handleLogin() {
        String username = userField.getText();
        String password = passField.getText();
        boolean valid = AuthManager.validate(username, password);

        if (valid) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/dashboard.fxml"));
                Parent dashboardRoot = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Dashboard");
                stage.setScene(new Scene(dashboardRoot));
                stage.centerOnScreen();
                stage.show();

                // Cierra la ventana de login
                Stage currentStage = (Stage) userField.getScene().getWindow();
                currentStage.close();

            } catch (IOException e) {
                e.printStackTrace();
                messageLabel.setText("Error al cargar el dashboard");
            }
        } else {
            messageLabel.setText("Usuario o Contraseña Incorrecto");
        }
    }
}
