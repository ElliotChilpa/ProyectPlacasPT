package com.placaspt.ui;

import com.placaspt.database.UsuariosDAO;
import com.placaspt.model.UsuarioTemporalPOJO;
import com.placaspt.model.UsuariosPOJO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;    // para paneCamposComunes
import javafx.stage.FileChooser;

import javax.sql.rowset.serial.SerialBlob;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;

public class AgregarUsuarios implements MainAware {

    // ——— FXML controls ———
    @FXML private ToggleButton rbUsuarioFijo;
    @FXML private ToggleButton rbUsuarioTemporal;
    @FXML private HBox         paneCamposComunes;     // envuelve Nombre/Apellido/Correo/Teléfono
    @FXML private HBox         paneUsuarioTemporal;   // campos de actividad, fechas, foto
    @FXML private TextField    usuarioNombreTf;
    @FXML private TextField    usuarioApellidosTf;
    @FXML private TextField    usuarioCorreoTf;
    @FXML private TextField    usuarioTelefonoTf;
    @FXML private TextField    usuarioTempActividad;
    @FXML private DatePicker   fechaInicio;
    @FXML private DatePicker   fechaFin;
    @FXML private Button       btnCargarFoto;
    @FXML private ImageView    dniPreview;
    @FXML private Button       btnAgregar;

    private MainController    mainController;
    private File              dniFile;
    private final UsuariosDAO dao = new UsuariosDAO();

