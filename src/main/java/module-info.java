module com.example.mindtrack {

    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // JDBC
    requires java.sql;

    // MySQL connector
    requires mysql.connector.j;

    // Allow JavaFX to access controllers via reflection
    opens controllers to javafx.fxml;

    // Allow JavaFX to access FXML if needed
    opens main to javafx.fxml;

    // Optional but useful if you bind entities to UI later
    opens entities to javafx.base;

    // Export main package so Application can launch
    exports main;
}
