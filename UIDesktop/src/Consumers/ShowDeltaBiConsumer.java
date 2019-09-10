package Consumers;

import Lib.Commit;
import Lib.SHA1;
import MagitExceptions.CommitException;
import MagitExceptions.RepositoryDoesnotExistException;
import controllers.AppController;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import models.GridPaneBuilder;
import models.ListViewBuilder;
import utils.GUIUtils;

import java.text.ParseException;
import java.util.List;
import java.util.function.BiConsumer;

public class ShowDeltaBiConsumer implements BiConsumer <AppController,List<SHA1> >{

    @Override
    public void accept(AppController appController, List<SHA1> sha1List) {
        Commit current = appController.getRepositoryManager().GetCurrentRepository().getCommitFromMapCommit(sha1List.get(1));
        Commit parent=null;
        if (!current.getPrevCommits().isEmpty()) {
            parent = appController.getRepositoryManager().GetCurrentRepository().getCommitFromMapCommit(sha1List.get(0));
        }
        try {
            List<List<String>> deltas = null;
            try {
                deltas = appController.getRepositoryManager().compareTwoCommits(current, parent);
            } catch (RepositoryDoesnotExistException e) {
                GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
            }
            ListView<String> addedListView = ListViewBuilder.buildListView("Added:", 400, 400);
            ListView<String> changedListView = ListViewBuilder.buildListView("Changed:", 400, 400);
            ListView<String> deletedListView = ListViewBuilder.buildListView("Deleted:", 400, 400);
            deltas.get(0).forEach(i -> addedListView.getItems().add(i));
            deltas.get(1).forEach(i -> changedListView.getItems().add(i));
            deltas.get(2).forEach(i -> deletedListView.getItems().add(i));
            GridPane gridPane = GridPaneBuilder.buildGridPane(1, 3, 400, 400);
            gridPane.add(addedListView, 0, 0, 1, 1);
            gridPane.add(changedListView, 1, 0, 1, 1);
            gridPane.add(deletedListView, 2, 0, 1, 1);
            Stage stage = new Stage();
            stage.setScene(new Scene(gridPane));
            stage.showAndWait();

        } catch (CommitException | ParseException e) {
            GUIUtils.popUpMessage(e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}