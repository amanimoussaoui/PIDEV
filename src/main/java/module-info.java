module org.example.gestionagricolejavafx {
    requires javafx.fxml;
    requires java.sql;
    requires de.jensd.fx.glyphs.fontawesome;
    requires jdk.jsobject;
    requires javafx.web;
    requires com.google.gson;
    requires json.simple;
    requires org.apache.pdfbox;
    requires org.controlsfx.controls;
    requires java.desktop;


    opens Agriwise to javafx.fxml;
    opens Agriwise.entities to javafx.base;
    opens Agriwise.controllers to javafx.fxml;
    opens Agriwise.views to javafx.fxml;
    opens Agriwise.views.Culture to javafx.fxml;
    opens Agriwise.views.Activite to javafx.fxml;

    // Add these specific exports and opens for Culture package
    exports Agriwise.controllers.Culture;
    opens Agriwise.controllers.Culture to javafx.fxml;
    exports Agriwise.controllers.Activite;
    opens Agriwise.controllers.Activite to javafx.fxml;
    exports Agriwise.controllers.Utilisateur;
    opens Agriwise.controllers.Utilisateur to javafx.fxml;


    exports Agriwise;
    exports Agriwise.controllers;
    exports Agriwise.controllers.Parcelle;
    opens Agriwise.controllers.Parcelle to javafx.fxml;
    exports Agriwise.controllers.Recolte;
    opens Agriwise.controllers.Recolte to javafx.fxml;
}