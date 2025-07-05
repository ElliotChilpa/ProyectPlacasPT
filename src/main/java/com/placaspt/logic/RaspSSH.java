package com.placaspt.logic;

import com.jcraft.jsch.*;

import java.io.InputStream;
import java.util.Scanner;

public class RaspSSH {

    /**
     * Lee de forma remota un archivo texto de la Raspberry vía SSH.
     *
     * @param host        IP o hostname de la Raspberry Pi
     * @param user        usuario SSH
     * @param pass        contraseña SSH
     * @param rutaArchivo ruta absoluta al archivo en la RPi
     * @return todo el contenido del archivo como String (líneas separadas por '\n')
     */
    public static String leerArchivoRasp(
            String host, String user, String pass, String rutaArchivo) {

        StringBuilder resultado = new StringBuilder();
        Session session = null;
        ChannelExec channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, 22);
            session.setPassword(pass);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(5000);

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("cat " + rutaArchivo);
            channel.setInputStream(null);

            InputStream in = channel.getInputStream();
            channel.connect(5000);

            try (Scanner sc = new Scanner(in)) {
                while (sc.hasNextLine()) {
                    resultado.append(sc.nextLine()).append("\n");
                }
            }

        } catch (JSchException | java.io.IOException e) {
            e.printStackTrace();
            // devolvemos mensaje de error para que el pollingService pueda detectarlo
            resultado.append("__SSH_ERROR__").append(e.getMessage());
        } finally {
            if (channel != null && channel.isConnected()) channel.disconnect();
            if (session != null && session.isConnected()) session.disconnect();
        }
        return resultado.toString();
    }
    /*
    public static String leerArchivoRasp(String host,
                                         String user,
                                         String pass,
                                         String rutaArchivo) {
        StringBuilder resultado = new StringBuilder();
        Session session = null;
        Channel channel = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, 22);
            session.setPassword(pass);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(3000);

            ChannelExec exec = (ChannelExec) session.openChannel("exec");
            exec.setCommand("cat " + rutaArchivo);
            exec.setInputStream(null);

            InputStream in = exec.getInputStream();
            exec.connect();

            try (Scanner scanner = new Scanner(in)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    resultado.append(line).append("\n");
                }
            }

            exec.disconnect();
            session.disconnect();
        } catch (Exception e) {
            // en caso de fallo devolvemos null o indicamos error en el String
            e.printStackTrace();
            return null;
        } finally {
            if (channel != null && channel.isConnected()) channel.disconnect();
            if (session != null && session.isConnected()) session.disconnect();
        }
        return resultado.toString();
    }*/

    /**
     * Ejecuta un comando remoto vía SSH, útil para renombrar/mover archivos en la RPi.
     *
     * @param host     IP o hostname de la Raspberry Pi
     * @param user     usuario SSH
     * @param pass     contraseña SSH
     * @param command  comando a ejecutar (por ejemplo "mv /ruta/a.json /ruta/a.json.leido")
     * @throws Exception si falla la conexión o la ejecución
     */
    public static void ejecutarComando(String host,
                                       String user,
                                       String pass,
                                       String command) throws Exception {
        Session session = null;
        ChannelExec channel = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, 22);
            session.setPassword(pass);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);

            InputStream in = channel.getInputStream();
            channel.connect();

            // (opcional) leer y descartar la salida
            byte[] buf = new byte[1024];
            while (in.available() > 0) {
                int len = in.read(buf, 0, buf.length);
                if (len < 0) break;
                // System.out.print(new String(buf, 0, len));
            }
        } finally {
            if (channel != null && channel.isConnected()) channel.disconnect();
            if (session != null && session.isConnected()) session.disconnect();
        }
    }
}
