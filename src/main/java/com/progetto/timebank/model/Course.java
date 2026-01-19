package com.progetto.timebank.model;

public enum Course {
    ALGEBRA_E_LOGICA("Algebra e Logica"),
    ANALISI_MATEMATICA_I("Analisi Matematica I"),
    FISICA_GENERALE_I("Fisica Generale I"),
    FONDAMENTI_DI_INFORMATICA("Fondamenti di Informatica"),
    GEOMETRIA("Geometria"),
    PROBABILITA_E_STATISTICA("Probabilità e Statistica"),
    ANALISI_MATEMATICA_II("Analisi Matematica II"),
    CALCOLATORI_ELETTRONICI("Calcolatori Elettronici"),
    FISICA_GENERALE_II("Fisica Generale II"),
    FONDAMENTI_DI_CONTROLLI("Fondamenti di Controlli"),
    INGEGNERIA_DEGLI_ALGORITMI("Ingegneria degli Algoritmi"),
    SISTEMI_OPERATIVI("Sistemi Operativi"),
    BASI_DI_DATI("Basi di Dati"),
    INGEGNERIA_DEL_SOFTWARE("Ingegneria del Software"),
    RICERCA_OPERATIVA("Ricerca Operativa"),
    FONDAMENTI_DI_TELECOMUNICAZIONI("Fond. di Telecomunicazioni"),
    CAMPI_ELETTROMAGNETICI("Campi Elettromagnetici");

    private final String displayName;

    Course(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}