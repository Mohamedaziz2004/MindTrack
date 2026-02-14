module org.mindtrack.mindtrackfxx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires okhttp3;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;

    // Open controller packages to FXML reflection
    opens org.mindtrack.mindtrackfxx.controller to javafx.fxml;
    // Keep existing opens for any legacy controllers/resources
    opens org.mindtrack.mindtrackfxx to javafx.fxml;

    // Export root and model/service APIs
    exports org.mindtrack.mindtrackfxx;
    exports org.mindtrack.mindtrackfxx.model;
    exports org.mindtrack.mindtrackfxx.service;

    // Newly added packages
    exports entities;
    exports servives;
    exports main;
    exports utils;
}