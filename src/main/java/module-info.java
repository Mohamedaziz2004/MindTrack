module com.exemple.mindtrack {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;  // Ajouter cette ligne
    requires java.sql;
    requires java.net.http;
    requires org.json;

    // Open packages to JavaFX
    opens application to javafx.graphics;
    opens controllers to javafx.fxml;
    opens entities to javafx.base;

    // Open the fxml folder to allow resource loading
    opens fxml to javafx.fxml;

    // Export all packages
    exports application;
    exports controllers;
    exports entities;
    exports services;
    exports utils;
    exports dao;
}