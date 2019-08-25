package controllers;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import models.GridPaneBuilder;
import models.ListViewBuilder;
import models.PopUpWindowWithBtn;
import Lib.*;
import MagitExceptions.*;
import models.BranchDetailsView;
import utils.GUIUtils;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import com.fxgraph.graph.PannableCanvas;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.CommitTreeLayout;
import models.CommitNode;

import javax.swing.tree.TreeNode;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class AppController {
    final Image FOLDER_ICON=new Image(getClass().getResourceAsStream("/resources/folderIcon.png"));
    final Image TEXT_ICON=new Image(getClass().getResourceAsStream("/resources/text-file-icon-5.jpg"));


    @FXML
    ActionBarController leftBtnsComponentController;

    @FXML
    RepoInfoController topInfoComponentController;

    private Stage primaryStage;
    private SimpleStringProperty repoPath;
    private SimpleStringProperty userName;
    private SimpleStringProperty repoName;
    private SimpleBooleanProperty isRepoLoadedProperty;
    private RepositoryManager repositoryManager;
    private Graph commitTree;
    private Map<SHA1,ICell> nodesMap=new HashMap<>();

    public AppController(){
        repoPath=new SimpleStringProperty();
        userName=new SimpleStringProperty();
        repoName=new SimpleStringProperty();
        repositoryManager=new RepositoryManager();
        isRepoLoadedProperty=new SimpleBooleanProperty();
        commitTree=new Graph();

    }

    public SimpleStringProperty repoNameProperty() {
        return repoName;
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

        /*//todo::remove block
       //===============================
        String path = "C:\\try";
        try {
            this.setRepository(path);
        } catch (RepositorySameToCurrentRepositoryException e) {
            e.printStackTrace();
        } catch (RepositoryDoesnotExistException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        repoPathProperty().set(path);
        repoNameProperty().set(getRepositoryManager().GetCurrentRepository().getName());
        getIsIsRepoLoadedProperty().set(true);
        createCommitsGraphForRepository();

        //==========================*/
    }

    public void setPrimaryStage(Stage primaryStage){
        this.primaryStage=primaryStage;
        leftBtnsComponentController.setPrimaryStage(primaryStage);
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

    public void setRepository(String path) throws RepositorySameToCurrentRepositoryException, RepositoryDoesnotExistException, ParseException, IOException {
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
        GridPane gridPane= GridPaneBuilder.buildGridPane(4,3,50,50);
        gridPane.setPrefSize(30,30);
        int i=0;
        for(List<String> list:lst){
            ListView<String> listView;
            if(i==0){
                listView= ListViewBuilder.buildListView("Added:",100,100);
                gridPane.add(listView,2,0,1,4);
            }
            else if(i==1){
                listView= ListViewBuilder.buildListView("Changed:",100,100);
                gridPane.add(listView,0,0,1,4);
            }
            else{
                listView= ListViewBuilder.buildListView("Deleted:",100,100);
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
        accordion.setPrefWidth(300);
        accordion.setPrefHeight(200);
        for(BranchDetails branchDetails : branchDetailsList){
            String headBranchName=repositoryManager.GetCurrentRepository().getActiveBranch().getName();
            String title=headBranchName.equals(branchDetails.getName())? headBranchName+" <--Head":branchDetails.getName();
            accordion.getPanes().add(BranchDetailsView.render(title,branchDetails.getCommitSh1().getSh1(),branchDetails.getMessage()));
        }
        anchorPane.getChildren().add(accordion);
        borderPane.setCenter(anchorPane);
    }

    public void deleteBranch(List<BranchDetails> branchesList) {
        BorderPane borderPane=(BorderPane)primaryStage.getScene().lookup("#root");
        String[] branchesName = branchesList.stream().map(BranchDetails::getName).toArray(String[]::new);
        final ComboBox<String> comboBox=new ComboBox<>();
        comboBox.setPrefSize(200,20);
        comboBox.getSelectionModel().select("choose branch to delete:");
        comboBox.setItems(FXCollections.observableArrayList(branchesName));
        final Label commitSha1=new Label("SHA-1:");
        final Label commitMessage=new Label("Commit message:");
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            List<BranchDetails> filterdBranchList=branchesList.stream().filter((v)->v.getName().equals(comboBox.getSelectionModel().getSelectedItem())).collect(Collectors.toList());
            commitSha1.setText("SHA-1: "+ filterdBranchList.get(0).getCommitSh1());
            commitMessage.setText("Commit message: "+filterdBranchList.get(0).getMessage());
        });
        GridPane gridPane=GridPaneBuilder.buildGridPane(6,3,20,50);
        gridPane.add(comboBox,1,0,2,2);
        gridPane.add(commitSha1,1,1);
        gridPane.add(commitMessage,1,2);
        Button button=new Button("Delete");
        button.setOnAction((e)-> {
            try {
                repositoryManager.DeleteBranch(comboBox.getSelectionModel().getSelectedItem());
                GUIUtils.popUpMessage("branch deleted sucssesfully", Alert.AlertType.INFORMATION);
                this.deleteBranch(repositoryManager.ShowBranches());
            } catch (HeadBranchDeletedExcption | BranchDoesNotExistException | BranchFileDoesNotExistInFolderException ex) {
                GUIUtils.popUpMessage(ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
        gridPane.add(button,1,3);
        borderPane.setCenter(gridPane);
    }

    public void checkOut(List<BranchDetails> branchesList) {
        String[] branchesName = branchesList.stream().map(BranchDetails::getName).toArray(String[]::new);
        ComboBox<String> comboBox=new ComboBox<>();
        comboBox.setPrefSize(150,10);
        comboBox.setItems(FXCollections.observableArrayList(branchesName));
        comboBox.getSelectionModel().select(0);
        Label order=new Label("Choose branch to checkout");
        PopUpWindowWithBtn.popUpWindow(100,300,"Checkout",(v)-> {
            try {
                repositoryManager.CheckOut(comboBox.getSelectionModel().getSelectedItem());
                GUIUtils.popUpMessage("Successful checkout", Alert.AlertType.INFORMATION);
            } catch (BranchDoesNotExistException| IOException |ParseException | BranchIsAllReadyOnWCException e) {
                GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
            }
        },new Object(),order,comboBox);
    }

    public void resetBranch(List<SHA1> commitsSHA1) {
        String[] commitsArray = commitsSHA1.stream().map(SHA1::getSh1).toArray(String[]::new);
        ComboBox<String> comboBox=new ComboBox<>();
        comboBox.setPrefSize(150,10);
        comboBox.setItems(FXCollections.observableArrayList(commitsArray));
        comboBox.getSelectionModel().select(0);
        Label order=new Label("Choose commit SHA-1 for active branch");
        PopUpWindowWithBtn.popUpWindow(100,300,"reset",(v)->{
            try {
                repositoryManager.ResetHeadBranch(new SHA1(comboBox.getSelectionModel().getSelectedItem()));
                GUIUtils.popUpMessage("Successful reset", Alert.AlertType.INFORMATION);
            } catch (CommitException | ParseException | IOException e) {
                GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
            }
        },new Object(),order,comboBox);
    }


    public void createCommitsGraphForRepository(){
        commitTree = new Graph();
        final Model model=commitTree.getModel();
        commitTree.beginUpdate();

        List<SHA1> commitsSha1=repositoryManager.getCurrentRepositoryAllCommitsSHA1();
        for(SHA1 commitSha1: commitsSha1){
            Commit commit=repositoryManager.getCommitFromCurrentRepositoryMapCommit(commitSha1);
            ICell cell=new CommitNode(commit.getCreateTime().toString(),commit.getWhoUpdated().getName(),commit.getMessage(),commitSha1.getSh1());
            model.addCell(cell);
            nodesMap.put(commitSha1,cell);
        }
        for(SHA1 commitSha1: commitsSha1){
            Commit commit=repositoryManager.getCommitFromCurrentRepositoryMapCommit(commitSha1);
            List<SHA1> prevCommits=commit.getPrevCommits();
            for(SHA1 sha1:prevCommits){
                final Edge edge=new Edge(nodesMap.get(commitSha1),nodesMap.get(sha1));
                model.addEdge(edge);
            }
        }
        commitTree.endUpdate();
        commitTree.layout(new CommitTreeLayout());

    }

    public void drawCommitsTree() {
        PannableCanvas canvas = commitTree.getCanvas();
        ScrollPane scrollPane=new ScrollPane();
        scrollPane.setPrefHeight(100);
        scrollPane.setPrefWidth(100);
        scrollPane.setContent(canvas);
        BorderPane borderPane=(BorderPane)primaryStage.getScene().lookup("#root");
        borderPane.setCenter(scrollPane);
        commitTree.getUseViewportGestures().set(false);
        commitTree.getUseNodeGestures().set(false);
    }

    //todo:: show leaf content of tree view
    public void commitFilesInformation(){
        BorderPane borderPane=(BorderPane)primaryStage.getScene().lookup("#root");
        String [] commitsArray=repositoryManager.getCurrentRepositoryAllCommitsSHA1().stream().map(SHA1::getSh1).toArray(String[]::new);
        ComboBox<String> comboBox=new ComboBox<>();
        comboBox.setPrefSize(300,10);
        comboBox.setItems(FXCollections.observableArrayList(commitsArray));
        comboBox.getSelectionModel().select(0);
        GridPane gridPane=GridPaneBuilder.buildGridPane(6,3,20,50);
        gridPane.add(comboBox,1,0,2,2);
        borderPane.setCenter(gridPane);
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            TreeView<String> tree=buildTreeViewOfCommitFiles(repositoryManager.getCommitFromCurrentRepositoryMapCommit(new SHA1(newValue)));
            renderTreeView(tree,comboBox,borderPane);
        });
    }

    private void renderTreeView(TreeView<String> treeView, ComboBox<String> comboBox,BorderPane borderPane){
        treeView.setPrefHeight(300);
        treeView.setMaxHeight(400);
        GridPane gridPane=GridPaneBuilder.buildGridPane(6,3,20,50);
        gridPane.add(comboBox,1,0,2,2);
        gridPane.add(treeView,1,2,1,3);

        borderPane.setCenter(gridPane);
    }

    public TreeView<String> buildTreeViewOfCommitFiles(Commit commit){
        TreeView<String> treeView = new TreeView<>();
        Folder mainFolder = getRepositoryManager().GetCurrentRepository().getFoldersMap().get(commit.getMainFolderSH1());
        String nameMainFolder = repositoryManager.getMainFolderName();
        TreeItem<String> root = new TreeItem<>(nameMainFolder);
        buildTreeViewOfCommitFilesRec(mainFolder,root);
        treeView.setRoot(root);
        EventHandler<MouseEvent> mouseEventHandle = (MouseEvent event) -> {
            Node node=event.getPickResult().getIntersectedNode();
            if(node instanceof TreeCell) {
                if (event.getClickCount() == 2 && ((TreeItem)treeView.getSelectionModel().getSelectedItem()).isLeaf()) {
                    System.out.println("shalom");
                }
            }

        };
        treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEventHandle);
        return treeView;
    }

    //todo:: fix icons
    public void buildTreeViewOfCommitFilesRec(Folder folder, TreeItem<String> treeItem){
        ImageView imageView=new ImageView();
        imageView.setFitHeight(20);
        imageView.setFitWidth(25);
        imageView.setImage(FOLDER_ICON);
        treeItem.setGraphic(imageView);
        for(FileDetails fd: folder.getInnerFiles()){
            if(fd.getFileType()==FileType.FOLDER){
                TreeItem<String> subTreeItem=new TreeItem<>(fd.getName());
                treeItem.getChildren().add(subTreeItem);
                buildTreeViewOfCommitFilesRec(getRepositoryManager().GetCurrentRepository().getFoldersMap().get(fd.getSh1()),subTreeItem);
            }
            else{
                ImageView imageView2=new ImageView();
                imageView2.setFitHeight(25);
                imageView2.setFitWidth(20);
                imageView2.setImage(TEXT_ICON);
                TreeItem<String> subTreeItem=new TreeItem<>(fd.getName());
                treeItem.getChildren().add(subTreeItem);
                subTreeItem.setGraphic(imageView2);
            }
        }
    }

    public void updateGraph() {
        Commit commitToAdd = repositoryManager.getCommitFromCurrentRepositoryMapCommit(repositoryManager.GetCurrentRepository().getActiveBranch().getCommitSH1());
        ICell newCell = new CommitNode(commitToAdd.getCreateTime().toString(),commitToAdd.getWhoUpdated().getName(),commitToAdd.getMessage(),commitToAdd.MakeSH1().getSh1());
        nodesMap.put(commitToAdd.MakeSH1(),newCell);
        //commitTree.beginUpdate();
        Model model = commitTree.getModel();
        model.addCell(newCell);
        for(SHA1 parentSh1 : commitToAdd.getPrevCommits()){
            model.addEdge(newCell,nodesMap.get(parentSh1));
        }
        model.endUpdate();
        commitTree.layout(new CommitTreeLayout());
    }
}
