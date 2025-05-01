package com.placaspt.logic;

import com.jcraft.jsch.*;

import java.io.InputStream;
import java.util.Scanner;

public class RaspSSH  {
    public static String leerArchivoRasp(String host, String user, String pass, String rutaArchivo) {
        StringBuilder resultado = new StringBuilder();

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            session.setPassword(pass);

            // Configurar sin preguntar por el fingerprint
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(3000);

            System.out.println("Conectado a la raspberry");
            System.out.println("Ejecutando: cat " + rutaArchivo);

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand("cat " + rutaArchivo);

            channel.setInputStream(null);
            InputStream input = channel.getInputStream();

            channel.connect();

            Scanner scanner = new Scanner(input);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println(">> " + line);
                resultado.append(line).append("\n");
            }
            System.out.println("Fin de lectura SSH");
            scanner.close();
            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
        resultado.append("Error:").append(e.getMessage());
        e.printStackTrace();
        }
        return resultado.toString();
    }
}
