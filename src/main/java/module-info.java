module com.example.demo6 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.demo6 to javafx.fxml;
    exports com.example.demo6;
    exports com.example.demo6.Model.Actions;
    opens com.example.demo6.Model.Actions to javafx.fxml;
    exports com.example.demo6.Model;
    opens com.example.demo6.Model to javafx.fxml;
}