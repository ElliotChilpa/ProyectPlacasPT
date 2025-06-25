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