    // ——— Modo edición ———
    private UsuariosPOJO existingUser;   // null = alta, no-null = edición
    private int          existingTempId; // PK de UsuarioTemporal (si aplica)

    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }

    @FXML
    public void initialize() {
        // 1) Creamos y asignamos el ToggleGroup
        ToggleGroup group = new ToggleGroup();
        rbUsuarioFijo.setToggleGroup(group);
        rbUsuarioTemporal.setToggleGroup(group);

        // 2) Ocultamos TODO hasta que selecciones
        paneCamposComunes.managedProperty()
                .bind(rbUsuarioFijo.selectedProperty().or(rbUsuarioTemporal.selectedProperty()));
        paneCamposComunes.visibleProperty()
                .bind(rbUsuarioFijo.selectedProperty().or(rbUsuarioTemporal.selectedProperty()));

        // 3) El bloque temporal solo cuando rbUsuarioTemporal esté seleccionado
        paneUsuarioTemporal.managedProperty()
                .bind(rbUsuarioTemporal.selectedProperty());
        paneUsuarioTemporal.visibleProperty()
                .bind(rbUsuarioTemporal.selectedProperty());

        // 4) Acción para cargar la foto de DNI
        btnCargarFoto.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Selecciona foto de credencial");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
            );
            File f = chooser.showOpenDialog(btnCargarFoto.getScene().getWindow());
            if (f != null) {
                dniFile = f;
                dniPreview.setImage(new Image(f.toURI().toString()));
            }
        });

        // 5) Opcional: que al inicio no esté ninguno seleccionado
        rbUsuarioFijo.setSelected(false);
        rbUsuarioTemporal.setSelected(false);
    }

    /**
     * Modo edición completo: precarga usuario base y subregistro temporal.
     */
    public void initData(UsuariosPOJO userBase,
                         boolean isFijo,
                         String actividad,
                         LocalDate inicio,
                         LocalDate fin,
                         Blob documento,
                         int idUsuarioTemporal) {
        this.existingUser   = userBase;
        this.existingTempId = idUsuarioTemporal;

        // 1) Campos base
        usuarioNombreTf.setText(userBase.getNombres());
        usuarioApellidosTf.setText(userBase.getApellidos());
        usuarioCorreoTf.setText(userBase.getCorreo());
        usuarioTelefonoTf.setText(userBase.getTelefono());

        // 2) Selecciona el tipo de usuario
        rbUsuarioFijo.setSelected(isFijo);
        rbUsuarioTemporal.setSelected(!isFijo);

        // 3) Si es temporal, precarga subregistro
        if (!isFijo) {
            usuarioTempActividad.setText(actividad != null ? actividad : "");
            fechaInicio.setValue(inicio);
            fechaFin.setValue(fin);
            if (documento != null) {
                try {
                    byte[] data = documento.getBytes(1, (int) documento.length());
                    File tmp   = File.createTempFile("dni", ".tmp");
                    Files.write(tmp.toPath(), data);
                    dniFile    = tmp;
                    dniPreview.setImage(new Image(tmp.toURI().toString()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        btnAgregar.setText("Guardar cambios");
    }

    /**
     * Sobrecarga para llamada sencilla desde UsuariosController.
     */
    public void initData(UsuariosPOJO userBase) {
        boolean isFijo = dao.existeUsuarioFijo(userBase.getId());
        UsuarioTemporalPOJO tmp = isFijo
                ? null
                : dao.buscarTemporalPorUsuario(userBase.getId());

        initData(
                userBase,
                isFijo,
                tmp != null ? tmp.getActividadRealizar() : null,
                tmp != null ? tmp.getFechaInicio()       : null,
                tmp != null ? tmp.getFechaFin()          : null,
                tmp != null ? tmp.getDocumentoDNI()      : null,
                tmp != null ? tmp.getIdUsuarioTemporal() : -1
        );
    }

    @FXML
    private void onBack() {
        mainController.loadView("/com/placaspt/ui/UsuariosView.fxml");
    }

    @FXML
    private void onGuardar() {
        // 1) Validar que hayas elegido fijo o temporal
        if (!rbUsuarioFijo.isSelected() && !rbUsuarioTemporal.isSelected()) {
            new Alert(Alert.AlertType.ERROR,
                    "Debes seleccionar si es Usuario Fijo o Usuario Temporal.")
                    .showAndWait();
            return;
        }

        // 2) Validación campos obligatorios
        String nom = usuarioNombreTf.getText().trim();
        String ape = usuarioApellidosTf.getText().trim();
        String cor = usuarioCorreoTf.getText().trim();
        if (nom.isEmpty() || ape.isEmpty() || cor.isEmpty()) {
            new Alert(Alert.AlertType.ERROR,
                    "Nombre, Apellidos y Correo son obligatorios.")
                    .showAndWait();
            return;
        }

        try {
            if (existingUser == null) {
                // —— MODO ALTA ——
                UsuariosPOJO u = new UsuariosPOJO(
                        nom,
                        ape,
                        cor,
                        usuarioTelefonoTf.getText().trim(),
                        LocalDate.now(),
                        1  // idAdministrador
                );
                int newId = dao.insertarUsuario(u);
                if (newId < 1) throw new SQLException("No pudo crear Usuario");

                if (rbUsuarioTemporal.isSelected()) {
                    byte[] data = Files.readAllBytes(dniFile.toPath());
                    Blob doc = new SerialBlob(data);
                    boolean ok = dao.insertarUsuarioTemporal(
                            newId,
                            usuarioTempActividad.getText().trim(),
                            fechaInicio.getValue(),
                            fechaFin.getValue(),
                            doc
                    );
                    if (!ok) throw new SQLException("No pudo crear Usuario Temporal");
                } else {
                    if (!dao.insertarUsuarioFijo(newId))
                        throw new SQLException("No pudo crear Usuario Fijo");
                }

                new Alert(Alert.AlertType.INFORMATION, "Usuario creado")
                        .showAndWait();

            } else {
                // —— MODO EDICIÓN ——
                existingUser.setNombres(nom);
                existingUser.setApellidos(ape);
                existingUser.setCorreo(cor);
                existingUser.setTelefono(usuarioTelefonoTf.getText().trim());
                if (!dao.actualizarUsuario(existingUser))
                    throw new SQLException("No pudo actualizar Usuario");

                if (rbUsuarioTemporal.isSelected()) {
                    byte[] data = dniFile != null
                            ? Files.readAllBytes(dniFile.toPath())
                            : null;
                    Blob doc = data != null ? new SerialBlob(data) : null;
                    if (!dao.actualizarUsuarioTemporal(
                            existingTempId,
                            usuarioTempActividad.getText().trim(),
                            fechaInicio.getValue(),
                            fechaFin.getValue(),
                            doc))
                        throw new SQLException("No pudo actualizar Usuario Temporal");
                }

                new Alert(Alert.AlertType.INFORMATION, "Usuario actualizado")
                        .showAndWait();
            }

            mainController.loadView("/com/placaspt/ui/UsuariosView.fxml");

        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Error al guardar:\n" + ex.getMessage())
                    .showAndWait();
        }
    }
}
