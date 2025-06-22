package com.placaspt.ui;

import com.placaspt.database.RegistroRFIDDAO;
import com.placaspt.database.TarjetaRFIDDAO;
import com.placaspt.logic.RS232RFID;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controlador para la vista de RFID.
 * Abre el puerto COM, muestra lecturas y registra en BD.
 */
public class RFIDController implements MainAware {

    @FXML private Label etiquetaLectura;
    @FXML private Button btnIniciar;
    @FXML private Button btnBack;    // botón “Atrás” inyectado desde el FXML+

    // Logica para conectarse Serial.
    private final RS232RFID lector = new RS232RFID();

    // Referencia al controlador principal para goBack() ─────────
    private MainController mainController;

    /**
     * Recibe la referencia al MainController para poder
     * llamar a loadView() y cambiar la vista central.
     */
    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }


    @FXML
    private void initialize() {
    }

    // Método para conexión SERIAL RFID ────────────────────────────────────

    /** Abre COM4 y comienza a escuchar datos RFID */
    @FXML
    private void iniciarLectura() {
        boolean conectado;
        try {
            conectado = lector.iniciar("COM4");
        } catch (Exception ex) {
            etiquetaLectura.setText("Error al abrir COM4");
            ex.printStackTrace();
            return;
        }

        if (!conectado) {
            etiquetaLectura.setText("No se pudo abrir COM4");
            return;
        }

        etiquetaLectura.setText("Puerto COM4 abierto. Escuchando...");
        lector.escuchar(datos -> Platform.runLater(() -> manejarLectura(datos)));
    }

    /** Procesa cada dato recibido del lector */
    private void manejarLectura(String datos) {
        etiquetaLectura.setText("Lectura: " + datos);
        System.out.println("Lectura RFID: " + datos);
        // En tu controller, dentro del método donde lo usas:

        // Esta parte ed de ejemplo porque tenía que instanciar TarjetaRFIDDAO()
        TarjetaRFIDDAO dao = new TarjetaRFIDDAO();
        try {
            if (dao.existeTag(datos)) {
            //if (TarjetaRFIDDAO.existeTag(datos)) {
                // Usuario fijo con ID 1 (ajustar según tu lógica)
                RegistroRFIDDAO.insertarRegistro(datos, 1, "EXITO", "Tarjeta reconocida");
                System.out.println("RFID: " + datos + " Reconocido");
            } else {
                System.out.println("Tag desconocido: " + datos);
            }
        } catch (Exception ex) {
            System.err.println("Error al registrar RFID en BD: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void onBack() {
        mainController.goBack();
    }

}

