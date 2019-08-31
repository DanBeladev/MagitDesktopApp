package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class RepoInfoController {

    private AppController mainController;

    @FXML
    private Label reposNameLabel;

    @FXML
    private Label repoPathLabel;

    @FXML
    private Label userLabel;

    @FXML
    private  ProgressBar taskProgressBar;

    public ProgressBar getTaskProgressBar() {
        return taskProgressBar;
    }

    public void setTaskProgressBar(ProgressBar taskProgressBar) {
        this.taskProgressBar = taskProgressBar;
    }



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
