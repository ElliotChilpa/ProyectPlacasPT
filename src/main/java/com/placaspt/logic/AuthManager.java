package com.placaspt.logic;

import com.placaspt.database.AdministradorDAO;

public class AuthManager {
    public static AdministradorPOJO login(String correo, String clave) {
        return AdministradorDAO.validarCredenciales(correo, clave);
    }
    /*public static boolean validate(String user, String pass) {
        return  user.equals("admin") && pass.equals("1234");
    }*/
}
