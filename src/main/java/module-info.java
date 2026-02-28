module com.example.mindtrack {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    // JDK
    requires java.desktop;
    requires java.sql;
    requires java.net.http;
    requires jdk.httpserver;

    // Third-party automatic modules
    requires mysql.connector.j;
    requires com.fasterxml.jackson.databind;
    requires webcam.capture;


    opens controllers to javafx.fxml;
    opens main to javafx.fxml;
    opens entities to javafx.base;

    exports main;
}
