package com.placaspt.ui;

public interface MainAware {
    /**
     * Será llamado desde MainController.loadView(...)
     * para inyectar la referencia al MainController en el sub-controlador.
     */
    void setMainController(MainController main);
}
