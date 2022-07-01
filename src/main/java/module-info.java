module com.ryanaong.tabletoppresenter {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;

    opens com.ryanaong.tabletoppresenter to javafx.fxml;
    exports com.ryanaong.tabletoppresenter;
}