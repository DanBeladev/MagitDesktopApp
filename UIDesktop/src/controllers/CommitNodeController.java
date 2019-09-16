package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;

import java.awt.*;
import java.util.List;

public class CommitNodeController {

    @FXML private Label commitTimeStampLabel;
    @FXML private Label messageLabel;
    @FXML private Label committerLabel;
    @FXML private Circle CommitCircle;
    @FXML private Label sha1Label;
    @FXML private TextFlow branches;

    public void setCommitTimeStamp(String timeStamp) {
        commitTimeStampLabel.setText(timeStamp);
        commitTimeStampLabel.setTooltip(new Tooltip(timeStamp));
    }

    public void setCommitter(String committerName) {
        committerLabel.setText(committerName);
        committerLabel.setTooltip(new Tooltip(committerName));
    }

    public void setCommitMessage(String commitMessage) {
        messageLabel.setText(commitMessage);
        messageLabel.setTooltip(new Tooltip(commitMessage));
    }
    public void setCommitSha1(String commitSha1){
        sha1Label.setText(commitSha1);
        sha1Label.setTooltip(new Tooltip(commitSha1));
    }
    public void setBranchLabel(List<Pair<String, Boolean>> pointBranches){
        for (Pair<String, Boolean> pair: pointBranches) {
            Text text = new Text(pair.getKey());
            if (pair.getValue()){
                text.setStyle("-fx-font-weight: bold");
            }
            branches.getChildren().add(text);
            branches.getChildren().add(new Text(" & "));
        }
        branches.getChildren().remove(branches.getChildren().size() -1 );
    }

    public int getCircleRadius() {
        return (int)CommitCircle.getRadius();
    }

    public void setRadius(int i) {
        CommitCircle.setRadius(i);
    }
    public void setColor(Color color) {
        CommitCircle.setFill(color);
    }
}
