package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // นี่คือจุดเริ่มต้นที่ JavaFX จะเรียกใช้เมื่อเปิดโปรแกรม
        primaryStage.setTitle("Battle Cat - Alpha Version");

        StackPane root = new StackPane();
        // ในอนาคตคุณจะเอา GameManager หรือ UI มาใส่ที่นี่

        Scene scene = new Scene(root, 1280, 720);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // คำสั่ง launch จะไปเรียก start() ด้านบน
        launch(args);
    }
}
