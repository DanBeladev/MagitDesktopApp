package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ConflictSolverController {

    @FXML
    private TextArea finalTextArea;

    @FXML
    private TextArea oursTextArea;

    @FXML
    private TextArea theirsTextArea;

    @FXML
    private TextArea ancestorTextArea;

    public void setOursTextArea(String finalTextArea) {
        this.oursTextArea.setText(finalTextArea);
    }
    public void setAncestorTextArea(String finalTextArea) {
        this.ancestorTextArea.setText(finalTextArea);
    }
    public void setTheirsTextArea(String finalTextArea) {
        this.theirsTextArea.setText(finalTextArea);
    }
    public void goNext(){

    }
}

