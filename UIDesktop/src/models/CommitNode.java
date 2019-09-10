package models;

import Consumers.ShowDeltaBiConsumer;
import Lib.Branch;
import Lib.Commit;
import Lib.RemoteBranch;
import Lib.SHA1;
import MagitExceptions.BranchNameIsAllreadyExistException;
import MagitExceptions.CommitException;
import MagitExceptions.RepositoryDoesnotExistException;
import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.IEdge;
import controllers.AppController;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import controllers.CommitNodeController;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import utils.GUIUtils;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommitNode extends AbstractCell {
    private String timestamp;
    private String committer;
    private String message;
    private String sha1;
    private List<Pair<String, Boolean>> pointedBranches;
    private CommitNodeController commitNodeController;
    private AppController appController;

    public CommitNode(String timestamp, String committer, String message, String sha1, AppController controller) {
        this.timestamp = timestamp;
        this.committer = committer;
        this.message = message;
        this.sha1 = sha1;
        this.appController = controller;
        this.pointedBranches = new ArrayList<>();

    }

    @Override
    public Region getGraphic(Graph graph) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/views/node/commitNode.fxml");
            fxmlLoader.setLocation(url);
            GridPane root = fxmlLoader.load(url.openStream());
            ContextMenuCommitNode contextMenuCommitNode = new ContextMenuCommitNode();
            chainShowDeltaToMenu(contextMenuCommitNode);
            chainShowCommitFiles(contextMenuCommitNode);
            chainResetHeadBranch(contextMenuCommitNode);
            chainCreateNewBranch(contextMenuCommitNode);
            chainMerge(contextMenuCommitNode);
            root.setOnMouseClicked((v) -> contextMenuCommitNode.getContextMenu().show(root, v.getScreenX(), v.getScreenY()));
            commitNodeController = fxmlLoader.getController();
            commitNodeController.setCommitMessage(message);
            commitNodeController.setCommitter(committer);
            commitNodeController.setCommitTimeStamp(timestamp);
            commitNodeController.setCommitSha1(sha1);
            commitNodeController.setBranchLabel(pointedBranches);

            return root;
        } catch (IOException e) {
            return new Label("Error when tried to create graphic node !");
        }
    }

    private void chainMerge(ContextMenuCommitNode contextMenuCommitNode) {
        MenuItem commitFilesItem = contextMenuCommitNode.createMenuItem("Merge", (v, w) -> {
            System.out.println("i merge");
        }, null, null);
        contextMenuCommitNode.mainMenuCreator(commitFilesItem);
    }


    private void chainResetHeadBranch(ContextMenuCommitNode contextMenuCommitNode) {
        MenuItem commitFilesItem = contextMenuCommitNode.createMenuItem("Reset head branch to here", (v, w) -> {
            try {
                appController.getRepositoryManager().ResetHeadBranch(new SHA1(sha1));
                GUIUtils.popUpMessage("Head branch was changed successfully", Alert.AlertType.INFORMATION);
            } catch (IOException | ParseException | CommitException e) {
                GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
            }
        }, null, null);

        contextMenuCommitNode.mainMenuCreator(commitFilesItem);

    }

    private void chainShowCommitFiles(ContextMenuCommitNode contextMenuCommitNode) {
        //MenuItem commitFilesItem = new MenuItem("Show all commit files");
        MenuItem commitFilesItem = contextMenuCommitNode.createMenuItem("Show all commit files", (v, w) -> {
            TreeView treeView = appController.buildTreeViewOfCommitFiles(appController.getRepositoryManager().GetCurrentRepository().getCommitFromMapCommit(new SHA1(sha1)));
            Stage stage = new Stage();
            stage.setScene(new Scene(treeView));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        }, null, null);
        contextMenuCommitNode.mainMenuCreator(commitFilesItem);

    }

    private void chainCreateNewBranch(ContextMenuCommitNode contextMenuCommitNode) {
        Menu newBranchMenu = new Menu("Create new branch here");
        Menu createRTBbranch = new Menu("Create Remote Tracking Branch");
        newBranchMenu.getItems().add(createRTBbranch);

        //////////////////////////////////////////////////////////////////////////////////
        /*create new rtb branch*/
        //////////////////////////////////////////////////////////////////////////////////
        List<Branch> RBonCommit =  appController.getRepositoryManager().GetCurrentRepository().getBranchesMap().values().stream().filter(v->v.getCommitSH1().getSh1().equals(sha1) && v instanceof RemoteBranch).collect(Collectors.toList());
        for(Branch RB : RBonCommit){
            List<String> remoteBranchesNamesList = new ArrayList<>();
            remoteBranchesNamesList.add(RB.getName());
            MenuItem RBMenuItem = new MenuItem(RB.getName());
            String[] rbSpliter = RB.getName().split("\\\\");
            String rtbName = rbSpliter[rbSpliter.length-1];
            RBMenuItem.setOnAction((e)-> {
                try {
                    appController.getRepositoryManager().CreateNewRemoteTrackingBranch(rtbName, (RemoteBranch)RB);
                    GUIUtils.popUpMessage("Remote Tracking Branch: " + rtbName + " added successfully", Alert.AlertType.INFORMATION);
                } catch (RepositoryDoesnotExistException | CommitException | BranchNameIsAllreadyExistException | IOException ex) {
                 GUIUtils.popUpMessage(ex.getMessage(), Alert.AlertType.ERROR);
                }
            });
            createRTBbranch.getItems().add(RBMenuItem);
        }
        //////////////////////////////////////////////////////////////////////////////////
        /*create new local branch*/
        //////////////////////////////////////////////////////////////////////////////////
        MenuItem createLocalBranch = contextMenuCommitNode.createMenuItem("Create new local branch here", (v, w) -> {
            String branchName = GUIUtils.getTextInput("Create new branch", "Please insert branch name", "Name: ", "");
            try {
                appController.getRepositoryManager().CreateNewBranch(branchName, new SHA1(sha1));
            } catch (BranchNameIsAllreadyExistException | IOException | CommitException | RepositoryDoesnotExistException e) {
                GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
            }

        }, null, null);
        newBranchMenu.getItems().add(createLocalBranch);
        contextMenuCommitNode.mainMenuCreator(newBranchMenu);
    }

    private void chainShowDeltaToMenu(ContextMenuCommitNode contextMenuCommitNode) {
        Menu differenceCommitMenu = new Menu("Show delta with prev commit");
        contextMenuCommitNode.mainMenuCreator(differenceCommitMenu);
        for (SHA1 sha : appController.getRepositoryManager().GetCurrentRepository().getCommitFromMapCommit(new SHA1(sha1)).getPrevCommits()) {
            List<SHA1> shaList = new ArrayList<>();
            shaList.add(sha);
            shaList.add(new SHA1(sha1));
            MenuItem subMenu = contextMenuCommitNode.createMenuItem(sha.getSh1(), new ShowDeltaBiConsumer(), appController, shaList);
            contextMenuCommitNode.addSubMenuItem(differenceCommitMenu, subMenu);
        }
    }


    @Override
    public DoubleBinding getXAnchor(Graph graph, IEdge edge) {
        final Region graphic = graph.getGraphic(this);
        return graphic.layoutXProperty().add(commitNodeController.getCircleRadius());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommitNode that = (CommitNode) o;

        return timestamp != null ? timestamp.equals(that.timestamp) : that.timestamp == null;
    }

    public String getCommitSha1() {
        return sha1;
    }

    @Override
    public int hashCode() {
        return timestamp != null ? timestamp.hashCode() : 0;
    }

    public void concatPointedBranches(String BranchToAdd, Boolean isActive) {
        pointedBranches.add(new Pair<>(BranchToAdd, isActive));
    }

    public String getTimestamp() {
        return timestamp;
    }


}
