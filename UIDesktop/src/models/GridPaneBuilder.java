package models;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

public class GridPaneBuilder {
    public static GridPane buildGridPane(int numOfRow, int numOfColumn, double rowHeight, double columnWidth) {
        GridPane gridPane = new GridPane();
        for (int i = 0; i < numOfColumn; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setMinWidth(5);
            columnConstraints.setPrefWidth(columnWidth);
            columnConstraints.setHgrow(Priority.SOMETIMES);
            gridPane.getColumnConstraints().add(columnConstraints);
        }
        for (int i = 0 ; i < numOfRow; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(5);
            rowConstraints.setPrefHeight(rowHeight);
            rowConstraints.setVgrow(Priority.SOMETIMES);
            gridPane.getRowConstraints().add(rowConstraints);
        }
        return gridPane;
    }
}
