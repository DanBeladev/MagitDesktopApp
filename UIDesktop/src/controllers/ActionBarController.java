package controllers;

import Lib.*;
import MagitExceptions.BranchNameIsAllreadyExistException;
import MagitExceptions.CommitException;
import MagitExceptions.RepositoryDoesnotExistException;
import MagitExceptions.RepositorySameToCurrentRepositoryException;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.ListViewBuilder;
import utils.GUIUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

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
            mainController.topInfoComponentController.getTaskProgressBar().setVisible(true);
            LoadXmlTask task = new LoadXmlTask(mainController.getRepositoryManager(), selectedFile,
                    () -> Platform.runLater(() -> {
                        try {
                            final RepositoryManager repoManager = mainController.getRepositoryManager();
                            final Repository currentRepo = repoManager.GetCurrentRepository();
                            if(repoManager.IsRepositoryExist(currentRepo.GetLocation())){
                                Optional<ButtonType> result = GUIUtils.popUpMessage(
                                        "An existing repository was found in path, would you like to override it?", Alert.AlertType.CONFIRMATION);
                                if (result.get() != ButtonType.OK) {
                                    return;
                                }
                            }

                            mainController.getIsIsRepoLoadedProperty().set(true);
                            mainController.repoNameProperty().set(currentRepo.getName());
                            mainController.repoPathProperty().set(currentRepo.GetLocation());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        finally {
                            mainController.topInfoComponentController.getTaskProgressBar().setVisible(false);
                        }
                    }),

                    (errors) -> Platform.runLater(() -> {
                        ListView<String> listView = ListViewBuilder.buildListView("Errors in Xml file", 200, 100);
                        errors.forEach(v -> listView.getItems().add(v));
                        BorderPane bp = (BorderPane) primaryStage.getScene().lookup("#root");
                        bp.setCenter(listView);
                        mainController.topInfoComponentController.getTaskProgressBar().setVisible(false);
                    }));



            mainController.topInfoComponentController.getTaskProgressBar().progressProperty().bind(task.progressProperty());
            new Thread(task).start();

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
                refresh();
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

    public void commitsTreeClick() {
        try {
            mainController.getRepositoryManager().IsRepositoryHasAtLeastOneCommit();
        } catch (CommitException e) {
            GUIUtils.popUpMessage("Repository without commits to draw", Alert.AlertType.INFORMATION);
            return;
        }
        mainController.createCommitsGraphForRepository();
        mainController.drawCommitsTree();
    }

    public void refresh(){
        try {
            mainController.getRepositoryManager().IsRepositoryHasAtLeastOneCommit();
        } catch (CommitException e) {
            return;
        }
        mainController.createCommitsGraphForRepository();
        mainController.drawCommitsTree();
    }

    private void bindNodeDisabledToBoolProperty(BooleanProperty booleanProperty, Node... nodes) {
        for (Node node : nodes) {
            node.disableProperty().bind(booleanProperty.not());
        }
    }

    public void MergeClick() {
        String branchToMerge = GUIUtils.getTextInput("merge", "enter branch to merge with it", "branch:", "");
        if (branchToMerge == null) {
            return;
        }
        try {
            List<MergeConfilct> conflicts = mainController.getRepositoryManager().MergeHeadBranchWithOtherBranch(mainController.getRepositoryManager().GetCurrentRepository().getBranchesMap().get(branchToMerge));
            for (MergeConfilct conflict : conflicts) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("../views/conflictsolver/ConflictSolver.fxml");
                fxmlLoader.setLocation(url);
                GridPane root = fxmlLoader.load(url.openStream());
                ConflictSolverController conflictSolverController = fxmlLoader.getController();
                conflictSolverController.setMergeConfilct(conflict);
                Stage secStage = new Stage();
                secStage.setScene(new Scene(root));
                conflictSolverController.setStage(secStage);
                conflictSolverController.setAncestorTextArea(conflict.getAncestorContent());
                conflictSolverController.setOursTextArea(conflict.getOurContent());
                conflictSolverController.setTheirsTextArea(conflict.getTheirsContent());
                secStage.showAndWait();
            }
        } catch (ParseException | IOException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void commitFilesInfoClick() {
        mainController.commitFilesInformation();
    }
}
