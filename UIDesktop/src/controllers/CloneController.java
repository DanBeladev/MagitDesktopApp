package controllers;

import MagitExceptions.RepositoryDoesnotExistException;
import MagitExceptions.RepositorySameToCurrentRepositoryException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utils.GUIUtils;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;


public class CloneController {

    private AppController appController;

    private Stage secondaryStage;

    @FXML
    private TextField rrTextField;

    @FXML
    private TextField lrTextField;

    @FXML
    private TextField newRepoNameTextField;

    @FXML
    private Button cloneBtn;

    public void initialize() {
        cloneBtn.setDisable(true);
        newRepoNameTextField.textProperty().addListener(observable -> disableCloneBtnHelper());
        rrTextField.textProperty().addListener(observable -> disableCloneBtnHelper());
        lrTextField.textProperty().addListener(observable -> disableCloneBtnHelper());
    }

    public void cloneClick() {
        if (appController != null) {
            try {
                appController.getRepositoryManager().CloneRepository(lrTextField.getText()+"\\"+newRepoNameTextField.getText(), rrTextField.getText(),newRepoNameTextField.getText());
                appController.getIsIsRepoLoadedProperty().set(true);
                appController.repoPathProperty().set(lrTextField.getText()+ "\\" + newRepoNameTextField.getText());
                appController.repoNameProperty().set(appController.getRepositoryManager().GetCurrentRepository().getName());
                secondaryStage.close();
            } catch (RepositoryDoesnotExistException | IOException | RepositorySameToCurrentRepositoryException | ParseException e) {
                GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
            }

        }
    }

    public void closeClick(){
        secondaryStage.close();
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void addLRPathBtn(){
        File selectedFile = GUIUtils.getFolderByDirectoryChooser("choose folder",secondaryStage);
        if(selectedFile!=null){
            lrTextField.setText(selectedFile.getPath());
        }
    }

    public void addRRPathBtn(){
        File selectedFile = GUIUtils.getFolderByDirectoryChooser("choose folder", secondaryStage);
        if(selectedFile!=null){
            rrTextField.setText(selectedFile.getPath());
        }
    }

    public void setSecondaryStage(Stage secondaryStage) {
        this.secondaryStage = secondaryStage;
    }

    private void disableCloneBtnHelper(){
           if(lrTextField.getText().equals("") || rrTextField.getText().equals("") || newRepoNameTextField.getText().equals("")){
               cloneBtn.setDisable(true);
           }
           else{
               cloneBtn.setDisable(false);
           }
    }

}
