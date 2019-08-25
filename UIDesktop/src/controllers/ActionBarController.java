package controllers;


import models.ListViewBuilder;
import Lib.BranchDetails;
import Lib.SHA1;
import Lib.User;
import MagitExceptions.*;
import utils.GUIUtils;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.List;

public class ActionBarController {

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
    private AppController mainController;


    @FXML
    public void initialize() {
    }

    public void setMainController(AppController controller) {
        this.mainController = controller;
        bindNodeDisabledToBoolProperty(mainController.getIsIsRepoLoadedProperty(), commitBtn, branchesListBtn, commitsTreeBtn, commitContentBtn, deleteBranchBtn, newBranchBtn, mergeBtn, showStatusBtn, resetBranchbtn, checkoutBtn);
    }


    public void switchRepoClick() {
        File selectedFile = GUIUtils.getFolderByDirectoryChooser("Select Repository file", primaryStage);
        if (selectedFile == null) {
            return;
        }
        try {
            String path = selectedFile.getAbsolutePath();
            mainController.setRepository(path);
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
            try {
                List<String> errors = mainController.getRepositoryManager().CheckXml(selectedFile.getAbsolutePath());
                if (errors.isEmpty()) {
                    mainController.getRepositoryManager().LoadXML();
                    mainController.getIsIsRepoLoadedProperty().set(true);
                    mainController.repoNameProperty().set(mainController.getRepositoryManager().GetCurrentRepository().getName());
                    mainController.repoPathProperty().set(mainController.getRepositoryManager().GetCurrentRepository().GetLocation());
                }else {
                    ListView<String> listView=ListViewBuilder.buildListView("Errors in Xml file",200,100);
                    errors.forEach(v->listView.getItems().add(v));
                    BorderPane bp=(BorderPane)primaryStage.getScene().lookup("#root");
                    bp.setCenter(listView);
                }

            } catch (BranchDoesNotExistException | RepositoryAllreadyExistException | BranchIsAllReadyOnWCException | JAXBException | IllegalAccessException | XMLException | NoSuchMethodException e) {
                GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
            } catch (ParseException | InvocationTargetException | IOException e) {
                e.printStackTrace();
            }
        }

    }


    public void initRepoClick() {
        String result = GUIUtils.getTextInput("Repository name", "Enter repository name:", "Name:", mainController.getRepositoryManager().GetUser().getName());
        if (result != null) {
            File selectedFile = GUIUtils.getFolderByDirectoryChooser("choose folder", primaryStage);
            if (selectedFile != null) {
                try {
                    mainController.getRepositoryManager().BonusInit(result, selectedFile.getAbsolutePath() + "\\" + result);
                    mainController.repoPathProperty().set(selectedFile.getAbsolutePath() + "\\" + result);
                    mainController.repoNameProperty().set(mainController.getRepositoryManager().GetCurrentRepository().getName());
                    mainController.getIsIsRepoLoadedProperty().set(true);
                } catch (IOException e) {
                    GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        }

    }

    public void switchUserClick() {
        String userName = GUIUtils.getTextInput("Change user", "Enter user name", "Name:", "");
        if (userName != null) {
            User newUser = new User(userName);
            mainController.getRepositoryManager().ChangeUser(newUser);
            mainController.getUserNameProperty().set(newUser.getName());
        }
    }

    public void commitClick() {
        String commitMsg = GUIUtils.getTextInput("Commit", "Enter commit message", "Message:", "");
        if (commitMsg != null) {
            try {
                mainController.getRepositoryManager().MakeCommit(commitMsg);
                GUIUtils.popUpMessage("Commit added successfully", Alert.AlertType.CONFIRMATION);
            } catch (IOException | ParseException | RepositoryDoesnotExistException | CommitException e) {
                GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    public void showStatusClick() {
        try {
            List<List<String>> lst = mainController.getRepositoryManager().ShowStatus();
            mainController.showStatus(lst);
        } catch (IOException | ParseException | CommitException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void createNewBranchClick() {
        try {
            String name = GUIUtils.getTextInput("Create new branch", "Enter branch name:", "Name", "");
            if (name != null) {
                mainController.getRepositoryManager().CreateNewBranch(name);
                GUIUtils.popUpMessage(name + " added successfully", Alert.AlertType.INFORMATION);
            }
        } catch (RepositoryDoesnotExistException | CommitException | IOException | BranchNameIsAllreadyExistException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void showBrancheListClick() {
        List<BranchDetails> branchDetailsList = mainController.getRepositoryManager().ShowBranches();
        mainController.showBranchesList(branchDetailsList);
    }

    public void deleteBranchClick() {
        List<BranchDetails> branchesList = mainController.getRepositoryManager().ShowBranches();
        mainController.deleteBranch(branchesList);
    }

    public void checkOutBtnClick() {
        List<BranchDetails> branchesList = mainController.getRepositoryManager().ShowBranches();
        mainController.checkOut(branchesList);
    }

    public void resetBranchClick() {
        List<SHA1> commitsList = mainController.getRepositoryManager().getCurrentRepositoryAllCommitsSHA1();
        mainController.resetBranch(commitsList);
    }
    public void commitsTreeClick(){
        try {
            mainController.getRepositoryManager().IsRepositoryHasAtLeastOneCommit();
        } catch (CommitException e) {
            GUIUtils.popUpMessage("Repository without commits to draw", Alert.AlertType.INFORMATION);
            return;
        }
        mainController.createCommitsGraphForRepository();
        mainController.drawCommitsTree();
    }

    public void bindNodeDisabledToBoolProperty(BooleanProperty booleanProperty, Node... nodes) {
        for (Node node : nodes) {
            node.disableProperty().bind(booleanProperty.not());
        }
    }

    public void  commitFilesInfoClick(){
        mainController.commitFilesInformation();
    }

}
