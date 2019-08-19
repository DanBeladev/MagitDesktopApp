package ActionBar;


import App.Controller;
import Lib.User;
import MagitExceptions.CommitException;
import MagitExceptions.RepositoryDoesnotExistException;
import MagitExceptions.RepositorySameToCurrentRepositoryException;
import Utils.GUIUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.security.Guard;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

public class ActionBarController {
cvbd
    @FXML
    private Button switchRepoBtn;

    @FXML
    private Button initRepoBtn;

    @FXML
    private Button loadXMLBtn;

    @FXML
    private Button switchUserBtn;

    @FXML
    private Button commitBtn;

    @FXML
    private Button showStatusBtn;

    @FXML
    private Button commitContentBtn;

    @FXML
    private Button commitsTreeBtn;

    @FXML
    private Button newBranchBtn;

    @FXML
    private Button branchesListBtn;

    @FXML
    private Button deleteBranchBtn;

    @FXML
    private Button checkoutBtn;

    @FXML
    private Button resetBranchbtn;

    @FXML
    private Button mergeBtn;

    private Stage primaryStage;
    private Controller mainController;


    @FXML
    public void initialize(){
    }
    public void setMainController(Controller controller) {
        this.mainController = controller;
        commitBtn.disableProperty().bind(mainController.getIsIsRepoLoadedProperty().not());
    }


    public void switchRepoClick() {
        File selectedFile = GUIUtils.getFolderByDirectoryChooser("Select Repository file", primaryStage);
        if (selectedFile == null) {
            return;
        }
        try {
            String path = selectedFile.getAbsolutePath();
            mainController.setRepositoryManager(path);
            mainController.repoPathProperty().set(path);
            mainController.repoNameProperty().set(mainController.getRepositoryManager().GetCurrentRepository().getName());
            mainController.getIsIsRepoLoadedProperty().set(true);

        } catch
        (RepositorySameToCurrentRepositoryException | RepositoryDoesnotExistException | ParseException | IOException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;

    }

    public void loadXMLClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            System.out.println(selectedFile.getPath());
        }

    }


    public void initRepoClick() {
        String result = GUIUtils.getTextInput("Repository name", "Enter repository name:", "Name:", mainController.getRepositoryManager().GetUser().getName());
        if(result!=null) {
            File selectedFile = GUIUtils.getFolderByDirectoryChooser("choose folder", primaryStage);
            if (selectedFile != null) {
                try {
                    mainController.getRepositoryManager().BonusInit(result, selectedFile.getAbsolutePath() + "\\" + result);
                    mainController.repoPathProperty().set(selectedFile.getAbsolutePath() + "\\" + result);
                    mainController.repoNameProperty().set(mainController.getRepositoryManager().GetCurrentRepository().getName());
                } catch (IOException e) {
                    GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        }

    }

    public void switchUserClick() {
        String userName = GUIUtils.getTextInput("Change user", "Enter user name", "Name:", "");
        if(userName!=null) {
            User newUser = new User(userName);
            mainController.getRepositoryManager().ChangeUser(newUser);
            mainController.getUserNameProperty().set(newUser.getName());
        }
    }
    public void commitClick(){
        String commitMsg=GUIUtils.getTextInput("Commit","Enter commit message","Message:","");
        if(commitMsg!=null) {
            try {
                mainController.getRepositoryManager().MakeCommit(commitMsg);
                GUIUtils.popUpMessage("Commit added successfully", Alert.AlertType.CONFIRMATION);
            } catch (IOException | ParseException | RepositoryDoesnotExistException |CommitException  e ) {
                GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    public void showStatusClick(){
        try {
            List<List<String>> lst=mainController.getRepositoryManager().ShowStatus();
            mainController.showStatus(lst);
        } catch (IOException | ParseException | CommitException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
