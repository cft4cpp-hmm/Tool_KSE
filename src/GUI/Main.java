package GUI;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    // Add this line to VM Option of Run --> Edit configuration menu
    //--module-path "E:\VietData\GitLab\Bai10\lib\javafx-sdk-11.0.2\lib" --add-modules javafx.controls,javafx.fxml

    //--module-path "E:\VietData\GitLab\Bai10\lib\javafx-sdk-11.0.2\lib" --add-modules javafx.controls,javafx.fxml -p "E:\VietData\GitLab\Bai10\lib\javafx-sdk-11.0.2\lib" --add-modules javafx.controls
    public static void main(String[] args) {
        launch(args);


    }

    @Override
    public void start(Stage primaryStage) {
        int i =0;
        System.out.println("Hello world");

        // set title for the stage
        primaryStage.setTitle("creating buttons");

        // create a button
        Button b = new Button("button");
//
//        // create a stack pane
        StackPane r = new StackPane();
//
//        // add button
        r.getChildren().add(b);

        // create a scene
        Scene sc = new Scene(r, 200, 200);

        // set the scene
        primaryStage.setScene(sc);

        primaryStage.show();
    }
}
