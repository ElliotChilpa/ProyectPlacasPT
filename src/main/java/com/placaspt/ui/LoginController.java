package com.placaspt.ui;

import com.placaspt.logic.AuthManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Parent;
import java.io.IOException;

public class LoginController {
    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private Label messageLabel;

    @FXML
    private void onLoginClick() {
        String username = userField.getText();
        String password = passField.getText();
        Boolean valid = AuthManager.validate(username, password);

        if (valid) {
            try{
                System.out.println(getClass().getResource("/com/placaspt/ui/main.fxml"));
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/main.fxml"));
                Parent root = loader.load();

                Stage stage= new Stage();
                stage.setTitle("Ventana principal");
                stage.setScene(new Scene(root));
                stage.show();

                // Cerramos ventana antigua.
                Stage thisStage = (Stage) userField.getScene().getWindow();
                thisStage.close();

            } catch (IOException e){
                e.printStackTrace();
            }
        }else {
            messageLabel.setText("Usuario o contraseña Incorrecta");
        }
        messageLabel.setText(valid ? "Bienvenido" : "Usuario o contraseña incorrectos");
    }
}
