package models;

import controllers.AppController;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ContextMenuCommitNode {
    private  ContextMenu contextMenu ;

    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    public ContextMenuCommitNode(){
        contextMenu = new ContextMenu();
        /*
        MenuItem m1 = new MenuItem("Show delta");
        MenuItem m2 = new MenuItem("Show commit files");
        MenuItem m3 = new MenuItem("Reset branch");
        MenuItem m4 = new MenuItem("Create new branch");
        Menu m5 = new Menu("Merge");
        contextMenu.getItems().addAll(m1,m2,m3,m4,m5);*/
    }

/*    public void addMenuItem(String content, BiConsumer consumer, Object o, Object b){
        MenuItem menuItem=new MenuItem(content);
        menuItem.setOnAction(v->consumer.accept(o,b));
        contextMenu.getItems().add(menuItem);
    }*/
    public void addSubMenuItem(Menu parent,MenuItem menuItem){
        parent.getItems().add(menuItem);
    }

    public MenuItem createMenuItem(String content, BiConsumer consumer, Object o, Object b){
        MenuItem menuItem=new MenuItem(content);
        menuItem.setOnAction(v->consumer.accept(o,b));
        return  menuItem;
    }

    public void mainMenuCreator(MenuItem menuItem){
        contextMenu.getItems().add(menuItem);
    }



}
