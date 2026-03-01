module com.example.mindtrack {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.media;

    // JDK
    requires java.desktop;
    requires java.sql;
    requires java.net.http;
    requires jdk.httpserver;

    // Third-party automatic modules
    requires mysql.connector.j;
    requires com.fasterxml.jackson.databind;
    requires webcam.capture;
    requires org.apache.commons.codec;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires jbcrypt;
    requires jakarta.mail;
    requires jakarta.activation;


    opens controllers to javafx.fxml;
    opens main to javafx.fxml;
    opens entities to javafx.base;
    exports main;
    exports utils;
}