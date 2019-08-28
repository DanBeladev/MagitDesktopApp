package models;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class ContextMenuCommitNode {
    private  ContextMenu contextMenu ;

    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    public ContextMenuCommitNode(){
        contextMenu = new ContextMenu();
        MenuItem m1 = new MenuItem("Show delta");
        MenuItem m2 = new MenuItem("Show commit files");
        MenuItem m3 = new MenuItem("Reset branch");
        MenuItem m4 = new MenuItem("Create new branch");
        Menu m5 = new Menu("Merge");
        contextMenu.getItems().addAll(m1,m2,m3,m4,m5);
    }
}
