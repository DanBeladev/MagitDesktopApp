package models;


import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;

public class ListViewBuilder {
    public static ListView<String> buildListView(String title,int height, int width){
        ListView<String> listView=new ListView<>();
        listView.getItems().add(title);
        listView.setPrefHeight(height);
        listView.setPrefWidth(width);
        return listView;


    }
}
