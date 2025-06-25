/*package com.placaspt.ui;

import com.placaspt.database.UsuariosDAO;
import com.placaspt.model.UsuarioTemporalPOJO;
import com.placaspt.model.UsuariosPOJO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
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
    @FXML private RadioButton rbUsuarioFijo;
    @FXML private RadioButton rbUsuarioTemporal;
    @FXML private GridPane   paneUsuarioTemporal;
    @FXML private TextField  usuarioNombreTf;
    @FXML private TextField  usuarioApellidosTf;
    @FXML private TextField  usuarioCorreoTf;
    @FXML private TextField  usuarioTelefonoTf;
    @FXML private TextField  usuarioTempActividad;
    @FXML private DatePicker fechaInicio;
    @FXML private DatePicker fechaFin;
    @FXML private Button     btnCargarFoto;
    @FXML private ImageView  dniPreview;
    @FXML private Button     btnAgregar;

    private MainController mainController;
    private File dniFile;
    private final UsuariosDAO dao = new UsuariosDAO();

    // ——— NUEVO: campo para modo edición ———
    private UsuariosPOJO existingUser;  // null = alta, no-null = edición

    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }

    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        rbUsuarioFijo.setToggleGroup(group);
        rbUsuarioTemporal.setToggleGroup(group);

        paneUsuarioTemporal.managedProperty().bind(rbUsuarioTemporal.selectedProperty());
        paneUsuarioTemporal.visibleProperty().bind(rbUsuarioTemporal.selectedProperty());

        btnCargarFoto.setOnAction(e -> {
            FileChooser ch = new FileChooser();
            ch.setTitle("Selecciona foto de credencial");
            ch.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Imágenes", "*.png","*.jpg","*.jpeg")
            );
            File f = ch.showOpenDialog(btnCargarFoto.getScene().getWindow());
            if (f != null) {
                dniFile = f;
                dniPreview.setImage(new Image(f.toURI().toString()));
            }
        });
    }

    /*
    // ——— NUEVO: método para precargar datos en modo edición ———
    public void initData(UsuariosPOJO userBase,
                         boolean isFijo,
                         String actividad,
                         LocalDate inicio,
                         LocalDate fin,
                         Blob documento) {
        this.existingUser = userBase;

        // 1) precarga campos base
        usuarioNombreTf.setText(userBase.getNombres());
        usuarioApellidosTf.setText(userBase.getApellidos());
        usuarioCorreoTf.setText(userBase.getCorreo());
        usuarioTelefonoTf.setText(userBase.getTelefono());

        // 2) seleccionar tipo
        if (isFijo) {
            rbUsuarioFijo.setSelected(true);
        } else {
            rbUsuarioTemporal.setSelected(true);
            // precargar subcampos temporales
            usuarioTempActividad.setText(actividad);
            fechaInicio.setValue(inicio);
            fechaFin.setValue(fin);
            // si tuvieras el blob, podrías convertirlo a File y mostrar:
            if (documento != null) {
                try {
                    byte[] data = documento.getBytes(1, (int) documento.length());
                    File tmp = File.createTempFile("dni", ".png");
                    Files.write(tmp.toPath(), data);
                    dniFile = tmp;
                    dniPreview.setImage(new Image(tmp.toURI().toString()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        // Cambiar texto del botón
        btnAgregar.setText("Guardar cambios");
    }*/
    /**
     * Sobrecarga para simplificar la llamada desde UsuariosController:
     * sólo recibe el UsuariosPOJO y se encarga de buscar subdatos.
     * /
    public void initData(UsuariosPOJO userBase) {
        boolean isFijo = new UsuariosDAO().existeUsuarioFijo(userBase.getId());
        Blob doc = null;
        String actividad = null;
        LocalDate inicio = null, fin = null;

        if (!isFijo) {
            UsuarioTemporalPOJO tmp = new UsuariosDAO().buscarTemporalPorUsuario(userBase.getId());
            actividad = tmp.getActividadRealizar();
            inicio    = tmp.getFechaInicio();
            fin       = tmp.getFechaFin();
            doc       = tmp.getDocumentoDNI();
        }

        // Llama a tu método original con toda la info
        initData(userBase, isFijo, actividad, inicio, fin, doc);
        // y cambia el texto del botón si quieres
        btnAgregar.setText("Guardar cambios");
    }


    @FXML
    private void onBack() {
        mainController.goBack();
    }

    @FXML
    private void onGuardar() {
        // 1) Lectura y validación de campos
        String nom  = usuarioNombreTf.getText().trim();
        String ape  = usuarioApellidosTf.getText().trim();
        String cor  = usuarioCorreoTf.getText().trim();
        String tel  = usuarioTelefonoTf.getText().trim();
        if (nom.isEmpty() || ape.isEmpty() || cor.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Nombre, Apellidos y Correo son obligatorios")
                    .showAndWait();
            return;
        }

        try {
            if (existingUser == null) {
                // ——— MODO ALTA ———
                UsuariosPOJO u = new UsuariosPOJO(
                        nom, ape, cor, tel,
                        LocalDate.now(),
                        1  // idAdministrador
                );
                int newId = dao.insertarUsuario(u);
                if (newId < 1) throw new SQLException("No pudo crear Usuario");

                if (rbUsuarioTemporal.isSelected()) {
                    // insertar subregistro temporal
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
                    boolean okF = dao.insertarUsuarioFijo(newId);
                    if (!okF) throw new SQLException("No pudo crear Usuario Fijo");
                }

                new Alert(Alert.AlertType.INFORMATION, "Usuario creado").showAndWait();
            } else {
                // ——— MODO EDICIÓN ———
                // 1) Actualizar base Usuario
                existingUser.setNombres(nom);
                existingUser.setApellidos(ape);
                existingUser.setCorreo(cor);
                existingUser.setTelefono(tel);
                boolean ok = dao.actualizarUsuario(existingUser);
                if (!ok) throw new SQLException("No pudo actualizar Usuario");

                // 2) Actualizar subregistro según radio
                if (rbUsuarioTemporal.isSelected()) {
                    // suponiendo que ya existe, aquí deberías llamar a:
                    // dao.actualizarUsuarioTemporal(existingUser.getId(), ...)
                    // (aún debes implementar ese método en UsuariosDAO)
                } else {
                    // fijo: igual, podrías no necesitar nada o llamar a actualizarUsuarioFijo
                }

                new Alert(Alert.AlertType.INFORMATION, "Usuario actualizado").showAndWait();
            }
            mainController.goBack();

        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al guardar:\n" + ex.getMessage())
                    .showAndWait();
        }
    }
}
*/
package com.placaspt.ui;

