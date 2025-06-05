package com.placaspt.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import java.io.IOException;

public class UsuariosController {

    @FXML
    private StackPane contentPane;

    @FXML
    private void initialize() {
        mostrarSeleccionTipoUsuario(); // Vista inicial
    }

    // Vista de selección entre Usuario Fijo y Usuario Temporal
    public void mostrarSeleccionTipoUsuario() {
        VBox box = new VBox(20);
        box.setStyle("-fx-alignment: center; -fx-background-color: #f6f6f6;");

        Button btnFijo = new Button("Usuario Fijo");
        Button btnTemporal = new Button("Usuario Temporal");

        btnFijo.setOnAction(e -> mostrarMenuUsuarioFijo());
        btnTemporal.setOnAction(e -> mostrarFormularioUsuarioTemporal());

        btnFijo.setStyle("-fx-padding: 10 20; -fx-border-color: #ccc; -fx-background-color: white;");
        btnTemporal.setStyle("-fx-padding: 10 20; -fx-border-color: #ccc; -fx-background-color: white;");

        box.getChildren().addAll(btnFijo, btnTemporal);
        contentPane.getChildren().setAll(box);
    }

    // Menú de opciones para Usuario Fijo
    public void mostrarMenuUsuarioFijo() {
        VBox box = new VBox(20);
        box.setStyle("-fx-alignment: center; -fx-background-color: #f6f6f6; -fx-padding: 30;");

        MenuButton opciones = new MenuButton("Opciones");
        opciones.setStyle("-fx-background-color: white; -fx-border-color: #888; -fx-font-size: 14;");

        MenuItem agregar = new MenuItem("AGREGAR");
        agregar.setOnAction(e -> mostrarFormularioUsuarioFijoAgregar());

        MenuItem editar = new MenuItem("EDITAR");
        editar.setOnAction(e -> mostrarFormularioUsuarioFijoEditar());

        MenuItem eliminar = new MenuItem("ELIMINAR");
        eliminar.setOnAction(e -> mostrarFormularioUsuarioFijoEliminar());

        opciones.getItems().addAll(agregar, editar, eliminar);

        Button btnVolver = new Button("Volver");
        btnVolver.setOnAction(e -> mostrarSeleccionTipoUsuario());
        btnVolver.setStyle("-fx-background-color: #cccccc; -fx-padding: 6 20;");

        box.getChildren().addAll(opciones, btnVolver);
        contentPane.getChildren().setAll(box);
    }

    // Formulario: Agregar Usuario Fijo
    private void mostrarFormularioUsuarioFijoAgregar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/usuario_fijo_agregar.fxml"));
            Parent form = loader.load();

            UsuarioFijoController controller = loader.getController();
            controller.setParentController(this);

            contentPane.getChildren().setAll(form);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Formulario: Editar Usuario Fijo
    private void mostrarFormularioUsuarioFijoEditar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/usuario_fijo_editar.fxml"));
            Parent form = loader.load();

            UsuarioFijoEditarController controller = loader.getController();
            controller.setParentController(this);

            contentPane.getChildren().setAll(form);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Formulario: Usuario Temporal
    private void mostrarFormularioUsuarioTemporal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/usuario_temporal.fxml"));
            Parent form = loader.load();

            UsuarioTemporalController controller = loader.getController();
            controller.setParentController(this);

            contentPane.getChildren().setAll(form);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Formulario: Eliminar Usuario Fijo
    private void mostrarFormularioUsuarioFijoEliminar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/usuario_fijo_eliminar.fxml"));
            Parent form = loader.load();

            UsuarioFijoEliminarController controller = loader.getController();
            controller.setParentController(this);

            contentPane.getChildren().setAll(form);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Acción para volver al dashboard principal
    @FXML
    private void volverAlDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setWidth(900);
            stage.setHeight(600);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
