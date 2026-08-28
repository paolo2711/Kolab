package com.kolab.common;

// distancia en línea recta entre dos puntos, fórmula del semiverseno
public final class Distancia {

    private static final double RADIO_TIERRA_KM = 6371.0;

    private Distancia() {
    }

    public static double entre(double latUno, double lonUno, double latDos, double lonDos) {
        double dLat = Math.toRadians(latDos - latUno);
        double dLon = Math.toRadians(lonDos - lonUno);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latUno)) * Math.cos(Math.toRadians(latDos))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return RADIO_TIERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
