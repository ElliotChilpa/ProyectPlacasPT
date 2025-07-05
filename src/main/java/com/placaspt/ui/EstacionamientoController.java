/*package com.placaspt.ui;

import com.placaspt.database.*;
import com.placaspt.logic.RS232RFID;
import com.placaspt.model.AccesoViewDTO;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class EstacionamientoController implements MainAware {
    private MainController mainController;
    private RS232RFID lector = RS232RFID.getInstance();  // Singleton

    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }
    // ——— FXML inyectados ———
    @FXML private Label lblOcupados;
    @FXML private DatePicker dpFecha;
    @FXML private TextField tfFiltro;
    @FXML private TableView<AccesoViewDTO> tblAccesos;
    @FXML private TableColumn<AccesoViewDTO, LocalDateTime> colFechaHora;
    @FXML private TableColumn<AccesoViewDTO, String> colUsuario;
    @FXML private TableColumn<AccesoViewDTO, String> colPlaca;
    @FXML private TableColumn<AccesoViewDTO, String> colMetodo;
    @FXML private TableColumn<AccesoViewDTO, String> colEstado;
    @FXML private TableColumn<AccesoViewDTO, Void> colAccion;
    @FXML private Button btnRefrescarPuerto;
    @FXML private Circle statusCircle;


    @FXML private AnchorPane root;

    // ——— DAOs y lector ———
    //private final RS232RFID lector = new RS232RFID();
    private final TarjetaRFIDDAO tarjetaDAO = new TarjetaRFIDDAO();
    private final RegistroRFIDDAO regRfidDAO = new RegistroRFIDDAO();
    private final RegistroPlacaDAO regPlaDAO = new RegistroPlacaDAO();
    private final AccesoDAO accesoDAO = new AccesoDAO();

    @FXML private Button btnBack;

    private static final int CAPACIDAD_TOTAL = 30;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    @FXML
    private void initialize() {
        RS232RFID lector = RS232RFID.getInstance();

        // Verificar si el puerto está abierto antes de intentar abrirlo
        if (!lector.isOpen()) {
            if (lector.iniciar("COM4")) {
                System.out.println("Puerto COM4 abierto");
            } else {
                System.out.println("Error al abrir COM4");
            }
        }
        // Escuchar el RFID
        lector.escuchar(tag -> Platform.runLater(() -> handleRfid(tag)));

        actualizarStatus(); // Cambiar color del indicador al entrar en la vista

        // — 1) Columnas de la tabla —
        colFechaHora.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
        colFechaHora.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime ts, boolean empty) {
                super.updateItem(ts, empty);
                setText(empty || ts == null ? null : ts.format(dtf));
            }
        });
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        colMetodo.setCellValueFactory(new PropertyValueFactory<>("metodo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

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

        // — 2) Filtros —
        dpFecha.setValue(LocalDate.now());
        dpFecha.valueProperty().addListener((o, oldV, newV) -> recargar());
        tfFiltro.textProperty().addListener((o, oldV, newV) -> filtrar());


        // 3) Carga inicial de la tabla
        recargar();
    }

    /** Lee un tag, valida, registra y refresca la tabla * /
    private void handleRfid(String tag) {
        // 1) ID de UsuarioFijo
        int idUsuarioFijo = tarjetaDAO.obtenerIdUsuarioFijoPorTag(tag);
        boolean permitido = idUsuarioFijo > 0;

        // 1b) Ahora obtenemos el ID de la tabla Usuario
        Integer idUsuario = null;
        if (permitido) {
            idUsuario = new UsuariosDAO().obtenerUsuarioBasePorFijo(idUsuarioFijo);
            if (idUsuario < 0) {
                // fallo inesperado: no encontramos el usuario base
                permitido = false;
                idUsuario = null;
            }
        }

        // 2) Registrar en registrorfid
        String estadoEvt = permitido ? "INGRESO" : "DENEGADO";
        String desc = permitido
                ? "Tarjeta reconocida"
                : "Tag no registrado";
        int idRegRfid = regRfidDAO.insertarRegistroRFID(tag, estadoEvt, desc);

        // 3) Insertar en acceso con el ID_Usuario correcto
        accesoDAO.insertarAcceso(
                LocalDateTime.now(),
                "RFID",
                idUsuario,   // null o el verdadero ID_Usuario
                null,
                idRegRfid
        );

        // 4) Refrescar tabla
        recargar();

        // 5) Breve alarma visual si es DENEGADO
        if (!permitido) {
            lblOcupados.setStyle("-fx-text-fill: red;");
            PauseTransition p = new PauseTransition(Duration.seconds(1));
            p.setOnFinished(e -> lblOcupados.setStyle(""));
            p.play();
        }
    }

    /** Carga los accesos de la fecha seleccionada * /
    private void recargar() {
        var lista = accesoDAO.listarAccesosPorFecha(dpFecha.getValue());
        var obs = FXCollections.observableArrayList(lista);
        tblAccesos.setItems(obs);

        // Desplazar al final
        if (!obs.isEmpty()) {
            tblAccesos.scrollTo(obs.size() - 1);
        }

        actualizarContador(lista);
    }

    /** Filtra la tabla por texto en usuario o placa * /
    private void filtrar() {
        String txt = tfFiltro.getText().toLowerCase().trim();
        if (txt.isEmpty()) {
            recargar();
        } else {
            var filtered = tblAccesos.getItems().stream()
                    .filter(a -> a.getUsuario().toLowerCase().contains(txt)
                            || a.getPlaca().toLowerCase().contains(txt))
                    .collect(Collectors.toList());
            tblAccesos.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    /** Actualiza el label de “Espacios ocupados” * /
    private void actualizarContador(List<AccesoViewDTO> lista) {
        long dentro = lista.stream().filter(a -> "INGRESO".equals(a.getEstado())).count();
        lblOcupados.setText("Espacios ocupados: " + dentro + " de " + CAPACIDAD_TOTAL);
    }

    /** Cierra manualmente un acceso abierto * /
    private void cerrarSalida(int idAcceso) {
        boolean ok = accesoDAO.cerrarSalida(idAcceso);
        if (!ok) {
            new Alert(Alert.AlertType.ERROR, "No se pudo cerrar la salida").showAndWait();
        }
        recargar();
    }

    @FXML
    private void refrescarPuerto() {
        // Primero, cierra el puerto si está abierto
        if (lector.isOpen()) {
            lector.cerrar();
        }

        // Intenta volver a abrir el puerto
        boolean conectado = lector.iniciar("COM4");

        if (conectado) {
            // Si se pudo abrir, actualizamos el círculo a verde
            statusCircle.setFill(Color.GREEN);
            new Alert(Alert.AlertType.INFORMATION, "Puerto COM4 refrescado y abierto correctamente").showAndWait();
        } else {
            // Si no se pudo abrir, actualizamos el círculo a rojo
            statusCircle.setFill(Color.RED);
            new Alert(Alert.AlertType.ERROR, "Error al intentar abrir el puerto COM4").showAndWait();
        }
    }


    public void actualizarStatus() {
        if (lector.isOpen()) {
            statusCircle.setFill(Color.GREEN);  // Conexión activa
        } else {
            statusCircle.setFill(Color.RED);    // Conexión inactiva
        }
    }

    @FXML
    private void onBack() {
        lector.cerrar();
        mainController.goBack();
    }
}*/


