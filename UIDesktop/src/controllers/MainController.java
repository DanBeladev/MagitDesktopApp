package controllers;

import Lib.RepositoryManager;
import Lib.SHA1;
import MagitExceptions.RepositoryDoesnotExistException;
import MagitExceptions.RepositorySameToCurrentRepositoryException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import puk.team.course.magit.ancestor.finder.AncestorFinder;
import resources.generated.MagitRepository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

//todo:: to notify in README that you have to click on the icons in tree view for let it work
//todo:: make commit tree to be home page
//todo:: repair resize of all components
//todo:: merge between two files with same name but different name - not must !
//todo:: update all fxml with current css
//todo:: adding button to "push branch to RR" bonus and check it

public class MainController extends Application {

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader=new FXMLLoader();
        URL url=getClass().getResource("/views/app/App.fxml");
        fxmlLoader.setLocation(url);
        Parent root=fxmlLoader.load(url.openStream());
        AppController controller=fxmlLoader.getController();
        controller.setPrimaryStage(primaryStage);
        Scene scene=new Scene(root,1300,650);
        primaryStage.setTitle("M.A.Git- Distributed version control");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
