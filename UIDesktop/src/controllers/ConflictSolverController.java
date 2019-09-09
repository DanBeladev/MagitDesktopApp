package controllers;

import Lib.MergeConfilct;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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

    @FXML
    private Label fileNameLabel;

    private Stage stage;
    private MergeConfilct mergeConfilct;

    public void setMergeConfilct(MergeConfilct mergeConfilct){
        this.mergeConfilct=mergeConfilct;
        setFileNameLabel(mergeConfilct.getPath());
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
    @FXML
    public void goNextAndResolve(){
        String almostFinal=finalTextArea.getText();
        String finalText=almostFinal.replace("\r","");
        mergeConfilct.resolveConflict(finalText);
        stage.close();
    }
    @FXML
    public void deleteCurrentFile(){
        stage.close();
    }

    public void setFileNameLabel(String fileFullPath){
        fileNameLabel.setText(fileFullPath);
    }
    public void getOurVersion(){
        finalTextArea.setText(oursTextArea.getText());
    }
    public void getAncestorVersion(){
        finalTextArea.setText(ancestorTextArea.getText());
    }
    public void getTheirsVersion(){
        finalTextArea.setText(theirsTextArea.getText());
    }
}

