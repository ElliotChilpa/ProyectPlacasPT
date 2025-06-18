package com.placaspt.ui;

import com.placaspt.database.AdministradorDAO;
import com.placaspt.logic.AdministradorPOJO;
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

    @FXML private TextField claveMaestraField;
    @FXML private TextField adminPassword;
    @FXML private TextField confirmAdminPassword;
    @FXML private Label labelTitleAdmin;
    @FXML private AnchorPane rootPaneNewUserAdmin;


    @FXML
    private void crearUsuario() {
        String nombre = adminName.getText();
        String apellido = adminSurname.getText();
        String correo = adminEmail.getText();
        String telefono = adminPhone.getText();
        String clave = adminPassword.getText();
        String confirmar = confirmAdminPassword.getText();
        String claveMaestra = claveMaestraField.getText();

        // Verificación básica
        if (!clave.equals(confirmar)) {
            System.out.println("Las contraseñas no coinciden.");
            return;
        }

        if (!claveMaestra.equals("admin123")) { // ejemplo de clave maestra
            System.out.println("Clave maestra incorrecta.");
            return;
        }

        AdministradorPOJO admin = new AdministradorPOJO(nombre, apellido, correo, telefono, clave);
        boolean exito = AdministradorDAO.insertarAdministrador(admin);

        if (exito) {
            System.out.println("Administrador registrado correctamente.");
            volverAlLogin();
        } else {
            System.out.println("Error al registrar administrador.");
        }
    }

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
