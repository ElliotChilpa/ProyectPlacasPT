/*package com.placaspt.logic;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;

public class RS232RFID {
    private SerialPort puerto;

    /**
     * Abre el puerto serie con parámetros 9600,8,N,1.
     * Antes, cierra cualquier otro handle abierto sobre ese mismo puerto.
     * /
    public boolean iniciar(String puertoNombre) {
        // 1) Cerrar cualquier instancia abierta de ese nombre
        for (SerialPort p : SerialPort.getCommPorts()) {
            if (p.getSystemPortName().equals(puertoNombre) && p.isOpen()) {
                p.closePort();
            }
        }
        // 2) Ahora sí obtenemos y abrimos nuestro SerialPort
        puerto = SerialPort.getCommPort(puertoNombre);
        puerto.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        puerto.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        return puerto.openPort();
    }

    public boolean isOpen() {
        return puerto != null && puerto.isOpen();
    }


    public void cerrar() {
        if (puerto != null && puerto.isOpen()) {
            puerto.closePort();
        }
    }

    public void escuchar(Callback callback) {
        if (puerto == null || !puerto.isOpen()) return;
        Thread hilo = new Thread(() -> {
            try (Scanner scanner = new Scanner(puerto.getInputStream())) {
                while (scanner.hasNextLine()) {
                    String linea = scanner.nextLine().trim();
                    if (!linea.isEmpty()) {
                        callback.nuevaLectura(linea);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, "RFID-Listener");
        hilo.setDaemon(true);
        hilo.start();
    }

    public interface Callback {
        void nuevaLectura(String datos);
    }
}
*/

// Este funcionaba
/*
package com.placaspt.logic;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;

public class RS232RFID {

    private static RS232RFID instance;
    private SerialPort puerto;

    // Constructor privado para evitar múltiples instancias
    private RS232RFID() {}

    // Método para obtener la única instancia de RS232RFID
    public static RS232RFID getInstance() {
        if (instance == null) {
            instance = new RS232RFID();
        }
        return instance;
    }

    public boolean iniciar(String puertoNombre) {
        puerto = SerialPort.getCommPort(puertoNombre);
        puerto.setComPortParameters(9600, 8, 1, 0);
        puerto.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);

        return puerto.openPort();
    }

    public void cerrar() {
        if (puerto != null && puerto.isOpen()) {
            puerto.closePort();
        }
    }

    public void escuchar(Callback callback) {
        if (puerto == null || !puerto.isOpen()) return;

        new Thread(() -> {
            try (Scanner scanner = new Scanner(puerto.getInputStream())) {
                while (scanner.hasNextLine()) {
                    String linea = scanner.nextLine();
                    callback.nuevaLectura(linea);
                }
            }
        }).start();
    }

    public interface Callback {
        void nuevaLectura(String datos);
    }
}
*/
package com.placaspt.logic;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;

public class RS232RFID {

    private static RS232RFID instance;
    private SerialPort puerto;

    private RS232RFID() {}

    public static RS232RFID getInstance() {
        if (instance == null) {
            instance = new RS232RFID();
        }
        return instance;
    }

    public boolean iniciar(String puertoNombre) {
        if (puerto != null && puerto.isOpen()) {
            // Si ya está abierto, no hacemos nada
            return true;
        }

        puerto = SerialPort.getCommPort(puertoNombre);
        puerto.setComPortParameters(9600, 8, 1, 0);
        puerto.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);

        return puerto.openPort();
    }

    public boolean isOpen() {
        return puerto != null && puerto.isOpen();
    }

    public void cerrar() {
        if (puerto != null && puerto.isOpen()) {
            puerto.closePort();
        }
    }

    public void escuchar(Callback callback) {
        if (puerto == null || !puerto.isOpen()) return;

        new Thread(() -> {
            try (Scanner scanner = new Scanner(puerto.getInputStream())) {
                while (scanner.hasNextLine()) {
                    String linea = scanner.nextLine();
                    callback.nuevaLectura(linea);
                }
            }
        }).start();
    }

    public interface Callback {
        void nuevaLectura(String datos);
    }
}
