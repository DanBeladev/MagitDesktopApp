package utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class GUIUtils {

    public static Optional<ButtonType> popUpMessage(String message, Alert.AlertType popUpType){
        Alert alert=new Alert(popUpType);
        alert.setTitle(popUpType.name());
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    public static String getTextInput(String title,String headerText,String contnet,String defaultValue){
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contnet);
        Optional<String> op=dialog.showAndWait();
        if(!op.equals(Optional.empty())){
            return op.get();
        }
        return null;
    }
    public static File getFolderByDirectoryChooser(String title, Stage primaryStage){
        DirectoryChooser directoryChooser=new DirectoryChooser();
        directoryChooser.setTitle(title);
        return directoryChooser.showDialog(primaryStage);
    }
}
