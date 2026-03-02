module MndTrack {

    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires java.net.http;

    requires com.fasterxml.jackson.databind;
    requires org.apache.pdfbox;

    requires mysql.connector.j;   // ✅ CORRECT

    opens controllers to javafx.fxml;
    opens entities to javafx.base;

    exports main;
    exports controllers;
    exports entities;
    exports services;
    exports utils;
}