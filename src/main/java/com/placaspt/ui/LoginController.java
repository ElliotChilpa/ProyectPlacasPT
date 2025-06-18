package com.placaspt.ui;

import com.placaspt.logic.AdministradorPOJO;
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
        AdministradorPOJO adminLogin = AuthManager.login(username, password);
        //Boolean valid = AuthManager.validate(username, password);

        if (adminLogin != null) {
            try{
                // Primera linea para imprimir bienvenida.
                System.out.println("Bienvenido, " + adminLogin.getNombre());
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
        //messageLabel.setText(valid ? "Bienvenido" : "Usuario o contraseña incorrectos");
    }

    @FXML
    private void crearAdmin() {
        try
        {
            System.out.println(getClass().getResource("/com/placaspt/ui/newUserAdmin.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/newUserAdmin.fxml"));
            Parent root = loader.load();

            Stage stage= new Stage();
            stage.setTitle("Crear Usuario Admin");
            stage.setScene(new Scene(root));
            stage.show();

            // Cerramos ventana antigua.
            Stage thisStage = (Stage) userField.getScene().getWindow();
            thisStage.close();
        }catch (IOException e){
            e.printStackTrace();
        }


    }
}
