module sample.yemek {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    opens sample.yemek.model to javafx.base;
    opens sample.yemek to javafx.fxml;
    exports sample.yemek.model;

    exports sample.yemek;
}