package com.placaspt.ui;

import com.placaspt.database.*;
import com.placaspt.logic.EstacionamientoService;
import com.placaspt.logic.RS232RFID;
import com.placaspt.logic.RaspberryPollingService;
import com.placaspt.model.AccesoViewDTO;
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
import java.util.stream.Collectors;

public class EstacionamientoController implements MainAware {
    // inyección del servico de estacionamiento para SSH
    private EstacionamientoService estacionamientoService = new EstacionamientoService();

    // // El polling de la Raspberry
    private RaspberryPollingService pollingService;

    private MainController mainController;
    private RS232RFID lector = RS232RFID.getInstance();  // Singleton

    // ——— FXML inyectados ———
    @FXML private Label lblOcupados;
    @FXML private DatePicker dpFecha;
    @FXML private TextField tfFiltro;
    @FXML private TableView<AccesoViewDTO> tblAccesos;
    @FXML private TableColumn<AccesoViewDTO, LocalDateTime> colFechaHora;
    @FXML private TableColumn<AccesoViewDTO, String> colUsuario;
    @FXML private TableColumn<AccesoViewDTO, String> colPlaca;
    @FXML private TableColumn<AccesoViewDTO, String> colMetodo;
    @FXML private TableColumn<AccesoViewDTO, String> colEstado;
    @FXML private TableColumn<AccesoViewDTO, Void> colAccion;
    @FXML private Button btnRefrescarPuerto;
    @FXML private Circle statusCircle;
    @FXML private AnchorPane root;

