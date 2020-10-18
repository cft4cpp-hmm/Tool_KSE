package GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class VNUDSE extends Application
{
    private static Stage stage;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        Parent root = FXMLLoader.load(getClass().getResource("HybridTestGen.fxml"));

        //((HybridTestGen)root).setParentStage(primaryStage);

        setStage(primaryStage);

        primaryStage.setTitle("VNU-UET test data generation tool for C/C++");

        primaryStage.setScene(new Scene(root));

        primaryStage.show();

        primaryStage.setOnCloseRequest(t ->
        {
            Platform.exit();
            System.exit(0);
        });
    }



    public static Stage getStage()
    {
        return stage;
    }

    public static void setStage(Stage _stage)
    {
        stage = _stage;
    }
}
