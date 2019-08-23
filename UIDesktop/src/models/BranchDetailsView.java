package models;

import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;

public class BranchDetailsView {

    public static TitledPane render(String title, String sha1, String message) {
        TitledPane t = new TitledPane();
        t.setText(title);
        Label sha1Label = new Label("SHA-1: " + sha1);
        sha1Label.setLayoutX(0);
        sha1Label.setLayoutY(23);
        Label messageLabel = new Label("Message: " + message);
        messageLabel.setLayoutX(0);
        messageLabel.setLayoutX(sha1Label.getLayoutY() - 23);
        AnchorPane innerAnchor = new AnchorPane();
        innerAnchor.getChildren().addAll(sha1Label, messageLabel);
        t.setContent(innerAnchor);

        return t;
    }
}