    // ——— DAOs y lector ———
    private final TarjetaRFIDDAO tarjetaDAO = new TarjetaRFIDDAO();
    private final RegistroRFIDDAO regRfidDAO = new RegistroRFIDDAO();
    private final RegistroPlacaDAO regPlaDAO = new RegistroPlacaDAO();
    private final AccesoDAO accesoDAO = new AccesoDAO();

    @FXML private Button btnBack;

    private static final int CAPACIDAD_TOTAL = 30;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }

    @FXML
    private void initialize() {
        // Verificar si el puerto está abierto antes de intentar abrirlo
        actualizarStatus();

        // Escuchar el RFID
        lector.escuchar(tag -> Platform.runLater(() -> handleRfid(tag)));

        // — 1) Columnas de la tabla —
        setupTableColumns();

        // — 2) Filtros —
        setupFilters();

        // — 3) Carga inicial de la tabla
        recargar();

        // 2) Arrancar el polling de placas
        //    Ajusta estos parámetros a tu configuración:
        String host        = "192.168.100.254";
        String user        = "placasPT";
        String pass        = "8586";
        String remotoArchivo = "/home/placasPT/placasPTpi/pruebas-YOLO/output4.json";
        long periodo       = 5;

        pollingService = new RaspberryPollingService(
                host,
                user,
                pass,
                remotoArchivo,
                periodo,
                TimeUnit.SECONDS,
                ev -> {
                    // ev es un EventoPlacaDTO deserializado con Jackson
                    estacionamientoService.procesarEvento(ev);
                    // luego refrescamos la tabla en JavaFX
                    Platform.runLater(this::recargar);
                }
        );
        pollingService.start();
    }

    /** Configura las columnas de la tabla */
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
        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        colMetodo.setCellValueFactory(new PropertyValueFactory<>("metodo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

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

    /** Configura los filtros de la tabla */
    private void setupFilters() {
        dpFecha.setValue(LocalDate.now());
        dpFecha.valueProperty().addListener((o, oldV, newV) -> recargar());
        tfFiltro.textProperty().addListener((o, oldV, newV) -> filtrar());
    }

    /** Lee un tag, valida, registra y refresca la tabla */
    private void handleRfid(String tag) {
        // 1) ID de UsuarioFijo
        int idUsuarioFijo = tarjetaDAO.obtenerIdUsuarioFijoPorTag(tag);
        boolean permitido = idUsuarioFijo > 0;

        // 1b) Ahora obtenemos el ID de la tabla Usuario
        Integer idUsuario = null;
        if (permitido) {
            idUsuario = new UsuariosDAO().obtenerUsuarioBasePorFijo(idUsuarioFijo);
            if (idUsuario < 0) {
                // fallo inesperado: no encontramos el usuario base
                permitido = false;
                idUsuario = null;
            }
        }

        // 2) Registrar en registrorfid
        String estadoEvt = permitido ? "INGRESO" : "DENEGADO";
        String desc = permitido ? "Tarjeta reconocida" : "Tag no registrado";
        int idRegRfid = regRfidDAO.insertarRegistroRFID(tag, estadoEvt, desc);

        // 3) Insertar en acceso con el ID_Usuario correcto
        accesoDAO.insertarAcceso(
                LocalDateTime.now(),
                "RFID",
                idUsuario,   // null o el verdadero ID_Usuario
                null,
                idRegRfid
        );

        // 4) Refrescar tabla
        recargar();

        // 5) Breve alarma visual si es DENEGADO
        if (!permitido) {
            lblOcupados.setStyle("-fx-text-fill: red;");
            PauseTransition p = new PauseTransition(Duration.seconds(1));
            p.setOnFinished(e -> lblOcupados.setStyle(""));
            p.play();
        }
    }

    /** Carga los accesos de la fecha seleccionada */
    private void recargar() {
        var lista = accesoDAO.listarAccesosPorFecha(dpFecha.getValue());
        var obs = FXCollections.observableArrayList(lista);
        tblAccesos.setItems(obs);

        // Desplazar al final
        if (!obs.isEmpty()) {
            tblAccesos.scrollTo(obs.size() - 1);
        }

        actualizarContador(lista);
    }

    /** Filtra la tabla por texto en usuario o placa */
    private void filtrar() {
        String txt = tfFiltro.getText().toLowerCase().trim();
        if (txt.isEmpty()) {
            recargar();
        } else {
            var filtered = tblAccesos.getItems().stream()
                    .filter(a -> a.getUsuario().toLowerCase().contains(txt)
                            || a.getPlaca().toLowerCase().contains(txt))
                    .collect(Collectors.toList());
            tblAccesos.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    /** Actualiza el label de “Espacios ocupados” */
    private void actualizarContador(List<AccesoViewDTO> lista) {
        long dentro = lista.stream().filter(a -> "INGRESO".equals(a.getEstado())).count();
        lblOcupados.setText("Espacios ocupados: " + dentro + " de " + CAPACIDAD_TOTAL);
    }

    /** Cierra manualmente un acceso abierto */
    private void cerrarSalida(int idAcceso) {
        boolean ok = accesoDAO.cerrarSalida(idAcceso);
        if (!ok) {
            new Alert(Alert.AlertType.ERROR, "No se pudo cerrar la salida").showAndWait();
        }
        recargar();
    }

    @FXML
    private void refrescarPuerto() {
        // Primero, cierra el puerto si está abierto
        if (lector.isOpen()) {
            lector.cerrar();
        }

        // Intenta volver a abrir el puerto
        boolean conectado = lector.iniciar("COM4");

        if (conectado) {
            // Si se pudo abrir, actualizamos el círculo a verde
            statusCircle.setFill(Color.GREEN);
            new Alert(Alert.AlertType.INFORMATION, "Puerto COM4 refrescado y abierto correctamente").showAndWait();
        } else {
            // Si no se pudo abrir, actualizamos el círculo a rojo
            statusCircle.setFill(Color.RED);
            new Alert(Alert.AlertType.ERROR, "Error al intentar abrir el puerto COM4").showAndWait();
        }
    }

    /** Actualiza el estado de la conexión al puerto serial */
    public void actualizarStatus() {
        if (lector.isOpen()) {
            statusCircle.setFill(Color.GREEN);  // Conexión activa
        } else {
            statusCircle.setFill(Color.RED);    // Conexión inactiva
        }
    }

    /*
    private void procesarEvento(RaspberryPollingService.EventoPlaca ev) {
        String placa = ev.getPlaca();
        String gate  = ev.getGate();             // "entrada" o "salida"
        LocalDateTime ts = ev.getTimestamp();    // parseado de ev.getTime()

        if (gate.equalsIgnoreCase("entrada")) {
            // 1) Verificar que la placa exista y esté activa
            if (!PlacasDAO.existePlacaActivaYAsignada(placa)) {
                // alert de denegado…
            } else {
                // 2) Registrar ingreso
                accesoDAO.insertarAcceso(ts, "PLACA", null, placa, null);
                // alert de permitido…
            }
        }
        else if (gate.equalsIgnoreCase("salida")) {
            // 1) Buscar el acceso abierto para esa placa
            Integer idAcceso = accesoDAO.obtenerUltimoIngresoPorPlaca(placa);
            if (idAcceso != null) {
                // 2) Cerrar salida
                accesoDAO.cerrarSalida(idAcceso);
                // alert de salida ok…
            } else {
                // no había ingreso abierto → error o logging
            }
        }

    }*/

    @FXML
    private void onBack() {
        lector.cerrar();
        mainController.goBack();
    }
}
