package com.placaspt.ui;

import com.placaspt.model.TarjetaRFIDPOJO;
import com.placaspt.database.TarjetaRFIDDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AddTarjetaRFIDDialogController {

    @FXML private DialogPane dialogPane;
    @FXML private TextField   tfRfidId;
    @FXML private DatePicker  dpInicio;
    @FXML private DatePicker  dpFin;
    @FXML private CheckBox    chkActiva;

    private final TarjetaRFIDDAO dao = new TarjetaRFIDDAO();
    private int usuarioFijoId;
    private TarjetaRFIDPOJO existing;  // si no es null, estamos en modo edición

    /**
     * Debes llamar a este método justo después de cargar el FXML,
     * antes de mostrar el diálogo.
     *
     * @param usuarioFijoId El ID del usuario fijo al que asignar/editar la tarjeta
     * @param existing      La tarjeta existente (null para alta)
     */
    public void initData(int usuarioFijoId, TarjetaRFIDPOJO existing) {
        this.usuarioFijoId = usuarioFijoId;
        this.existing      = existing;
    }

    @FXML
    public void initialize() {
        // Configurar el botón "Guardar" para que valide antes de cerrar:
        Button btnGuardar = (Button) dialogPane.lookupButton(ButtonType.OK);
        btnGuardar.addEventFilter(ActionEvent.ACTION, e -> {
            if (!onGuardarTarjeta()) {
                e.consume();
            }
        });

        // Si hay una tarjeta existente, precargamos sus valores:
        if (existing != null) {
            tfRfidId.setText(existing.getIdRfid());
            tfRfidId.setDisable(true);  // no permitir cambiar el ID
            dpInicio.setValue(existing.getFechaInicio());
            dpFin.   setValue(existing.getFechaFin());
            chkActiva.setSelected(existing.isActiva());
        } else {
            // Valores por defecto para alta:
            dpInicio.setValue(LocalDate.now());
            chkActiva.setSelected(true);
        }
    }

    /**
     * Valida los datos de la UI, llama al DAO para insertar o actualizar.
     * @return true si todo salió bien (y el diálogo puede cerrar), false si hay error.
     */
    private boolean onGuardarTarjeta() {
        String idRfid = tfRfidId.getText().trim();
        LocalDate inicio = dpInicio.getValue();
        LocalDate fin    = dpFin.getValue();
        boolean activa   = chkActiva.isSelected();

        // 1) Validaciones básicas
        if (idRfid.isEmpty() || inicio == null || fin == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Debes completar ID, Fecha Inicio y Fecha Fin.").showAndWait();
            return false;
        }
        if (fin.isBefore(inicio)) {
            new Alert(Alert.AlertType.WARNING,
                    "La Fecha Fin no puede ser anterior a la Fecha Inicio.").showAndWait();
            return false;
        }

        // 2) Construir el POJO
        // Nota: sustituye el "1" por tu ID de administrador actual si lo gestionas dinámicamente
        TarjetaRFIDPOJO tarjeta = new TarjetaRFIDPOJO(
                idRfid,
                inicio,
                fin,
                activa,
                /*fkAdministrador=*/1,
                usuarioFijoId
        );

        // 3) Insertar o actualizar según corresponda
        boolean ok;
        if (existing != null) {
            ok = dao.actualizarTarjeta(tarjeta);
        } else {
            ok = dao.insertarTarjeta(tarjeta);
        }

        // 4) Manejo de error
        if (!ok) {
            new Alert(Alert.AlertType.ERROR,
                    "Ocurrió un error al guardar la tarjeta RFID.").showAndWait();
        }
        return ok;
    }

    /**
     * Método opcional para cerrar manualmente el diálogo.
     */
    public void closeDialog() {
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.close();
    }
}
