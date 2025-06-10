package com.placaspt.ui;

import com.placaspt.database.RegistroRFIDDAO;
import com.placaspt.database.TarjetaRFIDDAO;
import com.placaspt.logic.RS232RFID;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class RFIDController {

    @FXML private Label etiquetaLectura;
    @FXML private Button btnIniciar;

    private final RS232RFID lector = new RS232RFID();

    @FXML
    public void initialize() {
        btnIniciar.setOnAction(e -> iniciarLectura());
    }

    private void iniciarLectura() {
        boolean conectado = lector.iniciar("COM4");

        if (conectado) {
            etiquetaLectura.setText("Puerto COM4 abierto. Escuchando...");
            lector.escuchar(datos -> Platform.runLater(() -> {
                System.out.println("Lectura RFID: " + datos);
                etiquetaLectura.setText("Lectura: " + datos);

                // Insesción de datos en esta parte del codigo en la base de datos.

                // Función para verificar si existe El tag
                //Integer idRFID = TarjetaRFIDDAO.buscarPorTag(datos);
                //if (idRFID != null) {
                    //RegistroRFIDDAO.insertarRegistro(idRFID, 1, "Exitoso", "Acceso autorizado");
                //} else {
                    //System.out.println("Tag desconocido");
                //}
                int idUsuarioFijo = 1;
                if (TarjetaRFIDDAO.existeTag(datos)) {
                    // Usuario fijo (o idUsuario) lo defines según tu lógica
                    RegistroRFIDDAO.insertarRegistro(
                            datos,
                            idUsuarioFijo,       // por ejemplo 1
                            "EXITO",
                            "Tarjeta reconocida"
                    );
                    System.out.println("RFID: " + datos + "Exito Reconocida");
                } else {
                    System.out.println("Tag desconocido: " + datos);
                }
                // Hasta aquí

            }));
        } else {
            etiquetaLectura.setText("No se pudo abrir COM4");
        }
    }
    @FXML
    void volverAlMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/main.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Panel Principal");
            stage.setScene(new Scene(root));
            stage.show();

            //Cerrar esta ventana (rfid.fxml)
            Stage thisStage = (Stage) etiquetaLectura.getScene().getWindow();
            thisStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

