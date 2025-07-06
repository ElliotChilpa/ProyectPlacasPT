package com.placaspt.ui;

import com.placaspt.database.AccesoDAO;
import com.placaspt.logic.EstacionamientoService;
import com.placaspt.logic.RS232RFID;
import com.placaspt.logic.RaspberryPollingService;
import com.placaspt.model.AccesoViewDTO;
import com.placaspt.model.EventoPlacaDTO;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EstacionamientoController implements MainAware {
    // — Servicios y utilidades —
    private final EstacionamientoService     estacionamientoService = new EstacionamientoService();
    private RaspberryPollingService          pollingService;
    private final RS232RFID                  lector                 = RS232RFID.getInstance();
    private final AccesoDAO                  accesoDAO              = new AccesoDAO();

    private MainController                   mainController;
    private static final int                 CAPACIDAD_TOTAL        = 30;
    private final DateTimeFormatter          dtf                    = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
    @FXML private Button      btnBack;

    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }

    @FXML
    private void initialize() {
        // 1) Abrir puerto RFID si no está ya abierto
        if (!lector.isOpen()) {
            if (!lector.iniciar("COM4")) {
                System.err.println("[RFID] No se pudo abrir COM4");
            }
        }
        actualizarStatus();

        // 2) Escuchar lecturas RFID y delegar al servicio
        lector.escuchar(tag -> Platform.runLater(() -> {
            System.out.println("[RFID] tag: " + tag);
            estacionamientoService.procesarRfid(tag);
            recargar();
        }));

        // 3) Configurar tabla y filtros
        setupTableColumns();
        setupFilters();
        recargar();

        // 4) Arrancar polling de placas desde Raspberry
        String host          = "192.168.100.254";
        String user          = "placasPT";
        String pass          = "8586";
        String remotoArchivo = "/home/placasPT/placasPTpi/pruebas-YOLO/output.json";
        long periodo         = 1; // segundos

        pollingService = new RaspberryPollingService(
                host, user, pass, remotoArchivo,
                periodo, TimeUnit.SECONDS,
                ev -> {
                    System.out.println("[JSON] evento placa: " + ev.getPlate() + " / " + ev.getGate());
                    estacionamientoService.procesarEvento(ev);
                    Platform.runLater(() -> {
                        recargar();
                        showEventoAlert(ev);
                    });
                }
        );
        pollingService.start();
    }

    /** Columnas de la tabla */
    private void setupTableColumns() {
        colFechaHora.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
        colFechaHora.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime ts, boolean empty) {
                super.updateItem(ts, empty);
                setText(empty || ts==null ? null : ts.format(dtf));
            }
        });

        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colPlaca  .setCellValueFactory(new PropertyValueFactory<>("placa"));
        colMetodo .setCellValueFactory(new PropertyValueFactory<>("metodo"));
        colEstado .setCellValueFactory(new PropertyValueFactory<>("estado"));

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnCerrar = new Button("Cerrar Salida");
            {
                btnCerrar.setOnAction(e -> {
                    AccesoViewDTO a = getTableView().getItems().get(getIndex());
                    cerrarSalida(a.getIdAcceso());
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    AccesoViewDTO a = getTableView().getItems().get(getIndex());
                    btnCerrar.setDisable(!"INGRESO".equals(a.getEstado()));
                    setGraphic(btnCerrar);
                }
            }
        });
    }

    /** Filtros de fecha y texto */
    private void setupFilters() {
        dpFecha.setValue(LocalDate.now());
        dpFecha.valueProperty().addListener((o,oldV,newV)->recargar());
        tfFiltro.textProperty().addListener((o,oldV,newV)->filtrar());
    }

    /** Recarga los accesos desde BD y actualiza contador */
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

    /** Filtra la tabla por usuario o placa */
    private void filtrar() {
        String txt = tfFiltro.getText().toLowerCase().trim();
        if (txt.isEmpty()) {
            recargar();
        } else {
            var filtered = tblAccesos.getItems().stream()
                    .filter(a ->
                            a.getUsuario().toLowerCase().contains(txt) ||
                                    a.getPlaca().toLowerCase().contains(txt)
                    )
                    .toList();
            tblAccesos.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    /** Cerrar manualmente la salida de un acceso abierto */
    private void cerrarSalida(int idAcceso) {
        boolean ok = accesoDAO.cerrarSalida(idAcceso);
        if (!ok) {
            new Alert(Alert.AlertType.ERROR, "No se pudo cerrar la salida").showAndWait();
        }
        recargar();
    }

    /** Muestra una alerta breve cuando llega un evento de placa */
    private void showEventoAlert(EventoPlacaDTO ev) {
        String mensaje;
        Alert.AlertType tipo;
        switch(ev.getGate()) {
            case INGRESO -> {
                mensaje = "Ingreso detectado: " + ev.getPlate();
                tipo = Alert.AlertType.INFORMATION;
            }
            case SALIDA -> {
                mensaje = "Salida detectada: " + ev.getPlate();
                tipo = Alert.AlertType.INFORMATION;
            }
            default -> {
                mensaje = "Acceso DENEGADO: " + ev.getPlate();
                tipo = Alert.AlertType.ERROR;
            }
        }
        // mostramos sin bloquear el hilo de polling
        Platform.runLater(() -> new Alert(tipo, mensaje).show());
    }

    /** Botón “Refrescar puerto” */
    @FXML private void refrescarPuerto() {
        if (lector.isOpen()) lector.cerrar();
        boolean ok = lector.iniciar("COM4");
        actualizarStatus();
        new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                ok ? "Puerto COM4 abierto correctamente"
                        : "Error al abrir COM4")
                .showAndWait();
    }

    /** Actualiza el color del círculo según estado del puerto */
    private void actualizarStatus() {
        statusCircle.setFill(lector.isOpen() ? Color.GREEN : Color.RED);
    }

    /** Al volver atrás: detenemos polling y cerramos puerto */
    @FXML private void onBack() {
        if (pollingService != null) pollingService.stop();
        lector.cerrar();
        mainController.goBack();
    }
}
