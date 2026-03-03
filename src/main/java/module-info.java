module org.mindtrack.mindtrackfxx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires okhttp3;
    requires com.google.gson;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires java.desktop;
    requires java.net.http;

    // OpenCV for facial emotion detection
    requires opencv;

    // Open packages to FXML reflection
    opens controllers to javafx.fxml;
    opens main to javafx.fxml;

    // Export packages
    exports main;
    exports controllers;
    exports utils;

    // Entity and service packages
    exports entities;
    exports services;
}