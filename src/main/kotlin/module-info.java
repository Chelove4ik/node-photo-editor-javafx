module com.example.photoeditorjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires java.desktop;
    requires javafx.swing;
    requires opencv;


    opens com.example.photoeditorjavafx to javafx.fxml;
    exports com.example.photoeditorjavafx;
}