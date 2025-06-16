package com.placaspt.ui;

import com.placaspt.database.UsuarioDAO;
import com.placaspt.logic.RaspSSH;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button; // Agregamos esta clase para prueba de menú.
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane; // Agregamos esta clase para prueba de menú lateral.
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    // Estó es para el Menú
    @FXML private StackPane contentArea;
    @FXML private Button btnUsuarios;
    @FXML private Button btnEstacionamiento;
    @FXML private Button btnConfiguraciones;

    @FXML private Label printmessage;
    @FXML private TextFlow textflow;

    /*@FXML
    public void initialize() {
        // Carga por defecto la sección Usuarios
        showUsuarios();
    }*/

    // Control para abrir vista de usuarios
    @FXML
    private void showUsuarios(ActionEvent event) {
        loadView("/com/placaspt/ui/UsuariosView.fxml");
    }

    // Control para abrir vista de Estacionamiento
    @FXML
    private void showEstacionamiento(ActionEvent event) {
        loadView("/com/placaspt/ui/estacionamientoView.fxml");
    }

    // Control para abrir vista de Configuraciones.
    @FXML
    private void showConfiguraciones(ActionEvent event) {
        loadView("/com/placaspt/ui/configuracionesView.fxml");
    }

    // Control para cerrar sesion
    @FXML
    private void logout(ActionEvent event) {
        try {
            Parent loginRoot = FXMLLoader.load(
                    getClass().getResource("/com/placaspt/ui/login.fxml")
            );
            Stage stage = (Stage)((javafx.scene.Node)event.getSource())
                    .getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            //stage.setTitle("Login");
        } catch (Exception e) {
            e.printStackTrace();
            // aquí podrías poner un Alert de error
        }
    }

    @FXML void onReadFile() {
        //Simulación de lectura de archivo en raspberry
        String host = "192.168.100.2"; // 🧠 IP de tu Raspberry
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
    @FXML
    void leerUsuarioDB() {
        printmessage.setText("Hola ejemplo 1");
        UsuarioDAO.listarUsuarios();
    }
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
        } */

    }

}
