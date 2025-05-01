package com.placaspt.logic;

import com.fazecast.jSerialComm.SerialPort;

import java.io.InputStream;
import java.util.Scanner;

public class RS232RFID {
    private SerialPort puerto;

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
