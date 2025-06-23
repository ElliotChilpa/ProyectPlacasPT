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
    public void initData(int usuarioFijoId, TarjetaRFIDPOJO existing) {
        this.usuarioFijoId = usuarioFijoId;
        this.existing      = existing;
    }

    @FXML
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
    }

    /**
     * Salvamos o actualizamos la tarjeta y volvemos a la vista Usuarios.
     */

    @FXML
    private void onGuardarTarjeta() {
        System.out.println("Guardando para UsuarioFijoId=" + usuarioFijoId);
        String idRfid = tfRfidId.getText().trim();
        LocalDate inicio = dpInicio.getValue();
        LocalDate fin    = dpFin.getValue();
        boolean activa   = chkActiva.isSelected();

        if (idRfid.isEmpty() || inicio == null || fin == null || fin.isBefore(inicio)) {
            new Alert(Alert.AlertType.WARNING,
                    "Completa correctamente todos los campos.").showAndWait();
            return;
        }

        // Prueba para ver que usuario fijo puede agregar rfid
        System.out.println("FK_UsuarioFijo = " + usuarioFijoId);

        TarjetaRFIDPOJO dto = new TarjetaRFIDPOJO(
                idRfid, inicio, fin, activa, /*fkAdmin*/ 1, usuarioFijoId
        );

        boolean ok = (existing != null)
                ? dao.actualizarTarjeta(dto)
                : dao.insertarTarjeta(dto);

        if (!ok) {
            new Alert(Alert.AlertType.ERROR,
                    "Error al guardar la tarjeta RFID.").showAndWait();
            return;
        }

        // Regresamos a la vista de usuarios
        mainController.loadView("/com/placaspt/ui/UsuariosView.fxml");
    }

    /** Simplemente vuelve a la vista usuarios sin guardar
     * */


    @FXML
    private void onCancelar() {
        mainController.loadView("/com/placaspt/ui/UsuariosView.fxml");
    }
}
