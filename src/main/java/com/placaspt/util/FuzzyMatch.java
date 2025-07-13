package com.placaspt.util;

import org.apache.commons.text.similarity.LevenshteinDistance;

public class FuzzyMatch {
    private static final LevenshteinDistance LD = new LevenshteinDistance();

    /**
     * Devuelve la distancia de edición entre dos cadenas.
     */
    public static int editDistance(String a, String b) {
        // LevenshteinDistance.apply() devuelve Integer ó null si supera maxDist (por defecto Integer.MAX)
        return LD.apply(a, b);
    }

    /**
     * Devuelve true si la distancia de edición raw es menor o igual que umbral.
     * Por ejemplo, umbral = 1 permite un solo carácter de diferencia.
     */
    public static boolean closeMatch(String entrada, String objetivo, int umbral) {
        return editDistance(entrada, objetivo) <= umbral;
    }
}
