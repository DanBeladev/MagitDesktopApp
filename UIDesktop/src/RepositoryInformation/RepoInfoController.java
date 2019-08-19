package RepositoryInformation;

import App.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
public class RepoInfoController {

    private Controller mainController;

    @FXML
    private Label reposNameLabel;

    @FXML
    private Label repoPathLabel;

    @FXML
    private Label userLabel;

    public void setMainController(Controller controller){
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
