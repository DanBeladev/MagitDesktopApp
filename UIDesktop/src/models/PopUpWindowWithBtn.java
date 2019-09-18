package models;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class PopUpWindowWithBtn {

    public static void popUpWindow(int hegiht, int width,String buttonText, Consumer consumer, Object o,Node... nodes){
        VBox vBox=new VBox();
        vBox.setPrefHeight(hegiht);
        vBox.setPrefWidth(width);
        for(Node node:nodes){
            vBox.getChildren().add(node);
        }
        vBox.setAlignment(Pos.CENTER);
        Button button=new Button(buttonText);
        vBox.getChildren().add(button);
        vBox.getChildren().stream().forEach(v->vBox.setMargin(v,new Insets(20,20,20,20)));
        Stage stage=new Stage();
        Scene scene=new Scene(vBox);
        button.setOnAction(v->{consumer.accept(o);stage.close();});
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}
