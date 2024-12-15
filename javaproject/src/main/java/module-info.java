module com.example.javaproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    //requires org.kordamp.ikonli.core;
    //requires org.kordamp.ikonli.javafx;
    //requires org.kordamp.ikonli.fontawesome5;
    opens com.example.javaproject to javafx.graphics;
    exports com.example.javaproject;
}
