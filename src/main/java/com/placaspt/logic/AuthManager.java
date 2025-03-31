package com.placaspt.logic;

public class AuthManager {
    public static boolean validate(String user, String pass) {
        return  user.equals("admin") && pass.equals("1234");
    }
}
