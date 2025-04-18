module org.example.gestionagricolejavafx {
    requires javafx.fxml;
    requires java.sql;
    requires de.jensd.fx.glyphs.fontawesome;
    requires javafx.controls;
    requires java.desktop;
    requires jdk.jsobject;
    requires javafx.web;

    opens GestionAgricole to javafx.fxml;
    opens GestionAgricole.entities to javafx.base;
    opens GestionAgricole.controllers to javafx.fxml;
    opens GestionAgricole.views to javafx.fxml;
    opens GestionAgricole.icons to javafx.fxml;
    opens GestionAgricole.views.Culture to javafx.fxml;
    opens GestionAgricole.views.Activite to javafx.fxml;

    // Add these specific exports and opens for Culture package
    exports GestionAgricole.controllers.Culture;
    opens GestionAgricole.controllers.Culture to javafx.fxml;
    exports GestionAgricole.controllers.Activite;
    opens GestionAgricole.controllers.Activite to javafx.fxml;

    exports GestionAgricole;
    exports GestionAgricole.controllers;
    exports GestionAgricole.controllers.Parcelle;
    opens GestionAgricole.controllers.Parcelle to javafx.fxml;
    exports GestionAgricole.controllers.Recolte;
    opens GestionAgricole.controllers.Recolte to javafx.fxml;
}