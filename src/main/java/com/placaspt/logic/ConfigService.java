package com.placaspt.logic;

import java.util.prefs.Preferences;

/**
 * Servicio singleton para leer/grabar parámetros de configuración
 * de la aplicación (puede usarse para COM, SSH, rutas, períodos…).
 */
public class ConfigService {
    private static final Preferences prefs =
            Preferences.userRoot().node("com/placaspt/config");

    private ConfigService() { }

    // Puerto serial RFID
    public static String getSerialPort() {
        return prefs.get("serial.port", "COM4");
    }
    public static void setSerialPort(String port) {
        prefs.put("serial.port", port);
    }

    // SSH: host, user, pass, archivo remoto
    public static String getSshHost() {
        return prefs.get("ssh.host", "192.168.0.11");
    }
    public static void setSshHost(String host) {
        prefs.put("ssh.host", host);
    }

    public static String getSshUser() {
        return prefs.get("ssh.user", "placasPT");
    }
    public static void setSshUser(String user) {
        prefs.put("ssh.user", user);
    }

    public static String getSshPassword() {
        return prefs.get("ssh.pass", "8586");
    }
    public static void setSshPassword(String pass) {
        prefs.put("ssh.pass", pass);
    }

    public static String getSshRemoteFile() {
        return prefs.get("ssh.file", "/home/placasPT/.../output.json");
    }
    public static void setSshRemoteFile(String path) {
        prefs.put("ssh.file", path);
    }

    // Período polling SSH en segundos
    public static int getSshPeriodSec() {
        return prefs.getInt("ssh.period", 5);
    }
    public static void setSshPeriodSec(int sec) {
        prefs.putInt("ssh.period", sec);
    }
}
