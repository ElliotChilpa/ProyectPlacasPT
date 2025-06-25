package com.placaspt.ui;

public class RFIDController{

}
/*package com.placaspt.ui;

import com.fazecast.jSerialComm.SerialPort;
import com.placaspt.database.RegistroRFIDDAO;
import com.placaspt.database.TarjetaRFIDDAO;
import com.placaspt.logic.RS232RFID;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class RFIDController implements MainAware {

    @FXML private Label etiquetaLectura;
    @FXML private Button btnIniciar;
    @FXML private Button btnBack;

    private final RS232RFID lector = new RS232RFID();
    private MainController mainController;

    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }

    @FXML
    private void initialize() {
        // nada aquí
    }

    @FXML
    private void iniciarLectura() {
        for (SerialPort p : SerialPort.getCommPorts()) {
            System.out.println("Puerto disponible: " + p.getSystemPortName());
        }

        // Impresion para ver si hay lectura
        System.out.println(">> iniciarLectura() llamado");
        boolean conectado;
        try {
            conectado = lector.iniciar("COM4");
            // Para probar si hay conexión
            System.out.println("RS232RFID.iniciar devolvió: " + conectado);

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

    private void manejarLectura(String datos) {
        etiquetaLectura.setText("Lectura: " + datos);
        System.out.println("Lectura RFID: " + datos);

        TarjetaRFIDDAO tarjetaDAO = new TarjetaRFIDDAO();
        RegistroRFIDDAO rfidDao    = new RegistroRFIDDAO();

        boolean existe = tarjetaDAO.obtenerIdUsuarioFijoPorTag(datos) > 0;
        String estado = existe ? "INGRESO" : "DENEGADO";
        int idReg = rfidDao.insertarRegistroRFID(datos, estado,
                existe ? "Tarjeta reconocida" : "Tag no registrado");
        System.out.println((existe ? "RFID reconocido" : "Tag desconocido")
                + " ID_REG=" + idReg);
    }

    @FXML
    private void onBack() {
        // 1) cerrar el puerto antes de cambiar de vista
        lector.cerrar();
        // 2) volver a la vista anterior
        mainController.goBack();
    }
}
*/