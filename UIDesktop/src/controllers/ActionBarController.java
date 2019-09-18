package controllers;

import Lib.*;
import MagitExceptions.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.CssManeger;
import models.ListViewBuilder;
import models.PopUpWindowWithBtn;
import utils.GUIUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ActionBarController {

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

    @FXML
    private Button fetchBtn;

    @FXML
    private Button pullBtn;

    @FXML
    private Button pushBtn;

    @FXML
    private Button pushNewBranchBtn;

    private Stage primaryStage;
    private AppController mainController;
    private CssManeger cssStyles;


    @FXML
    public void initialize() {
        cssStyles=new CssManeger();
        cssStyles.addCssSheet("views/app/ActionBarStyle.css");
        cssStyles.addCssSheet("views/app/ActionBarStyle2.css");
        cssStyles.addCssSheet("views/app/ActionBarStyle3.css");
    }

    public void setMainController(AppController controller) {
        this.mainController = controller;
        bindNodeDisabledToBoolProperty(mainController.getIsIsRepoLoadedProperty(), commitBtn, branchesListBtn, commitsTreeBtn, commitContentBtn, deleteBranchBtn, newBranchBtn, mergeBtn, showStatusBtn, resetBranchbtn, checkoutBtn);
        bindNodeDisabledToBoolProperty(mainController.isIsClonedRepository() ,pushNewBranchBtn, fetchBtn, pullBtn, pushBtn);
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
            if (mainController.getRepositoryManager().GetCurrentRepository().getRRLocation() != null) {
                mainController.setIsClonedRepository(true);
            }
            else{
                mainController.setIsClonedRepository(false);
            }
            mainController.refresh();
        } catch
        (RepositorySameToCurrentRepositoryException | RepositoryDoesnotExistException | ParseException | IOException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;


    }

    private void displayErrors(List<String> errors) {
        Platform.runLater(() -> {
            ListView<String> listView = ListViewBuilder.buildListView("Errors in Xml file", 200, 100);
            errors.forEach(v -> listView.getItems().add(v));
            BorderPane bp = (BorderPane) primaryStage.getScene().lookup("#root");
            bp.setCenter(listView);
            mainController.topInfoComponentController.getTaskProgressBar().setVisible(false);
        });
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

                            if (repoManager.IsRepositoryExist(repoManager.GetMagitRepository().getLocation())) {
                                Optional<ButtonType> result = GUIUtils.popUpMessage(
                                        "An existing repository was found in path, would you like to override it?", Alert.AlertType.CONFIRMATION);
                                if (result.get() != ButtonType.OK) {
                                    return;
                                }
                            }
                            new Thread(() -> {
                                try {
                                    repoManager.LoadXML();
                                    Platform.runLater(() -> {
                                        final Repository currentRepo = repoManager.GetCurrentRepository();
                                        if (currentRepo == null) {
                                            return;
                                        }
                                        mainController.getIsIsRepoLoadedProperty().set(true);
                                        mainController.repoNameProperty().set(currentRepo.getName());
                                        mainController.repoPathProperty().set(currentRepo.GetLocation());
                                        if (currentRepo.getRRLocation() != null) {
                                            mainController.setIsClonedRepository(true);
                                        }
                                        else{
                                            mainController.setIsClonedRepository(false);
                                        }
                                    });
                                } catch (Exception e) {
                                    ArrayList<String> errors = new ArrayList<>();
                                    errors.add(e.getMessage());
                                    displayErrors(errors);
                                }
                            }).start();

                        } catch (Exception e) {
                            ArrayList<String> errors = new ArrayList<>();
                            errors.add(e.getMessage());
                            displayErrors(errors);
                        } finally {
                            mainController.topInfoComponentController.getTaskProgressBar().setVisible(false);
                        }
                    }),
                    this::displayErrors);
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
                    mainController.getRepositoryManager().BonusInit(result, selectedFile.getAbsolutePath() + "\\" + result, false);
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
                mainController.getRepositoryManager().MakeCommit(commitMsg, null);
                GUIUtils.popUpMessage("Commit added successfully", Alert.AlertType.CONFIRMATION);
                mainController.refresh();
            } catch (IOException | ParseException | RepositoryDoesnotExistException | CommitException e) {
                GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    public void showStatusClick() {
        try {
            List<List<String>> lst = mainController.getRepositoryManager().ShowStatus();
            mainController.showStatus(lst);
        } catch (ParseException | CommitException | IOException | RepositoryDoesnotExistException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }

    }

    public void createNewBranchClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/views/newbranch/NewBranch.fxml"));
            GridPane root = fxmlLoader.load();
            Scene scene = new Scene(root);
            Stage stageForCreate = new Stage();
            stageForCreate.setScene(scene);
            NewBranchController newBranchController = fxmlLoader.getController();
            newBranchController.setAppController(mainController);
            newBranchController.insertValuesToComboBox(mainController.getRepositoryManager().GetCurrentRepository().getCommitMap().keySet().stream().map(v -> v.getSh1()).collect(Collectors.toList()));
            newBranchController.setSecondaryStage(stageForCreate);
            stageForCreate.initModality(Modality.APPLICATION_MODAL);
            stageForCreate.showAndWait();
            mainController.refresh();
        } catch (IOException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void showBrancheListClick() {
        List<BranchDetails> branchDetailsList = null;
        try {
            branchDetailsList = mainController.getRepositoryManager().ShowBranches();
        } catch (RepositoryDoesnotExistException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
        mainController.showBranchesList(branchDetailsList);
    }

    public void deleteBranchClick() {
        List<BranchDetails> branchesList = null;
        try {
            branchesList = mainController.getRepositoryManager().ShowBranches();
        } catch (RepositoryDoesnotExistException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
        mainController.deleteBranch(branchesList);
    }

    public void checkOutBtnClick() {
        List<BranchDetails> branchesList = null;
        try {
            branchesList = mainController.getRepositoryManager().ShowBranches();
        } catch (RepositoryDoesnotExistException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
        mainController.checkOut(branchesList);
    }

    public void resetBranchClick() {
        List<SHA1> commitsList = null;
        try {
            commitsList = mainController.getRepositoryManager().getCurrentRepositoryAllCommitsSHA1();
        } catch (RepositoryDoesnotExistException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
        mainController.resetBranch(commitsList);
    }

    public void commitsTreeClick() {
        try {
            mainController.getRepositoryManager().IsRepositoryHasAtLeastOneCommit();
            mainController.drawCommitsTree();
        } catch (CommitException | RepositoryDoesnotExistException e) {
            GUIUtils.popUpMessage("Repository without commits to draw", Alert.AlertType.INFORMATION);
        }
    }

    private void bindNodeDisabledToBoolProperty(BooleanProperty booleanProperty, Node... nodes) {
        for (Node node : nodes) {
            node.disableProperty().bind(booleanProperty.not());
        }
    }

    public void MergeClick() {
        RepositoryManager repositoryManager = mainController.getRepositoryManager();
        String branchToMerge = GUIUtils.getTextInput("merge", "enter branch to merge with it", "branch:", "");
        if (branchToMerge == null) {
            return;
        }
        try {
            List<MergeConfilct> conflicts ;
            conflicts = repositoryManager.MergeHeadBranchWithOtherBranch(branchToMerge);
            mainController.handleConflicts(conflicts, branchToMerge);
        } catch (BranchDoesNotExistException | OpenChangesException | RepositoryDoesnotExistException | CommitException | ParseException | IOException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        } catch (FFException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.INFORMATION);
        }
    }

    public void cloneClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/views/clone/CloneRepositoryView.fxml"));
            GridPane gridPane = fxmlLoader.load();
            CloneController cloneController = fxmlLoader.getController();
            cloneController.setAppController(this.mainController);
            Stage stage = new Stage();
            cloneController.setSecondaryStage(stage);
            stage.setScene(new Scene(gridPane));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            mainController.setIsClonedRepository(true);
            GUIUtils.popUpMessage("Clone done successfully", Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }

    }

    public void fetchClick() {
        try {
            mainController.getRepositoryManager().FetchRRNewData();
            GUIUtils.popUpMessage("Fetch done successfully", Alert.AlertType.INFORMATION);
            mainController.refresh();
        } catch (RepositoryDoesnotExistException | RepositoryDoesntTrackAfterOtherRepositoryException | IOException | ParseException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void pullClick() {
        try {
            mainController.getRepositoryManager().Pull();
            GUIUtils.popUpMessage("Pull done successfully", Alert.AlertType.INFORMATION);
            mainController.refresh();
        } catch (BranchDoesNotExistException | RepositoryDoesnotExistException | RepositoryDoesntTrackAfterOtherRepositoryException | ParseException | CommitException | IOException | OpenChangesException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void pushClick() {
        try {
            mainController.getRepositoryManager().Push();
            GUIUtils.popUpMessage("Push done successfully", Alert.AlertType.INFORMATION);
        } catch (RepositoryDoesntTrackAfterOtherRepositoryException | ParseException | CommitException | IOException | RemoteTrackingBranchException | OpenChangesException | RepositoryDoesnotExistException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    public void commitFilesInfoClick() {
        mainController.commitFilesInformation();
    }

    public void changeStyleClick(){
        cssStyles.nextCss();
        BorderPane bp = (BorderPane) primaryStage.getScene().lookup("#root");
        bp.getStylesheets().clear();
        bp.getStylesheets().add(cssStyles.getCurrentCss());
    }

    public void pushNewBranchBtnClick(){
        Collection<Branch> branches=mainController.getRepositoryManager().GetCurrentRepository().getBranchesMap().values();
        branches=branches.stream().filter(v->(!(v instanceof RemoteTrackingBranch) && !(v instanceof RemoteBranch))).collect(Collectors.toList());
        ComboBox<String> comboBox=new ComboBox<>();
        comboBox.setPromptText("Choose local branch to push");
        comboBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        comboBox.getItems().setAll(branches.stream().map(Branch::getName).collect(Collectors.toList()));
        PopUpWindowWithBtn.popUpWindow(100,300,"Push",v->{
            try {
                mainController.getRepositoryManager().pushLocalBranchToRemoteBranch(mainController.getRepositoryManager().GetCurrentRepository().getBranchesMap().get(comboBox.getSelectionModel().getSelectedItem()));
                GUIUtils.popUpMessage("Pushing branch done successfully", Alert.AlertType.INFORMATION);
                mainController.refresh();
            } catch (IOException | BranchNameIsAllreadyExistException | CommitException | BranchFileDoesNotExistInFolderException | RepositoryDoesnotExistException | RepositoryDoesntTrackAfterOtherRepositoryException | ParseException | BranchDoesNotExistException | HeadBranchDeletedExcption e) {
                GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
            }
        },new Object(), new Label("Choose branch to push to RR"), comboBox);


    }
}
