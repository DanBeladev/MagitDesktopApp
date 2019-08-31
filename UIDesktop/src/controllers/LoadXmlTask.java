package controllers;

import Lib.RepositoryManager;
import javafx.concurrent.Task;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class LoadXmlTask extends Task<Void> {
    private File xmlFile;
    private Runnable onSuccess;
    private Consumer<List<String>> onFailed;
    private RepositoryManager repoManager;

    public LoadXmlTask(RepositoryManager repoManager, File xmlFile, Runnable onSuccess, Consumer<List<String>> onFailed) {
        this.xmlFile = xmlFile;
        this.onSuccess = onSuccess;
        this.onFailed = onFailed;
        this.repoManager = repoManager;
    }

    private Void returnErrors(List<String> errors) {
        this.onFailed.accept(errors);
        return null;
    }

    @Override
    protected Void call() throws Exception {
        List<String> errors = repoManager.CheckXml(xmlFile.getAbsolutePath());
        if (!errors.isEmpty()) {
            return returnErrors(errors);
        }
        try{
            repoManager.LoadXML();
        }catch (Exception e){
            errors.add(e.getMessage());
            return returnErrors(errors);
        }

        onSuccess.run();

        return null;
    }


}