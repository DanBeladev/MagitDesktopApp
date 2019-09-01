package controllers;

import Lib.MergeConfilct;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.beans.EventHandler;

public class ConflictSolverController {

    @FXML
    private TextArea finalTextArea;

    @FXML
    private TextArea oursTextArea;

    @FXML
    private TextArea theirsTextArea;

    @FXML
    private TextArea ancestorTextArea;

    private Stage stage;
    private MergeConfilct mergeConfilct;

    public void setMergeConfilct(MergeConfilct mergeConfilct){
        this.mergeConfilct=mergeConfilct;
    }
    public void setStage(Stage stage){
        this.stage=stage;
    }

    public void setOursTextArea(String finalTextArea) {
        this.oursTextArea.setText(finalTextArea);
    }
    public void setAncestorTextArea(String finalTextArea) {
        this.ancestorTextArea.setText(finalTextArea);
    }
    public void setTheirsTextArea(String finalTextArea) {
        this.theirsTextArea.setText(finalTextArea);
    }
    public void goNextAndResolve(){
        mergeConfilct.resolveConflict(finalTextArea.getText());
        stage.close();
    }
    public void deleteCurrentFile(){
        stage.close();
    }
}

