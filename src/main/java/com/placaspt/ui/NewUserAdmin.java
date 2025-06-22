package com.placaspt.ui;

import com.placaspt.database.AdministradorDAO;
import com.placaspt.model.AdministradorPOJO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import com.placaspt.MainPlacasPT;

import java.io.IOException;

public class NewUserAdmin {
    @FXML private TextField adminName, adminSurname, adminEmail, adminPhone;
    @FXML private TextField claveMaestraField, adminPassword, confirmAdminPassword;
    @FXML private Label   labelTitleAdmin;
    @FXML private AnchorPane rootPaneNewUserAdmin;

    @FXML
    private void crearUsuario() {
        String nom  = adminName.getText().trim();
        String ape  = adminSurname.getText().trim();
        String cor  = adminEmail.getText().trim();
        String tel  = adminPhone.getText().trim();
        String pwd  = adminPassword.getText();
        String conf = confirmAdminPassword.getText();
        String key  = claveMaestraField.getText();

        if (!pwd.equals(conf)) {
            System.out.println("Las contraseñas no coinciden.");
            return;
        }
        if (!"admin123".equals(key)) {
            System.out.println("Clave maestra incorrecta.");
            return;
        }

        AdministradorPOJO admin = new AdministradorPOJO(nom, ape, cor, tel, pwd);
        boolean ok = AdministradorDAO.insertarAdministrador(admin);
        if (ok) {
            System.out.println("Administrador registrado.");
            volverAlLogin();
        } else {
            System.out.println("Error al registrar administrador.");
        }
    }

    @FXML
    private void volverAlLogin() {
        try {
            Parent loginRoot = FXMLLoader.load(
                    getClass().getResource("/com/placaspt/ui/login.fxml")
            );
            Stage stage = MainPlacasPT.getPrimaryStage();
            stage.getScene().setRoot(loginRoot);
            stage.setTitle("Placas PT – Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
