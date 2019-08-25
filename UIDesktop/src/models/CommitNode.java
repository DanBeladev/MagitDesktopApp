package models;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.IEdge;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import controllers.CommitNodeController;

import java.io.IOException;
import java.net.URL;

public class CommitNode extends AbstractCell {

    private String timestamp;
    private String committer;
    private String message;
    private String sha1;
    private String pointedBranches="";
    private CommitNodeController commitNodeController;

    public CommitNode(String timestamp, String committer, String message, String sha1) {
        this.timestamp = timestamp;
        this.committer = committer;
        this.message = message;
        this.sha1=sha1;
    }

    @Override
    public Region getGraphic(Graph graph) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("../views/node/commitNode.fxml");
            fxmlLoader.setLocation(url);
            GridPane root = fxmlLoader.load(url.openStream());
            root.setOnMouseClicked((v)-> System.out.println(message));
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
    public String getCommitSha1(){
        return sha1;
    }

    @Override
    public int hashCode() {
        return timestamp != null ? timestamp.hashCode() : 0;
    }

    public String getPointedBranches() {
        return pointedBranches;
    }

    public void concatPointedBranches(String BranchToAdd) {
        if(pointedBranches.equals("")){
            pointedBranches = pointedBranches.concat(BranchToAdd);
        }
        else{
            pointedBranches = pointedBranches.concat(","+BranchToAdd);
        }
    }
}
