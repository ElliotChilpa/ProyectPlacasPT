/*package com.placaspt.ui;

import com.placaspt.logic.AppService;
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

    /*
    @FXML
    private void initialize() {
        if (lector.iniciar("COM4")) {
            System.out.println("Puerto COM4 abierto");
        } else {
            System.out.println("Error al abrir COM4");
        }
    }* /
    @FXML
    private void initialize() {
        // 1) Arranca AppService
        AppService app = AppService.getInstance();

        if (!app.startRfid("COM4")) {
            System.err.println("[AppService] No se pudo abrir COM4");
        }
        app.startPlacaPolling(
                "192.168.100.254", "placasPT",
                "8586", "/home/placasPT/.../output.json",
                5
        );

        // 2) Carga la vista inicial
        showEstacionamiento(null);
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
*/
package com.placaspt.ui;

import com.placaspt.logic.AppEventListener;
import com.placaspt.logic.AppService;
import com.placaspt.logic.RaspberryPollingService;
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
    @FXML private Label printmessage;

    private final Deque<Node> history = new ArrayDeque<>();

    // guardamos la última instancia de controlador cargada
    private Object currentController;

    private final AppService app = AppService.getInstance();

    @FXML
    private void initialize() {
        // 1) Arrancamos el servicio de fondo una sola vez.
        if (!app.startRfid("COM4")) {
            System.err.println("[AppService] No se pudo abrir COM4");
        }
        app.startPlacaPolling(
                "192.168.100.254",
                "placasPT",
                "8586",
                "/home/placasPT/placasPTpi/pruebas-YOLO/output.json",
                5,
                java.util.concurrent.TimeUnit.SECONDS
        );

        // 2) Mostramos directamente la vista de estacionamiento al iniciar
        showEstacionamiento(null);
    }

    // Botones de menú

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

    // Métodos para manejar la carga de vistas
    public void setContent(Node content) {
        contentArea.getChildren().setAll(content);
    }

    @FXML
    private void logout(ActionEvent event) {
        // Detenemos los servicios al cerrar sesión
        app.stopRfid();
        app.stopPlacaPolling();

        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/placaspt/ui/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(loginRoot);
            stage.setMaximized(true);
            stage.setTitle("Placas PT – Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Carga dinámica de vistas
    public void loadView(String fxmlPath) {
        try {
            // 1) Antes de cargar la nueva vista:
            //   si la anterior era AppEventListener, damos de baja
            if (currentController instanceof AppEventListener) {
                AppService.getInstance().unregisterListener((AppEventListener) currentController);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            // 2) Inyección de MainController
            Object ctrl = loader.getController();
            if (ctrl instanceof MainAware) {
                ((MainAware) ctrl).setMainController(this);
            }

            // 3) Si la nueva vista es AppEventListener, la damos de alta
            if (ctrl instanceof AppEventListener) {
                AppService.getInstance().registerListener((AppEventListener) ctrl);
            }

            // 4) Sustituimos contenido y guardamos controlador
            contentArea.getChildren().setAll(view);
            currentController = ctrl;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
    public void loadView(String fxmlPath) {
        try {
            if (!contentArea.getChildren().isEmpty()) {
                history.push(contentArea.getChildren().get(0));
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof MainAware) {
                ((MainAware) ctrl).setMainController(this);
            }
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public void goBack() {
        if (!history.isEmpty()) {
            Node previous = history.pop();
            contentArea.getChildren().setAll(previous);
        }
    }

    // Helpers de estilo

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