import com.placaspt.database.UsuariosDAO;
import com.placaspt.model.UsuarioTemporalPOJO;
import com.placaspt.model.UsuariosPOJO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
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
    @FXML private RadioButton rbUsuarioFijo;
    @FXML private RadioButton rbUsuarioTemporal;
    @FXML private GridPane   paneUsuarioTemporal;
    @FXML private TextField  usuarioNombreTf;
    @FXML private TextField  usuarioApellidosTf;
    @FXML private TextField  usuarioCorreoTf;
    @FXML private TextField  usuarioTelefonoTf;
    @FXML private TextField  usuarioTempActividad;
    @FXML private DatePicker fechaInicio;
    @FXML private DatePicker fechaFin;
    @FXML private Button     btnCargarFoto;
    @FXML private ImageView  dniPreview;
    @FXML private Button     btnAgregar;

    private MainController     mainController;
    private File               dniFile;
    private final UsuariosDAO  dao = new UsuariosDAO();

    // ——— Modo edición ———
    private UsuariosPOJO existingUser;    // null = alta, no-null = edición
    private int          existingTempId;  // PK de UsuarioTemporal (si aplica)

    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }

    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        rbUsuarioFijo.setToggleGroup(group);
        rbUsuarioTemporal.setToggleGroup(group);

        paneUsuarioTemporal.managedProperty().bind(rbUsuarioTemporal.selectedProperty());
        paneUsuarioTemporal.visibleProperty().bind(rbUsuarioTemporal.selectedProperty());

        btnCargarFoto.setOnAction(e -> {
            FileChooser ch = new FileChooser();
            ch.setTitle("Selecciona foto de credencial");
            ch.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Imágenes", "*.png","*.jpg","*.jpeg")
            );
            File f = ch.showOpenDialog(btnCargarFoto.getScene().getWindow());
            if (f != null) {
                dniFile = f;
                dniPreview.setImage(new Image(f.toURI().toString()));
            }
        });
    }

    /**
     * Modo edición completo: precarga usuario base y subregistro temporal.
     *
     * @param userBase             datos de la tabla Usuario
     * @param isFijo               true si es usuario fijo
     * @param actividad            actividad de UsuarioTemporal
     * @param inicio               fecha de inicio
     * @param fin                  fecha de fin
     * @param documento            blob del DNI
     * @param idUsuarioTemporal    PK de UsuarioTemporal
     */
    public void initData(UsuariosPOJO userBase,
                         boolean isFijo,
                         String actividad,
                         LocalDate inicio,
                         LocalDate fin,
                         Blob documento,
                         int idUsuarioTemporal) {
        this.existingUser    = userBase;
        this.existingTempId  = idUsuarioTemporal;

        // 1) Precarga campos base
        usuarioNombreTf.setText(userBase.getNombres());
        usuarioApellidosTf.setText(userBase.getApellidos());
        usuarioCorreoTf.setText(userBase.getCorreo());
        usuarioTelefonoTf.setText(userBase.getTelefono());

        // 2) Tipo de usuario
        rbUsuarioFijo.setSelected(isFijo);
        rbUsuarioTemporal.setSelected(!isFijo);

        // 3) Subregistro temporal
        if (!isFijo) {
            usuarioTempActividad.setText(actividad != null ? actividad : "");
            fechaInicio.setValue(inicio);
            fechaFin.setValue(fin);
            if (documento != null) {
                try {
                    byte[] data = documento.getBytes(1, (int) documento.length());
                    File tmp = File.createTempFile("dni", ".tmp");
                    Files.write(tmp.toPath(), data);
                    dniFile = tmp;
                    dniPreview.setImage(new Image(tmp.toURI().toString()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        // Cambiar texto del botón
        btnAgregar.setText("Guardar cambios");
    }

    /**
     * Sobrecarga para llamada sencilla desde UsuariosController:
     * solo pasa el UsuariosPOJO y el formulario resuelve el resto.
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
        //mainController.goBack();
        mainController.loadView("/com/placaspt/ui/UsuariosView.fxml");
    }

    @FXML
    private void onGuardar() {
        // 1) Lectura y validación de campos
        String nom = usuarioNombreTf.getText().trim();
        String ape = usuarioApellidosTf.getText().trim();
        String cor = usuarioCorreoTf.getText().trim();
        String tel = usuarioTelefonoTf.getText().trim();
        if (nom.isEmpty() || ape.isEmpty() || cor.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Nombre, Apellidos y Correo son obligatorios")
                    .showAndWait();
            return;
        }

        try {
            if (existingUser == null) {
                // ——— MODO ALTA ———
                UsuariosPOJO u = new UsuariosPOJO(
                        nom, ape, cor, tel,
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
                    boolean okF = dao.insertarUsuarioFijo(newId);
                    if (!okF) throw new SQLException("No pudo crear Usuario Fijo");
                }

                new Alert(Alert.AlertType.INFORMATION, "Usuario creado").showAndWait();
            } else {
                // ——— MODO EDICIÓN ———
                // 1) Actualizar datos de Usuario
                existingUser.setNombres(nom);
                existingUser.setApellidos(ape);
                existingUser.setCorreo(cor);
                existingUser.setTelefono(tel);
                boolean ok = dao.actualizarUsuario(existingUser);
                if (!ok) throw new SQLException("No pudo actualizar Usuario");

                // 2) Actualizar subregistro temporal si aplica
                if (rbUsuarioTemporal.isSelected()) {
                    byte[] data = dniFile != null
                            ? Files.readAllBytes(dniFile.toPath())
                            : null;
                    Blob doc = data != null ? new SerialBlob(data) : null;
                    boolean okTmp = dao.actualizarUsuarioTemporal(
                            existingTempId,
                            usuarioTempActividad.getText().trim(),
                            fechaInicio.getValue(),
                            fechaFin.getValue(),
                            doc
                    );
                    if (!okTmp) throw new SQLException("No pudo actualizar Usuario Temporal");
                }

                new Alert(Alert.AlertType.INFORMATION, "Usuario actualizado").showAndWait();
            }

            //mainController.goBack();
            mainController.loadView("/com/placaspt/ui/UsuariosView.fxml");

        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al guardar:\n" + ex.getMessage())
                    .showAndWait();
        }
    }
}
