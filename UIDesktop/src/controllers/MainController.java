package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

//todo:: to notify in README that you have to click on the icons in tree view for let it work
public class MainController extends Application {

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader=new FXMLLoader();
        URL url=getClass().getResource("../views/app/App.fxml");
        fxmlLoader.setLocation(url);
        Parent root=fxmlLoader.load(url.openStream());
        AppController controller=fxmlLoader.getController();
        controller.setPrimaryStage(primaryStage);
        Scene scene=new Scene(root,1300,700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
