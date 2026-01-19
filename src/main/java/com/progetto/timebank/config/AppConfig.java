package com.progetto.timebank.config;

public class AppConfig {
    // CAMBIA QUESTO VALORE PER PASSARE DA DEMO-VERSION A FULL-VERSION
    private static boolean isDemo = false;

    // CAMBIA QUESTO VALORE PER DECIDERE SE USARE TRANSACTION VERSIONE DAO O FILESYSTEM (CSV)
    private static boolean isCSV = true;

    private AppConfig() {
        throw new IllegalStateException("Utility class");
    }

    public static void setIsDemo(boolean value) { isDemo = value; }
    public static boolean getIsDemo() { return isDemo; }

    public static void setIsCSV(boolean value) { isCSV = value; }
    public static boolean getIsCSV() { return isCSV; }
}
