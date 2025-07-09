package com.placaspt.ui;

import com.placaspt.database.AccesoDAO;
import com.placaspt.logic.AppEventListener;
import com.placaspt.logic.AppService;
import com.placaspt.model.EventoEstado;
import com.placaspt.logic.EstacionamientoService;
import com.placaspt.logic.RS232RFID;
import com.placaspt.model.AccesoViewDTO;
import com.placaspt.model.EventoPlacaDTO;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EstacionamientoController implements MainAware, AppEventListener {
    // — Servicios y utilidades —
    private final EstacionamientoService estacionamientoService = new EstacionamientoService();
    private final AccesoDAO            accesoDAO               = new AccesoDAO();
    private final RS232RFID            lector                  = RS232RFID.getInstance();

    private MainController   mainController;
    private static final int CAPACIDAD_TOTAL = 30;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // —– Panel de avisos —–
    @FXML private Pane statusRect;
    @FXML private Label     statusLabel;
    // Para temporizar la vuelta a blanco
    private PauseTransition avisoPause;
    // —– FXML inyectados —–
    @FXML private Label      lblOcupados;
    @FXML private DatePicker dpFecha;
    @FXML private TextField  tfFiltro;
    @FXML private TableView<AccesoViewDTO>    tblAccesos;
    @FXML private TableColumn<AccesoViewDTO, LocalDateTime> colFechaHora;
    @FXML private TableColumn<AccesoViewDTO, String> colUsuario;
    @FXML private TableColumn<AccesoViewDTO, String> colPlaca;
    @FXML private TableColumn<AccesoViewDTO, String> colMetodo;
    @FXML private TableColumn<AccesoViewDTO, String> colEstado;
    @FXML private TableColumn<AccesoViewDTO, Void>   colAccion;
    @FXML private Button      btnRefrescarPuerto;
    @FXML private Circle      statusCircle;
    @FXML private AnchorPane  root;

    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }

    // ——— AppEventListener ———
    @Override
    public void onRfidTag(String tag) {
        Platform.runLater(() -> {
            EventoEstado estado = estacionamientoService.procesarRfid(tag);
            recargar();
            switch (estado) {
                case INGRESO -> showIngresoAlert(tag);
                case SALIDA  -> showSalidaAlert(tag);
                case DENEGADO-> showDenegadoAlert(tag);
            }
        });
    }

    @Override
    public void onPlacaEvent(EventoPlacaDTO ev) {
        Platform.runLater(() -> {
            EventoEstado estado = estacionamientoService.procesarEvento(ev);
            recargar();

            String placa = ev.getPlate();
            switch (estado) {
                case INGRESO  -> showIngresoAlert(placa);
                case SALIDA   -> showSalidaAlert(placa);
                case DENEGADO -> showDenegadoAlert(placa);
            }
        });
    }
    /*
    @Override
    public void onPlacaEvent(EventoPlacaDTO ev) {
        Platform.runLater(() -> {
            EventoEstado estado = estacionamientoService.procesarEvento(ev);
            recargar();
            String id = ev.getPlate();
            switch (estado) {
                case INGRESO -> showIngresoAlert(id);
                case SALIDA  -> showSalidaAlert(id);
                case DENEGADO-> showDenegadoAlert(id);
            }
        });
    }*/


    @FXML
    private void initialize() {
        // 1) registro para recibir eventos desde AppService
        AppService.getInstance().registerListener(this);
        // Hacemos que el Rectangle siga siempre la altura de la tabla

        // 2) UI → tablas, filtros, estado
        setupTableColumns();
        setupFilters();
        recargar();
        actualizarStatus();
    }

    /** Columnas y botón “Cerrar Salida” */
    private void setupTableColumns() {
        colFechaHora.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
        colFechaHora.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime ts, boolean empty) {
                super.updateItem(ts, empty);
                setText(empty || ts == null ? null : ts.format(dtf));
            }
        });
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colPlaca  .setCellValueFactory(new PropertyValueFactory<>("placa"));
        colMetodo .setCellValueFactory(new PropertyValueFactory<>("metodo"));
        //colEstado .setCellValueFactory(new PropertyValueFactory<>("estado"));
        // Columna Estado
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setCellFactory(col -> new TableCell<AccesoViewDTO, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    String color = switch (estado) {
                        case "INGRESO"  -> "green";
                        case "SALIDA"   -> "dodgerblue";
                        default         -> "crimson";   // DENEGADO
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnCerrar = new Button("Cerrar Salida");
            {
                btnCerrar.setOnAction(e -> {
                    var a = getTableView().getItems().get(getIndex());
                    cerrarSalida(a.getIdAcceso());
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    var a = getTableView().getItems().get(getIndex());
                    btnCerrar.setDisable(!"INGRESO".equals(a.getEstado()));
                    setGraphic(btnCerrar);
                }
            }
        });
    }

    /** Filtros de fecha y texto */
    private void setupFilters() {
        dpFecha.setValue(LocalDate.now());
        dpFecha.valueProperty().addListener((o,oldV,newV)-> recargar());
        tfFiltro.textProperty().addListener((o,oldV,newV)-> filtrar());
    }

    /** Recarga la tabla y actualiza el contador */
    private void recargar() {
        List<AccesoViewDTO> lista = accesoDAO.listarAccesosPorFecha(dpFecha.getValue());
        var obs = FXCollections.observableArrayList(lista);
        tblAccesos.setItems(obs);
        if (!obs.isEmpty()) tblAccesos.scrollTo(obs.size()-1);

        long dentro = lista.stream()
                .filter(a->"INGRESO".equals(a.getEstado()))
                .count();
        lblOcupados.setText("Espacios ocupados: " + dentro + " de " + CAPACIDAD_TOTAL);
    }

    /** Filtra por texto en usuario o placa */
    private void filtrar() {
        String txt = tfFiltro.getText().toLowerCase().trim();
        if (txt.isEmpty()) {
            recargar();
        } else {
            var filtered = tblAccesos.getItems().stream()
                    .filter(a-> a.getUsuario().toLowerCase().contains(txt)
                            || a.getPlaca() .toLowerCase().contains(txt))
                    .toList();
            tblAccesos.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    /** Cierra manualmente una salida */
    private void cerrarSalida(int idAcceso) {
        if (!accesoDAO.cerrarSalida(idAcceso)) {
            new Alert(Alert.AlertType.ERROR, "No se pudo cerrar la salida")
                    .showAndWait();
        }
        recargar();
    }

    // ———  Tres métodos de alerta  ———

    /** Alerta de ingreso permitido */
    private void showIngresoAlert(String id) {
        if (avisoPause != null) avisoPause.stop();
        mostrarAviso("green", "Acceso permitido\nUsuario: " + id);
        avisoPause = new PauseTransition(Duration.seconds(5));
        avisoPause.setOnFinished(e -> mostrarAviso("WHITE", ""));
        avisoPause.play();
    }

    /** Alerta de salida registrada */
    private void showSalidaAlert(String id) {
        if (avisoPause != null) avisoPause.stop();
        mostrarAviso("dodgerblue", "Salida permitida\nUsuario: " + id);
        avisoPause = new PauseTransition(Duration.seconds(5));
        avisoPause.setOnFinished(e -> mostrarAviso("WHITE", ""));
        avisoPause.play();
    }
    /** Alerta de acceso denegado */
    private void showDenegadoAlert(String id) {
        if (avisoPause != null) avisoPause.stop();
        mostrarAviso("crimson", "Acceso DENEGADO\nTag/Placa: " + id);
        avisoPause = new PauseTransition(Duration.seconds(2));
        avisoPause.setOnFinished(e -> mostrarAviso("WHITE", ""));
        avisoPause.play();
    }

    /**
     * Muestra un aviso en el recuadro: pinta el rectángulo y pone el texto.
     *
     * @param mensaje    texto a mostrar junto al rectángulo
     */
    private void mostrarAviso(String colorCss, String mensaje) {
        // colorCss puede ser "green", "#FFFFFF", etc.
        statusRect.setStyle(
                "-fx-background-color: " + colorCss + ";" +
                        "-fx-border-color: #333;" +
                        "-fx-border-width: 1;"
        );
        statusLabel.setText(mensaje);
    }

    /** Refrescar manual del puerto RFID (debug) */
    @FXML private void refrescarPuerto() {
        if (lector.isOpen()) lector.cerrar();
        boolean ok = RS232RFID.getInstance().iniciar("COM4");
        actualizarStatus();
        new Alert(
                ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                ok ? "Puerto COM4 abierto correctamente"
                        : "Error al abrir COM4"
        ).showAndWait();
    }

    /** Actualiza indicador del lector */
    private void actualizarStatus() {
        statusCircle.setFill(lector.isOpen() ? Color.GREEN : Color.RED);
    }

    /** Al volver atrás, me desregistro y regreso */
    @FXML private void onBack() {
        AppService.getInstance().unregisterListener(this);
        mainController.goBack();
    }
}
