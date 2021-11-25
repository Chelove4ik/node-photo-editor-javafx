module com.example.photoeditorjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;


    opens com.example.photoeditorjavafx to javafx.fxml;
    exports com.example.photoeditorjavafx;
}