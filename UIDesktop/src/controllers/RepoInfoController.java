package controllers;

import controllers.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
public class RepoInfoController {

    private AppController mainController;

    @FXML
    private Label reposNameLabel;

    @FXML
    private Label repoPathLabel;

    @FXML
    private Label userLabel;

    public void setMainController(AppController controller){
        this.mainController=controller;
    }


    public Label getRepoPathLabel() {
        return repoPathLabel;
    }

    public Label getReposNameLabel() {
        return reposNameLabel;
    }

    public Label getUserLabel() {
        return userLabel;
    }

}
