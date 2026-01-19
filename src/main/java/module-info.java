module com.progetto.timebank {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports com.progetto.timebank;

    opens com.progetto.timebank.viewgui to javafx.fxml;

    opens com.progetto.timebank.bean to javafx.base;
    opens com.progetto.timebank to org.junit.platform.commons;
}
