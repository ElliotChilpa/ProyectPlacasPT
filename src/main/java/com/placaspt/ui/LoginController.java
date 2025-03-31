package com.placaspt.ui;

import com.placaspt.logic.AuthManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private Label messageLabel;

    @FXML
    private void onLoginClick() {
        Boolean valid = AuthManager.validate(userField.getText(), passField.getText());
        messageLabel.setText(valid ? "Bienvenido" : "Usuario o contraseña incorrectos");
    }
}
