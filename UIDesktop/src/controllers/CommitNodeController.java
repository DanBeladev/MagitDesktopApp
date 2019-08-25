package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;

public class CommitNodeController {

    @FXML private Label commitTimeStampLabel;
    @FXML private Label messageLabel;
    @FXML private Label committerLabel;
    @FXML private Circle CommitCircle;
    @FXML private Label sha1Label;
    @FXML private Label branchLabel;

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
    public void setBranchLabel(String branches){
        branchLabel.setText(branches);
        branchLabel.setTooltip(new Tooltip(branches));
    }

    public int getCircleRadius() {
        return (int)CommitCircle.getRadius();
    }

}
