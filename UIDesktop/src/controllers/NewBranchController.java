package controllers;

import Lib.SHA1;
import MagitExceptions.BranchNameIsAllreadyExistException;
import MagitExceptions.CommitException;
import MagitExceptions.RepositoryDoesnotExistException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utils.GUIUtils;

import java.io.IOException;
import java.util.List;

public class NewBranchController {

    AppController appController;
    Stage secondaryStage;
    @FXML
    private TextField branchNameTxtField;

    @FXML
    private ComboBox<String> sha1ComboBox;

    public void onCreateClick(){
        try {
            if(!branchNameTxtField.getText().isEmpty()) {
                appController.getRepositoryManager().CreateNewBranch(branchNameTxtField.getText(), new SHA1(sha1ComboBox.getSelectionModel().getSelectedItem()));
                GUIUtils.popUpMessage(branchNameTxtField.getText() + " created successfully", Alert.AlertType.INFORMATION);
            }
            else {
                GUIUtils.popUpMessage("Function failed, you have to choose a name", Alert.AlertType.ERROR);
            }
        } catch (BranchNameIsAllreadyExistException | RepositoryDoesnotExistException | CommitException | IOException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
        finally {
            secondaryStage.close();
        }
    }

    public  void onCanceled(){
        secondaryStage.close();
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }
    public void setSecondaryStage(Stage stage) {
        this.secondaryStage = stage;
    }
    public void insertValuesToComboBox(List<String> list){
        for(String str: list){
            sha1ComboBox.getItems().add(str);
        }
    }
}
