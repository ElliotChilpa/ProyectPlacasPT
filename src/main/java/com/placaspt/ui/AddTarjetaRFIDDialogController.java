package com.placaspt.ui;

import com.placaspt.model.TarjetaRFIDPOJO;
import com.placaspt.database.TarjetaRFIDDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.time.LocalDate;

public class AddTarjetaRFIDDialogController implements MainAware {

    @FXML private AnchorPane     root;
    @FXML private TextField      tfRfidId;
    @FXML private DatePicker     dpInicio;
    @FXML private DatePicker     dpFin;
    @FXML private CheckBox       chkActiva;

    // El prmer string es para guardar el RFID asignado
    private String originalRfidId;     // ← almacena el ID antiguo
    private MainController       mainController;
    private final TarjetaRFIDDAO dao = new TarjetaRFIDDAO();
    private int                  usuarioFijoId;
    private TarjetaRFIDPOJO      existing;  // null => alta

    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }

    /**
     * Init data: ID usuario fijo y objeto existente (o null).
     */
    /*public void initData(int usuarioFijoId, TarjetaRFIDPOJO existing) {
        this.usuarioFijoId = usuarioFijoId;
        this.existing      = existing;
    }*/

    /**
     * Aquí recibes el ID y el posible POJO existente
     * y de inmediato llenas o dejas en blanco los controles.
     */
    public void initData(int usuarioFijoId, TarjetaRFIDPOJO existing) {
        this.usuarioFijoId = usuarioFijoId;
        this.existing      = existing;

        if (existing != null) {
            originalRfidId = existing.getIdRfid();
            tfRfidId.setText(originalRfidId);
            //tfRfidId.setText(existing.getIdRfid()); // Esto es para bloquear la modificación de RFID
            //tfRfidId.setDisable(true);                     // ID no cambia
            dpInicio.setValue(existing.getFechaInicio());
            dpFin.setValue(existing.getFechaFin());
            chkActiva.setSelected(existing.isActiva());
        } else {
            originalRfidId = null;
            tfRfidId.clear();
            //tfRfidId.setDisable(false);
            dpInicio.setValue(LocalDate.now());
            dpFin.setValue(null);
            chkActiva.setSelected(true);
        }
    }

    public void initialize() {

    }
    /*@FXML
    public void initialize() {
        // Si editamos, precargamos
        if (existing != null) {
            tfRfidId.setText(existing.getIdRfid());
            tfRfidId.setDisable(true);
            dpInicio.setValue(existing.getFechaInicio());
            dpFin.setValue(existing.getFechaFin());
            chkActiva.setSelected(existing.isActiva());
        } else {
            dpInicio.setValue(LocalDate.now());
            chkActiva.setSelected(true);
        }
    }*/

    /**
     * Salvamos o actualizamos la tarjeta y volvemos a la vista Usuarios.
     */
    /*
    @FXML
    private void onGuardarTarjeta() {
        // ... validaciones ...
        TarjetaRFIDPOJO dto = new TarjetaRFIDPOJO(
                tfRfidId.getText().trim(),
                dpInicio.getValue(),
                dpFin.getValue(),
                chkActiva.isSelected(),
                /*fkAdmin* /1,
                usuarioFijoId
        );

        boolean ok = existing == null
                ? dao.insertarTarjeta(dto)
                : dao.actualizarTarjeta(dto);

        if (!ok) {
            new Alert(Alert.AlertType.ERROR,
                    "Error al guardar la tarjeta RFID.").showAndWait();
            return;
        }
        //mainController.onCancelar();  // o loadView("UsuariosView.fxml")
        mainController.loadView("/com/placaspt/ui/UsuariosView.fxml");
    }*/

    /**
     * Salvamos o actualizamos la tarjeta y volvemos a la vista Usuarios.
     */

    @FXML
    private void onGuardarTarjeta() {
        String nuevoId = tfRfidId.getText().trim();
        LocalDate inicio = dpInicio.getValue();
        LocalDate fin    = dpFin.getValue();
        boolean activa   = chkActiva.isSelected();

        // … tus validaciones …

        TarjetaRFIDPOJO dto = new TarjetaRFIDPOJO(
                nuevoId, inicio, fin, activa,
                /*fkAdmin*/1, usuarioFijoId
        );

        boolean ok;
        if (existing == null) {
            ok = dao.insertarTarjeta(dto);
        } else {
            ok = dao.actualizarTarjeta(originalRfidId, dto);
        }

        if (!ok) {
            new Alert(Alert.AlertType.ERROR,
                    "Error al guardar la tarjeta RFID.").showAndWait();
            return;
        }
        //mainController.goBack();
        mainController.loadView("/com/placaspt/ui/UsuariosView.fxml");
    }

    /** Simplemente vuelve a la vista usuarios sin guardar
     * */


    @FXML
    private void onCancelar() {
        mainController.loadView("/com/placaspt/ui/UsuariosView.fxml");
    }
}
