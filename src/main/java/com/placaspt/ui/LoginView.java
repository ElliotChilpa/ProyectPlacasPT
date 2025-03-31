package com.placaspt.ui;

import com.placaspt.logic.AuthManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoginView {
    public void show(Stage stage) {
        // Agregaremos Elementos.
        Label userLabel = new Label("Usuario");
        TextField userField = new TextField();

        Label passLabel = new Label("Contraseña");
        PasswordField passField = new PasswordField();

        Button loginButton = new Button("Iniciar Sesion");
        Label messageLabel = new Label();

        //Acciones del Boton
        loginButton.setOnAction(e -> {
           String username = userField.getText();
           String password = passField.getText();
           boolean valid = AuthManager.validate(username, password);
           if (valid) {
               messageLabel.setText("Acceso Correcto");
           }else {
               messageLabel.setText("Usuario o Contraseña Incorrecto");
           }
        });

        // Layout
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);
        grid.add(loginButton, 1, 2);
        grid.add(messageLabel, 1, 3);

        // Mostrar
        Scene scene = new Scene(grid, 350, 200);
        stage.setScene(scene);
        stage.setTitle("Login - PlacasPT");
        stage.show();
    }
}
