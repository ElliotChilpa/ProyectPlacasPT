package com.placaspt.ui;

import com.placaspt.database.UsuariosDAO;
import com.placaspt.logic.RaspSSH;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button; // Agregamos esta clase para prueba de menú.
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane; // Agregamos esta clase para prueba de menú lateral.
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class MainController {
    // Esto es una ruta para identificar el pane como contenedor central.

    // Estó es para el Menú
    // Esto tambiés es para un historial de pestañas
    @FXML private StackPane contentArea;
    @FXML private Button btnUsuarios;
    @FXML private Button btnEstacionamiento;
    @FXML private Button btnConfiguraciones;

    // Boton para cerra sesión
    //@FXML private Button btnLogout;

    @FXML private Label printmessage;
    @FXML private TextFlow textflow;
    // Pila donde apilamos las vistas anteriores
    private final Deque<Node> history = new ArrayDeque<>();

    /*@FXML
    public void initialize() {
        // Carga por defecto la sección Usuarios
        showUsuarios();
    }*/
    /*
     Para quitar la clase "active en el css" para ver los botones seleccionados
     */
    // Helper: quita la clase 'active' de todos
    private void clearActive() {
        btnUsuarios.getStyleClass().remove("active");
        btnEstacionamiento.getStyleClass().remove("active");
        btnConfiguraciones.getStyleClass().remove("active");
        //btnLogout.getStyleClass().remove("active");
    }
    // Helper: marca sólo este botón como activo
    private void setActive(Button b) {
        clearActive();
        b.getStyleClass().add("active");
    }

    // Control para abrir vista de usuarios
    @FXML
    private void showUsuarios(ActionEvent event) {
        setActive(btnUsuarios);
        loadView("/com/placaspt/ui/UsuariosView.fxml");
    }

    // Control para abrir vista de Estacionamiento
    @FXML
    private void showEstacionamiento(ActionEvent event) {
        setActive(btnEstacionamiento);
        loadView("/com/placaspt/ui/estacionamientoView.fxml");
    }

    // Control para abrir vista de Configuraciones.
    @FXML
    private void showConfiguraciones(ActionEvent event) {
        setActive(btnConfiguraciones);
        loadView("/com/placaspt/ui/configuracionesView.fxml");
    }

    // Control para cerrar sesion
    @FXML
    private void logout(ActionEvent event) {
        try {
            Parent loginRoot = FXMLLoader.load(
                    getClass().getResource("/com/placaspt/ui/login.fxml")
            );
            // Obtén el Stage existente
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                    .getScene().getWindow();
            // Para reutiliza la misma Scene y cambia solo el root
            Scene scene = stage.getScene();
            scene.setRoot(loginRoot);
            // (Opcional) asegura que siga maximizada
            stage.setMaximized(true);
            stage.setTitle("Placas PT – Login");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML void onReadFile() {
        //Simulación de lectura de archivo en raspberry
        //String host = "192.168.100.2"; // IP de tu Raspberry
        String host = "192.168.100.254"; // IP de tu Raspberry
        String user = "placasPT";            // Usuario SSH
        String pass = "8586";     // Contraseña SSH
        String archivo = "/home/placasPT/credenciales.txt"; // Ruta real del archivo

        String resultado = RaspSSH.leerArchivoRasp(host, user, pass, archivo);

        //Alert alert = new Alert(Alert.AlertType.INFORMATION);
        //alert.setTitle("Resultado desde Raspberry Pi");
        //alert.setHeaderText("Contenido del archivo:");
        //alert.setContentText(resultado);
        //alert.showAndWait();


        // CON TEXT FLOW SOLO
        //textflow.getChildren().clear();
        //textflow.getChildren().add(new Label(resultado));

        // CON FORMATO TEXTFLOW pero CONSOLA
        //Label contenido = new Label(resultado);
        //contenido.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13;");
        //textflow.getChildren().add(contenido);

        //textflow.getChildren().clear();
        //textflow.getChildren().add(contenido);

        textflow.getChildren().clear();

        String[] lineas = resultado.split("\n");
        for (String linea : lineas) {
            Text texto = new Text(linea + "\n");
            texto.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13;");
            textflow.getChildren().add(texto);
        }
    }

    /**
     * Esta seccion del codigo era un ejemplo de como utilizar un DAO para leer una lista de usuarios.
     */
    /**
    @FXML
    void leerUsuarioDB() {
        printmessage.setText("Hola ejemplo 1");
        UsuariosDAO.listarUsuarios();
    }*/
    @FXML
    void abrirRFID() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/rfid.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Lector RFID - COM4");
            stage.setScene(new Scene(root));
            stage.show();

            // Cerrar esta ventana (main.fxml)
            Stage thisStage = (Stage) printmessage.getScene().getWindow();
            thisStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // ////////////////////////// ESTAS DOS FUNCIONES SON PARA BARRA LATERAL.
    // Cambiamos a publico para los siguientes menús
    /*
    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            // inyecta referencia a este MainController si el sub-controlador la necesita
            Object subController = loader.getController();
            if (subController instanceof MainAware) {
                ((MainAware)subController).setMainController(this);
            }
            contentArea.getChildren().setAll(view);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        /** try {
            // Carga la vista FXML
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            // Reemplaza el contenido actual
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            // Puedes mostrar un diálogo de error si falla la carga
        } //**

    }*/

    /**
     * Sustituye el centro de la aplicación por el nodo que le pases.
     */
    public void setContent(Node content) {
        //root.setCenter(content);
        contentArea.getChildren().setAll(content);
    }

    /**
     * Carga una nueva vista, guardando la actual en el historial
     */
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
                ((MainAware)ctrl).setMainController(this);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Vuelve a la vista anterior sacándola de la pila
     */
    public void goBack() {
        if (!history.isEmpty()) {
            Node previous = history.pop();
            contentArea.getChildren().setAll(previous);
        }
    }

    // ////////////////////////// ESTA FUNCIÓN ES PARA UN HISTORIAL DE PESTAÑAS.

}
