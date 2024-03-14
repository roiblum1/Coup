module com.example.demo6 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.demo6 to javafx.fxml;
    exports com.example.demo6;
    exports com.example.demo6.Actions;
    opens com.example.demo6.Actions to javafx.fxml;
}