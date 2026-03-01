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
    opens org.mindtrack.mindtrackfxx.controller to javafx.fxml;
    opens org.mindtrack.mindtrackfxx to javafx.fxml;

    // Export packages
    exports org.mindtrack.mindtrackfxx;
    exports org.mindtrack.mindtrackfxx.controller;
    exports org.mindtrack.mindtrackfxx.util;

    // Entity and service packages
    exports entities;
    exports services;
    exports utils;
}