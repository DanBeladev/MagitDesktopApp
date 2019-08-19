package App;

import ActionBar.ActionBarController;
import Lib.BranchDetails;
import Lib.RepositoryManager;
import MagitExceptions.RepositoryDoesnotExistException;
import MagitExceptions.RepositorySameToCurrentRepositoryException;
import RepositoryInformation.RepoInfoController;
import DumbComponents.BranchDetailsView;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class Controller {

    @FXML
    ActionBarController leftBtnsComponentController;

    @FXML
    RepoInfoController topInfoComponentController;

    private Stage primaryStage;

    private SimpleStringProperty repoPath;

    private SimpleStringProperty userName;
    public SimpleStringProperty repoNameProperty() {
        return repoName;
    }

    private SimpleStringProperty repoName;

    private SimpleBooleanProperty isRepoLoadedProperty;

    private RepositoryManager repositoryManager;


    public Controller(){
        repoPath=new SimpleStringProperty();
        userName=new SimpleStringProperty();
        repoName=new SimpleStringProperty();
        repositoryManager=new RepositoryManager();
        isRepoLoadedProperty=new SimpleBooleanProperty();
    }

    @FXML
    public void initialize(){
        if(leftBtnsComponentController!=null && topInfoComponentController!=null){
            leftBtnsComponentController.setMainController(this);
            leftBtnsComponentController.setPrimaryStage(primaryStage);
            topInfoComponentController.setMainController(this);
        }
        topInfoComponentController.getRepoPathLabel().textProperty().bind(repoPath);
        topInfoComponentController.getUserLabel().textProperty().bind(userName);
        topInfoComponentController.getReposNameLabel().textProperty().bind(repoName);
        userName.set(repositoryManager.GetUser().getName());
        isRepoLoadedProperty.set(false);
    }
    public void setPrimaryStage(Stage primaryStage){
        this.primaryStage=primaryStage;
    }

    public String getRepoPath() {
        return repoPath.get();
    }

    public SimpleStringProperty repoPathProperty() {
        return repoPath;
    }
    public String getRepoName() {
        return repoName.get();
    }

    public void setRepositoryManager(String path) throws RepositorySameToCurrentRepositoryException, RepositoryDoesnotExistException, ParseException, IOException {
        this.repositoryManager.ChangeRepository(path);
    }

    public RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    public String getUserName() {
        return userName.get();
    }

    public SimpleStringProperty getUserNameProperty() {
        return userName;
    }

    public SimpleBooleanProperty getIsIsRepoLoadedProperty() {
        return isRepoLoadedProperty;
    }

    public void showStatus(List<List<String>> lst) throws IOException {
        BorderPane borderPane=(BorderPane)primaryStage.getScene().lookup("#root");
        GridPane gridPane=new GridPane();
        for(int i =0;i<4;i++) {
            if (i<3) {
                ColumnConstraints columnConstraints = new ColumnConstraints();
                columnConstraints.setMinWidth(5);
                columnConstraints.setPrefWidth(50);
                columnConstraints.setHgrow(Priority.SOMETIMES);
                gridPane.getColumnConstraints().add(columnConstraints);
            }
            RowConstraints rowConstraints=new RowConstraints();
            rowConstraints.setMinHeight(5);
            rowConstraints.setPrefHeight(50);
            rowConstraints.setVgrow(Priority.SOMETIMES);
            gridPane.getRowConstraints().add(rowConstraints);
        }
        gridPane.setPrefSize(30,30);
        int i=0;
        for(List<String> list:lst){
            ListView<String> listView=new ListView<>();
            if(i==0){
                listView.getItems().add("Added:");
                listView.setPrefHeight(100);
                listView.setPrefWidth(100);
                gridPane.add(listView,2,0,1,4);
            }
            if(i==1){
                listView.getItems().add("Changed:");
                listView.setPrefHeight(100);
                listView.setPrefWidth(100);
                gridPane.add(listView,0,0,1,4);
            }
            if(i==2){
                listView.getItems().add("Deleted:");
                listView.setPrefHeight(100);
                listView.setPrefWidth(100);
                gridPane.add(listView,1,0,1,4);
            }
            i++;
            for(String str:list){
                listView.getItems().add(str);
            }
        }
        borderPane.setCenter(gridPane);
    }

    public void showBranchesList(List<BranchDetails> branchDetailsList) {
        BorderPane borderPane=(BorderPane)primaryStage.getScene().lookup("#root");
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPrefHeight(200);
        anchorPane.setPrefWidth(200);
        Accordion accordion = new Accordion();
        accordion.setPrefHeight(167);
        accordion.setPrefHeight(444);
        for(BranchDetails branchDetails : branchDetailsList){
            accordion.getPanes().add(BranchDetailsView.render(branchDetails.getName(),branchDetails.getCommitSh1().getSh1(),branchDetails.getMessage()));
        }
        anchorPane.getChildren().add(accordion);
        borderPane.setCenter(anchorPane);
    }
}
