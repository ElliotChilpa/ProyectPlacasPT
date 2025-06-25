package com.placaspt.ui;

import com.placaspt.logic.RS232RFID;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button btnUsuarios;
    @FXML private Button btnEstacionamiento;
    @FXML private Button btnConfiguraciones;

    // Establecer conexión con el lector RFID
    //private RS232RFID lector = new RS232RFID();
    //private static final String RFID_PORT = "COM4"; // Puerto para RFID

    private RS232RFID lector = RS232RFID.getInstance();  // Singleton

    // Para manejar el historial de vistas
    private final Deque<Node> history = new ArrayDeque<>();

    @FXML private Label printmessage;

    @FXML
    private void initialize() {
        if (lector.iniciar("COM4")) {
            System.out.println("Puerto COM4 abierto");
        } else {
            System.out.println("Error al abrir COM4");
        }
    }

    // Método para obtener el lector RFID para otros controladores
    public RS232RFID getLector() {
        return lector;
    }

    // Cambiar la vista para diferentes botones
    @FXML
    private void showUsuarios(ActionEvent event) {
        setActive(btnUsuarios);
        loadView("/com/placaspt/ui/UsuariosView.fxml");
    }

    @FXML
    private void showEstacionamiento(ActionEvent event) {
        setActive(btnEstacionamiento);
        loadView("/com/placaspt/ui/estacionamientoView.fxml");
    }

    @FXML
    private void showConfiguraciones(ActionEvent event) {
        setActive(btnConfiguraciones);
        loadView("/com/placaspt/ui/configuracionesView.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        // Solo cerrar el puerto cuando cerramos sesión
        lector.cerrar();
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/placaspt/ui/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(loginRoot);
            stage.setMaximized(true);
            stage.setTitle("Placas PT – Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Métodos para manejar la carga de vistas
    public void setContent(Node content) {
        contentArea.getChildren().setAll(content);
    }


    public void loadView(String fxmlPath) {
        try {
            // Si ya hay algo cargado, lo guardamos antes de reemplazar
            if (!contentArea.getChildren().isEmpty()) {
                history.push(contentArea.getChildren().get(0));
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            // Inyectar MainController si el controlador implementa MainAware
            Object ctrl = loader.getController();
            if (ctrl instanceof MainAware) {
                ((MainAware) ctrl).setMainController(this);  // Inyección de mainController
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Volver a la vista anterior
    public void goBack() {
        if (!history.isEmpty()) {
            Node previous = history.pop();
            lector.cerrar();
            contentArea.getChildren().setAll(previous);
        }
    }

    // Helper para manejar el estado de los botones del menú
    private void clearActive() {
        btnUsuarios.getStyleClass().remove("active");
        btnEstacionamiento.getStyleClass().remove("active");
        btnConfiguraciones.getStyleClass().remove("active");
    }

    private void setActive(Button b) {
        clearActive();
        b.getStyleClass().add("active");
    }
}
